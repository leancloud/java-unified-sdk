package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;

public abstract class GenericObjectCallback {
  public void onSuccess(String content, AVException e) {}

  public void onFailure(int statusCode, Throwable error, String content) {
    if (isRetryNeeded(statusCode, error)) {
      retry(error, content);
    } else {
      onFailure(error, content);
    }
  }

  public void onFailure(Throwable error, String content) {

  }


  public void onGroupRequestFinished(int left, int total, AVObject object) {

  }

  public boolean isRetryNeeded(int statusCode, Throwable error) {
    return false;
  }

  public void retry(Throwable error, String content) {

  }

  public boolean isRequestStatisticNeed() {
    return true;
  }
}