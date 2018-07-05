package cn.leancloud.im;

import java.io.File;
import java.util.Map;

public class SimpleFileMetaAccessor implements FileMetaAccessor {
  public Map<String, Object> mediaInfo(File file) {
    return null;
  }
  public String getMimeType(String url) {
    return null;
  }
  public Map<String, Object> getImageMeta(File file) {
    return null;
  }
}
