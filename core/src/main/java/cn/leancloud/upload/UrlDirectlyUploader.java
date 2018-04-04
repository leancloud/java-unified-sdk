package cn.leancloud.upload;

import cn.leancloud.AVException;
import cn.leancloud.callback.ProgressCallback;
import cn.leancloud.AVFile;

public class UrlDirectlyUploader extends HttpClientUploader {
  public UrlDirectlyUploader(AVFile avFile,
                             ProgressCallback progressCallback) {
    super(avFile, progressCallback);
  }

  public AVException execute() {
    return new AVException(new UnsupportedOperationException("UrlDirectlyUploader is deprecated."));
  }
}