package cn.leancloud.utils;

import cn.leancloud.AVException;

public class ErrorUtils {
  public static AVException createException(int code, String content) {
    return new AVException(code, content);
  }

  public static AVException sessionMissingException() {
    return ErrorUtils.createException(AVException.SESSION_MISSING,
            "No valid session token, make sure signUp or login has been called.");
  }

  static public AVException invalidObjectIdException() {
    return ErrorUtils.createException(AVException.MISSING_OBJECT_ID, "Invalid object id.");
  }
}
