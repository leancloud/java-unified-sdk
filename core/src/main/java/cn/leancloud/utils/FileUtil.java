package cn.leancloud.utils;

import cn.leancloud.LCFile;

import java.util.regex.Pattern;

public class FileUtil {
  public static final int DEFAULT_FILE_KEY_LEN = 40;
  public static final String DEFAULTMIMETYPE = "application/octet-stream";

  public static interface MimeTypeDetector {
    String getMimeTypeFromUrl(String url);
    String getMimeTypeFromPath(String filePath);
    String getMimeTypeFromExtension(String extension);
  }
  private static MimeTypeDetector detector = new DefaultMimeTypeDetector();
  public static void config(MimeTypeDetector mimeTypeDetector) {
    detector = mimeTypeDetector;
  }

//  public static String generateFileKey(String name, boolean keepFilename) {
//    String key = StringUtil.getRandomString(DEFAULT_FILE_KEY_LEN);
//    int idx = 0;
//    if (!StringUtil.isEmpty(name)) {
//      idx = name.lastIndexOf(".");
//    }
//    if (keepFilename) {
//      key += "/" + name;
//    } else if (idx > 0) {
//      String postFix = name.substring(idx);
//      key += postFix;
//    }
//    return key;
//  }

  public static String getExtensionFromFilename(String filename) {
    if (!StringUtil.isEmpty(filename) && Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename)) {
      int dotPos = filename.lastIndexOf('.');
      if (0 <= dotPos) {
        return filename.substring(dotPos + 1);
      }
    }
    return "";
  }

  public static String getFileMimeType(LCFile avFile) {
    String fileName = avFile.getName();
    String fileUrl = avFile.getUrl();
    String mimeType = null;
    if (!StringUtil.isEmpty(fileName)) {
      mimeType = getMimeTypeFromFilename(fileName);
    } else if (!StringUtil.isEmpty(fileUrl)) {
      mimeType = getMimeTypeFromUrl(fileUrl);
    }
    if (StringUtil.isEmpty(mimeType)) {
      mimeType = DEFAULTMIMETYPE;
    }
    return mimeType;
  }

  public static String getMimeTypeFromFilename(String fileName) {
    String extension = getExtensionFromFilename(fileName);
    if (!StringUtil.isEmpty(extension)) {
      String result = detector.getMimeTypeFromExtension(extension);
      return null != result? result : "";
    }
    return "";
  }

  public static String getMimeTypeFromPath(String localPath) {
    if (!StringUtil.isEmpty(localPath)) {
      String result = detector.getMimeTypeFromPath(localPath);
      return null != result? result : "";
    }
    return "";
  }

  public static String getMimeTypeFromUrl(String url) {
    if (!StringUtil.isEmpty(url)) {
      String result = detector.getMimeTypeFromUrl(url);
      return null != result? result : "";
    }
    return "";
  }
}
