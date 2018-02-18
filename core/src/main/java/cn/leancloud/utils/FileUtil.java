package cn.leancloud.utils;

import cn.leancloud.core.AVFile;

public class FileUtil {
  public static final int DEFAULT_FILE_KEY_LEN = 40;

  public static interface MimeTypeDetector {
    String getFileExtensionFromUrl(String url);
    String getMimeTypeFromExtension(String extension);
  }
  private static MimeTypeDetector detector = null;
  public static void config(MimeTypeDetector mimeTypeDetector) {
    detector = mimeTypeDetector;
  }

  public static String generateFileKey(String name) {
    String key = StringUtil.getRandomString(DEFAULT_FILE_KEY_LEN);
    int idx = 0;
    if (!StringUtil.isEmpty(name)) {
      idx = name.lastIndexOf(".");
    }
    if (idx > 0) {
      String postFix = name.substring(idx);
      key += postFix;
    }
    return key;
  }

  public static String getFileMimeType(AVFile avFile) {
    String fileName = avFile.getName();
    String fileUrl = avFile.getUrl();
    String mimeType = AVFile.DEFAULTMIMETYPE;
    if (!StringUtil.isEmpty(fileName)) {
      mimeType = getMimeTypeFromLocalFile(fileName);
    } else if (!StringUtil.isEmpty(fileUrl)) {
      mimeType = getMimeTypeFromUrl(fileUrl);
    }
    return mimeType;
  }

  public static String getMimeTypeFromLocalFile(String localPath) {
    if (!StringUtil.isEmpty(localPath) && localPath.contains(".")) {
      String extension = localPath.substring(localPath.lastIndexOf('.') + 1);
      if (!StringUtil.isEmpty(extension)) {
        return detector.getMimeTypeFromExtension(extension);
      }
    }
    return "";
  }

  public static String getMimeTypeFromUrl(String url) {
    if (!StringUtil.isEmpty(url)) {
      String extension = detector.getFileExtensionFromUrl(url);
      if (!StringUtil.isEmpty(extension)) {
        return detector.getMimeTypeFromExtension(extension);
      }
    }
    return "";
  }
}
