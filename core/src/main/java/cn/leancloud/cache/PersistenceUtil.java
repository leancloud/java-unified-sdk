package cn.leancloud.cache;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PersistenceUtil {
  private static PersistenceUtil INSTANCE = new PersistenceUtil();
  private static final int MAX_FILE_BUF_SIZE = 1024*1024*2;

  private String documentDir = "./data/";
  private String fileCacheDir = "./file/";
  private String commandCacheDir = "./command/";
  private String analyticsCacheDir = "./stats/";
  private String queryResultCacheDir = "./PaasKeyValueCache";
  private ConcurrentHashMap<String, ReentrantReadWriteLock> fileLocks =
          new ConcurrentHashMap<String, ReentrantReadWriteLock>();
  private SystemSetting defaultSetting = null;

  private PersistenceUtil() {
  }

  public static PersistenceUtil sharedInstance() {
    return INSTANCE;
  }

  public void config(String documentDir, String fileCacheDir, String queryResultCacheDir,
                     String commandCacheDir, String analyticsCacheDir,
                     SystemSetting defaultSetting) {
    this.documentDir = documentDir;
    if (!documentDir.endsWith("/")) {
      this.documentDir += "/";
    }
    File dirFile = new File(this.documentDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }

    this.fileCacheDir = fileCacheDir;
    if (!fileCacheDir.endsWith("/")) {
      this.fileCacheDir += "/";
    }
    dirFile = new File(this.fileCacheDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }

    if (!queryResultCacheDir.endsWith("/")) {
      queryResultCacheDir += "/";
    }
    dirFile = new File(queryResultCacheDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }
    this.queryResultCacheDir = queryResultCacheDir;

    this.commandCacheDir = commandCacheDir;
    if (!commandCacheDir.endsWith("/")) {
      this.commandCacheDir += "/";
    }
    dirFile = new File(this.commandCacheDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }

    this.analyticsCacheDir = analyticsCacheDir;
    if (!analyticsCacheDir.endsWith("/")) {
      this.analyticsCacheDir += "/";
    }
    dirFile = new File(this.analyticsCacheDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }

    this.defaultSetting = defaultSetting;
  }

  public String getDocumentDir() {
    return documentDir;
  }

  public String getFileCacheDir() {
    return fileCacheDir;
  }

  public String getQueryResultCacheDir() {
    return this.queryResultCacheDir;
  }

  public String getCommandCacheDir() {
    return commandCacheDir;
  }

  public String getAnalyticsCacheDir() {
    return analyticsCacheDir;
  }

  public SystemSetting getDefaultSetting() {
    return defaultSetting;
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
  void removeLock(String path) {
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
    Lock writeLock = getLock(fileForSave.getAbsolutePath()).writeLock();
    boolean succeed = true;
    FileOutputStream out = null;
    if (writeLock.tryLock()) {
      try {
        out = new FileOutputStream(fileForSave, false);
        out.write(content);
      } catch (Exception e) {
        succeed = false;
      } finally {
        if (out != null) {
          closeQuietly(out);
        }
        writeLock.unlock();
      }
    }
    return succeed;
  }

  public byte[] readContentBytesFromFile(File fileForRead) {
    if (fileForRead == null) {
      return null;
    };
    if (!fileForRead.exists() || !fileForRead.isFile()) {
      return null;
    }
    Lock readLock = getLock(fileForRead.getAbsolutePath()).readLock();
    readLock.lock();
    byte[] data = null;
    InputStream input = null;
    try {
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
      readLock.unlock();
    }
    return null;
  }

  public boolean deleteFile(String localPath) {
    return deleteFile(new File(localPath));
  }

  public boolean deleteFile(File localFile) {
    if (null == localFile || !localFile.exists()) {
      return false;
    }
    Lock writeLock = getLock(localFile.getAbsolutePath()).writeLock();
    if (writeLock.tryLock()) {
      localFile.delete();
      writeLock.unlock();
    }
    return true;
  }

  public boolean saveFileToLocal(String localPath, File inputFile) {
    boolean succeed = false;
    FileOutputStream os = null;
    InputStream is = null;

    Lock writeLock = getLock(localPath).writeLock();
    if (writeLock.tryLock()) {
      try {
        is = getInputStreamFromFile(inputFile);
        os = getOutputStreamForFile(new File(localPath), false);
        byte buf[] = new byte[MAX_FILE_BUF_SIZE];
        int len  = 0;
        while((len = is.read(buf)) != -1) {
          os.write(buf, 0, len);
        }
        succeed = true;
      } catch (IOException ex) {
        succeed = false;
      } finally {
        if (null != is) {
          closeQuietly(is);
        }
        if (null != os) {
          closeQuietly(os);
        }
        writeLock.unlock();
      }
    }
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
