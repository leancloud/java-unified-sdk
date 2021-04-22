package cn.leancloud.utils;

import cn.leancloud.LCException;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import retrofit2.HttpException;
import retrofit2.Response;

import java.io.IOException;

public class ErrorUtils {
  public static LCException propagateException(String content) {
    try {
      JSONObject object = JSON.parseObject(content);
      String errorMessage = object.getString("error");
      int code = object.getIntValue("code");
      return new LCException(code, errorMessage);
    } catch (Exception exception) {
      return new LCException(LCException.UNKNOWN, content);
    }
  }

  public static LCException propagateException(Throwable throwable) {
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
            LCException exception = ErrorUtils.propagateException(content);
            return exception;
          } catch (IOException ex) {
            ;
          }
        }
      }
    }
    return new LCException(LCException.UNKNOWN, throwable.getMessage());
  };

  public static LCException propagateException(int code, String content) {
    return new LCException(code, content);
  }

  public static LCException sessionMissingException() {
    return ErrorUtils.propagateException(LCException.SESSION_MISSING,
            "No valid session token, make sure signUp or login has been called.");
  }

  public static LCException illegalArgument(String message) {
    return ErrorUtils.propagateException(LCException.INVALID_PARAMETER, message);
  }

  public static LCException invalidObjectIdException() {
    return ErrorUtils.propagateException(LCException.MISSING_OBJECT_ID, "Invalid object id.");
  }

  public static LCException invalidStateException() {
    return ErrorUtils.propagateException(LCException.INVALID_STATE, "Invalid State Exception.");
  }

  public static LCException invalidStateException(String message) {
    return ErrorUtils.propagateException(LCException.INVALID_STATE, message);
  }
}
