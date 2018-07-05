package cn.leancloud.im;

import java.io.File;
import java.util.Map;

public interface FileMetaAccessor {
  Map<String, Object> mediaInfo(File file);
  String getMimeType(String url);
  Map<String, Object> getImageMeta(File file);
}
