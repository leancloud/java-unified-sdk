package cn.leancloud.core.cache;

import cn.leancloud.utils.StringUtil;

import java.io.File;
import java.io.InputStream;

public class FileCache {
  private static final int MAX_FILE_BUF_SIZE = 4 * 1024 * 1024;

  public static FileCache getIntance() {
    return null;
  }

  public String saveData(String name, byte[] content) {
    String path = PersistenceUtil.sharedInstance().getFileCacheDir() + name;
    PersistenceUtil.sharedInstance().saveContentToFile(content, new File(path));
    return path;
  }

  public String saveLocalFile(String name, File localFile) {
    if (StringUtil.isEmpty(name)) {
      return null;
    }
    if (null == localFile || !localFile.exists() || !localFile.isFile()) {
      return null;
    }
    String path = PersistenceUtil.sharedInstance().getFileCacheDir() + name;
    boolean saveResult = PersistenceUtil.sharedInstance().saveFileToLocal(path, localFile);
    if (saveResult) {
      return path;
    } else {
      return null;
    }
  }

  public File getCacheFile(String url) {
    return null;
  }

  public InputStream getInputStreamFromFile(File file) {
    return null;
  }

  public void clearCachedFile(String name) {
    if (StringUtil.isEmpty(name)) {
      return;
    }
    String path = PersistenceUtil.sharedInstance().getFileCacheDir() + name;
    PersistenceUtil.sharedInstance().deleteFile(path);
  }

  public void clearAllCachedFiles() {
    clearCacheMoreThanDays(0);
  }

  public void clearCacheMoreThanDays(int days) {
    long curTime = System.currentTimeMillis();
    if ( days > 0) {
      curTime -= days * 86400000; // 86400000 is one day.
    }
    String fileCacheDir = PersistenceUtil.sharedInstance().getFileCacheDir();
    PersistenceUtil.sharedInstance().clearDir(fileCacheDir, curTime);
  }
}
