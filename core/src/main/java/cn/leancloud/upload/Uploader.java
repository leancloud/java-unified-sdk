package cn.leancloud.upload;

import cn.leancloud.AVException;

public interface Uploader {
  AVException doWork();

  String getFinalUrl();
  String getFinalObjectId();

  void execute();

  void publishProgress(int percentage);

  boolean cancel(boolean interrupt);

  boolean isCancelled();

  static interface UploadCallback {
    void finishedWithResults(String finalObjectId, String finalUrl);
  }
}
