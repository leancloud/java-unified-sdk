package cn.leancloud.im.v2;

import cn.leancloud.AVException;

public class AVIMException extends AVException {
  int appCode;

  public AVIMException(int code, int appCode, String theMessage) {
    super(code, theMessage);
    this.appCode = appCode;
  }

  public AVIMException(int theCode, String theMessage) {
    super(theCode, theMessage);
  }

  public AVIMException(int appCode, String message, Throwable cause) {
    super(message, cause);
    this.appCode = appCode;
  }

  public AVIMException(String message, Throwable cause) {
    super(message, cause);
  }

  public AVIMException(int appCode, Throwable cause) {
    super(cause);
    this.appCode = appCode;
  }

  public AVIMException(Throwable cause) {
    super(cause);
    if (cause instanceof AVIMException) {
      this.appCode = ((AVIMException) cause).getAppCode();
    }
  }

  public AVIMException(int appCode, AVException error) {
    super(error.getMessage(), error.getCause());
    this.appCode = appCode;
  }

  /**
   * 获取由用户在云代码中自定义的response code
   *
   * @return
   */
  public int getAppCode() {
    return appCode;
  }

  void setAppCode(int appCode) {
    this.appCode = appCode;
  }

  public static AVIMException wrapperAVException(Throwable e) {
    if (e == null) {
      return null;
    } else if (e instanceof AVIMException){
      return (AVIMException)e;
    } else {
      return new AVIMException(e);
    }
  }
}