package cn.leancloud.cache;

import cn.leancloud.codec.MD5;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.utils.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileCache extends LocalStorage{
  private static final int MAX_FILE_BUF_SIZE = 4 * 1024 * 1024;
  private static FileCache INSTANCE = null;

  public static FileCache getIntance() {
    if (null == INSTANCE) {
      synchronized (FileCache.class) {
        if (null == INSTANCE) {
          INSTANCE = new FileCache();
        }
      }
    }
    return INSTANCE;
  }

  private FileCache() {
    super(AppConfiguration.getFileCacheDir());
  }

  public String saveLocalFile(String name, File localFile) {
    return super.saveFile(name, localFile);
  }

  public File getCacheFile(String url) {
    try {
      String urlMd5 = MD5.computeMD5(url.getBytes("UTF-8"));
      return super.getCacheFile(urlMd5);
    } catch (Exception ex) {
      return null;
    }
  }
}
