package cn.leancloud.im.v2;

import cn.leancloud.LCException;

public class LCIMException extends LCException {
  int appCode;

  public LCIMException(int code, int appCode, String theMessage) {
    super(code, theMessage);
    this.appCode = appCode;
  }

  public LCIMException(int theCode, String theMessage) {
    super(theCode, theMessage);
  }

  public LCIMException(int appCode, String message, Throwable cause) {
    super(message, cause);
    this.appCode = appCode;
  }

  public LCIMException(String message, Throwable cause) {
    super(message, cause);
  }

  public LCIMException(int appCode, Throwable cause) {
    super(cause);
    this.appCode = appCode;
  }

  public LCIMException(Throwable cause) {
    super(cause);
    if (cause instanceof LCIMException) {
      this.appCode = ((LCIMException) cause).getAppCode();
    }
  }

  public LCIMException(int appCode, LCException error) {
    super(error.getMessage(), error.getCause());
    this.appCode = appCode;
  }

  /**
   * 获取由用户在云代码中自定义的response code
   *
   * @return response code.
   */
  public int getAppCode() {
    return appCode;
  }

  void setAppCode(int appCode) {
    this.appCode = appCode;
  }

  public static LCIMException wrapperAVException(Throwable e) {
    if (e == null) {
      return null;
    } else if (e instanceof LCIMException){
      return (LCIMException)e;
    } else {
      return new LCIMException(e);
    }
  }
}