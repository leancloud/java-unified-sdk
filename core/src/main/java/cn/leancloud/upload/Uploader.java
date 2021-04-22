package cn.leancloud.upload;

import cn.leancloud.LCException;

public interface Uploader {
  LCException execute();

  void publishProgress(int percentage);

  boolean cancel(boolean interrupt);

  boolean isCancelled();
}
