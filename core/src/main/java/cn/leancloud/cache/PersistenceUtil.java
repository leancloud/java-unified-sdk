package cn.leancloud.cache;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PersistenceUtil {
  private static PersistenceUtil INSTANCE = new PersistenceUtil();
  private static final int MAX_FILE_BUF_SIZE = 1024*1024*2;

  private ConcurrentHashMap<String, ReentrantReadWriteLock> fileLocks =
          new ConcurrentHashMap<String, ReentrantReadWriteLock>();

  private PersistenceUtil() {
  }

  public static PersistenceUtil sharedInstance() {
    return INSTANCE;
  }

  public ReentrantReadWriteLock getLock(String path) {
    ReentrantReadWriteLock lock = fileLocks.get(path);
    if (lock == null) {
      lock = new ReentrantReadWriteLock();
      ReentrantReadWriteLock oldLock = fileLocks.putIfAbsent(path, lock);
      if (oldLock != null) {
        lock = oldLock;
      }
    }
    return lock;
  }
  public void removeLock(String path) {
    fileLocks.remove(path);
  }

  static void closeQuietly(Closeable closeable) {
    try {
      if (closeable != null) closeable.close();
    } catch (IOException e) {
      //
    }
  }
  public boolean saveContentToFile(String content, File fileForSave) {
    try {
      return saveContentToFile(content.getBytes("utf-8"), fileForSave);
    } catch (UnsupportedEncodingException e) {
      return false;
    }
  }

  public boolean saveContentToFile(byte[] content, File fileForSave) {
    boolean succeed = true;
    FileOutputStream out = null;
    Lock writeLock = getLock(fileForSave.getAbsolutePath()).writeLock();
    writeLock.lock();
    try {
      out = new FileOutputStream(fileForSave, false);
      out.write(content);
    } catch (Exception e) {
      succeed = false;
    } finally {
      if (out != null) {
        closeQuietly(out);
      }
    }
    writeLock.unlock();

    return succeed;
  }

  public String readContentFromFile(File fileForRead) {
    byte[] data = this.readContentBytesFromFile(fileForRead);
    if (null == data || data.length < 1) {
      return "";
    } else {
      return new String(data);
    }
  }

  public byte[] readContentBytesFromFile(File fileForRead) {
    if (fileForRead == null) {
      return null;
    };
    if (!fileForRead.exists() || !fileForRead.isFile()) {
      return null;
    }
    InputStream input = null;
    Lock readLock = getLock(fileForRead.getAbsolutePath()).readLock();
    readLock.lock();
    try {
      byte[] data = null;
      data = new byte[(int) fileForRead.length()];
      int totalBytesRead = 0;
      input = new BufferedInputStream(new FileInputStream(fileForRead), 8192);
      while (totalBytesRead < data.length) {
        int bytesRemaining = data.length - totalBytesRead;
        int bytesRead = input.read(data, totalBytesRead, bytesRemaining);
        if (bytesRead > 0) {
          totalBytesRead = totalBytesRead + bytesRead;
        }
      }
      return data;
    } catch (IOException e) {
      ;
    } finally {
      closeQuietly(input);
    }
    readLock.unlock();
    return null;
  }

  public boolean deleteFile(String localPath) {
    return deleteFile(new File(localPath));
  }

  public boolean deleteFile(File localFile) {
    if (null == localFile || !localFile.exists()) {
      return false;
    }
    boolean result = true;
    Lock writeLock = getLock(localFile.getAbsolutePath()).writeLock();
    if (writeLock.tryLock()) {
      result = localFile.delete();
      writeLock.unlock();
    }
    return result;
  }

  public boolean saveFileToLocal(String localPath, File inputFile) {
    boolean succeed = false;
    FileOutputStream os = null;
    InputStream is = null;

    Lock writeLock = getLock(localPath).writeLock();
    writeLock.lock();
    try {
      is = getInputStreamFromFile(inputFile);
      os = getOutputStreamForFile(new File(localPath), false);
      byte buf[] = new byte[MAX_FILE_BUF_SIZE];
      int len  = 0;
      if (null != is && null != os) {
        while ((len = is.read(buf)) != -1) {
          os.write(buf, 0, len);
        }
        succeed = true;
      }
    } catch (Exception ex) {
      succeed = false;
    } finally {
      if (null != is) {
        closeQuietly(is);
      }
      if (null != os) {
        closeQuietly(os);
      }
    }
    writeLock.unlock();
    return succeed;
  }

  public static FileOutputStream getOutputStreamForFile(File fileForWrite, boolean append) throws IOException {
    if (null == fileForWrite) {
      return null;
    }
    return new FileOutputStream(fileForWrite, append);
  }

  public static InputStream getInputStreamFromFile(File fileForRead) throws IOException{
    if (fileForRead == null) {
      return null;
    };
    if (!fileForRead.exists() || !fileForRead.isFile()) {
      return null;
    }
    return new BufferedInputStream(new FileInputStream(fileForRead), 8192);
  }

  public void clearDir(String dirPath, long lastModified) {
    File dir = new File(dirPath);
    if (null == dir || !dir.exists() || !dir.isDirectory()) {
      return;
    }
    File[] files = dir.listFiles();
    for (File f: files) {
      if (f.isFile()) {
        if (f.lastModified() < lastModified) {
          deleteFile(f);
        }
      } else if (f.isDirectory()) {
        clearDir(f.getAbsolutePath(), lastModified);
      }
    }
  }
}
