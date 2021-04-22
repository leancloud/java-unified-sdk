package cn.leancloud.upload;

import cn.leancloud.LCException;
import cn.leancloud.callback.ProgressCallback;
import cn.leancloud.LCFile;

public class UrlDirectlyUploader extends HttpClientUploader {
  public UrlDirectlyUploader(LCFile avFile,
                             ProgressCallback progressCallback) {
    super(avFile, progressCallback);
  }

  public LCException execute() {
    return new LCException(new UnsupportedOperationException("UrlDirectlyUploader is deprecated."));
  }


}