package cn.leancloud.utils;


import java.io.File;
import java.nio.file.*;

public class DefaultMimeTypeDetector implements FileUtil.MimeTypeDetector{
  public String getMimeTypeFromUrl(String url) {
    if (!StringUtil.isEmpty(url)) {
      int fragment = url.lastIndexOf('#');
      if (fragment > 0) {
        url = url.substring(0, fragment);
      }

      int query = url.lastIndexOf('?');
      if (query > 0) {
        url = url.substring(0, query);
      }

      int filenamePos = url.lastIndexOf('/');
      String filename =
              0 <= filenamePos ? url.substring(filenamePos + 1) : url;

      String extension = FileUtil.getExtensionFromFilename(filename);
      return getMimeTypeFromExtension(extension);
    }

    return "";
  }
  public String getMimeTypeFromPath(String localPath) {
    try {
      return Files.probeContentType(new File(localPath).toPath());
    } catch (Exception ex) {
      return "";
    }
  }
  public String getMimeTypeFromExtension(String extension) {
    if (StringUtil.isEmpty(extension)) {
      return "";
    }
    return MimeUtils.guessExtensionFromMimeType(extension);
  }
}
