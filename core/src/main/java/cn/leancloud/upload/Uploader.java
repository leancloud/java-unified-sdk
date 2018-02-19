package cn.leancloud.upload;

import cn.leancloud.AVException;

public interface Uploader {
  AVException execute();

  void publishProgress(int percentage);

  boolean cancel(boolean interrupt);

  boolean isCancelled();
}
