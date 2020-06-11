package cn.leancloud.utils;

import cn.leancloud.AVException;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import retrofit2.HttpException;
import retrofit2.Response;

import java.io.IOException;

public class ErrorUtils {
  public static AVException propagateException(String content) {
    try {
      JSONObject object = JSON.parseObject(content);
      String errorMessage = object.getString("error");
      int code = object.getIntValue("code");
      return new AVException(code, errorMessage);
    } catch (Exception exception) {
      return new AVException(AVException.UNKNOWN, content);
    }
  }

  public static AVException propagateException(Throwable throwable) {
    if (null == throwable) {
      return null;
    }
    if (throwable instanceof HttpException) {
      HttpException httpException = (HttpException) throwable;
      if (null != httpException.response()) {
        Response response = httpException.response();
        if (null != response && null != response.errorBody()) {
          try {
            String content = response.errorBody().string();
            AVException exception = ErrorUtils.propagateException(content);
            return exception;
          } catch (IOException ex) {
            ;
          }
        }
      }
    }
    return new AVException(AVException.UNKNOWN, throwable.getMessage());
  };

  public static AVException propagateException(int code, String content) {
    return new AVException(code, content);
  }

  public static AVException sessionMissingException() {
    return ErrorUtils.propagateException(AVException.SESSION_MISSING,
            "No valid session token, make sure signUp or login has been called.");
  }

  public static AVException illegalArgument(String message) {
    return ErrorUtils.propagateException(AVException.INVALID_PARAMETER, message);
  }

  public static AVException invalidObjectIdException() {
    return ErrorUtils.propagateException(AVException.MISSING_OBJECT_ID, "Invalid object id.");
  }

  public static AVException invalidStateException() {
    return ErrorUtils.propagateException(AVException.INVALID_STATE, "Invalid State Exception.");
  }

  public static AVException invalidStateException(String message) {
    return ErrorUtils.propagateException(AVException.INVALID_STATE, message);
  }
}
