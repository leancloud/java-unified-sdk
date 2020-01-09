package cn.leancloud.cache;

import cn.leancloud.utils.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class LocalStorage {
  private String baseDir;
  private boolean disableLocalCache = false;

  public LocalStorage(String baseDir) {
    if (StringUtil.isEmpty(baseDir)) {
      disableLocalCache = true;
    } else {
      if (!baseDir.endsWith("/")) {
        baseDir += "/";
      }
      File root = new File(baseDir);
      if (!root.exists()) {
        root.mkdirs();
      }

      this.baseDir = baseDir;
    }
  }

  public String saveData(String key, byte[] content) {
    if (disableLocalCache) {
      return null;
    }
    if (StringUtil.isEmpty(key) || null == content) {
      return null;
    }
    String path = baseDir + key;
    PersistenceUtil.sharedInstance().saveContentToFile(content, new File(path));
    return path;
  }

  public String saveFile(String key, File localFile) {
    if (disableLocalCache) {
      return null;
    }
    if (StringUtil.isEmpty(key)) {
      return null;
    }
    if (null == localFile || !localFile.exists() || !localFile.isFile()) {
      return null;
    }
    String path = baseDir + key;
    boolean saveResult = PersistenceUtil.sharedInstance().saveFileToLocal(path, localFile);
    if (saveResult) {
      return path;
    } else {
      return null;
    }
  }

  public byte[] readData(String key) {
    if (disableLocalCache) {
      return null;
    }
    if (StringUtil.isEmpty(key)) {
      return null;
    }
    String path = baseDir + key;
    return readData(new File(path));
  }

  public byte[] readData(File file) {
    if (disableLocalCache) {
      return null;
    }
    return PersistenceUtil.sharedInstance().readContentBytesFromFile(file);
  }

  public File getCacheFile(String key) {
    if (disableLocalCache) {
      return null;
    }
    if (StringUtil.isEmpty(key)) {
      return null;
    }
    return new File(this.baseDir + key);
  }

  public InputStream getInputStreamFromFile(File file) throws FileNotFoundException {
    if (disableLocalCache) {
      return null;
    }
    return new FileInputStream(file);
  }

  public void clearCachedFile(String key) {
    if (disableLocalCache || StringUtil.isEmpty(key)) {
      return;
    }
    String path = baseDir + key;
    PersistenceUtil.sharedInstance().deleteFile(path);
  }

  public void clearAllCachedFiles() {
    if (disableLocalCache) {
      return;
    }
    clearCacheMoreThanDays(0);
  }

  public void clearCacheMoreThanDays(int days) {
    if (disableLocalCache) {
      return;
    }
    long curTime = System.currentTimeMillis();
    if ( days > 0) {
      curTime -= 86400000L * days; // 86400000 is one day.
    }
    PersistenceUtil.sharedInstance().clearDir(baseDir, curTime);
  }
}
