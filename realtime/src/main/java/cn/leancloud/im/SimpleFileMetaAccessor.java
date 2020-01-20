package cn.leancloud.im;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SimpleFileMetaAccessor implements FileMetaAccessor {
  public Map<String, Object> mediaInfo(File file) {
    return new HashMap<>();
  }
  public String getMimeType(String url) {
    return null;
  }
  public Map<String, Object> getImageMeta(File file) {
    return new HashMap<>();
  }
}
