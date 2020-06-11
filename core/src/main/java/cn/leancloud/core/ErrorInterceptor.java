package cn.leancloud.core;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class ErrorInterceptor implements Interceptor {
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request originalRequest = chain.request();
    Response response = chain.proceed(originalRequest);
    int responseCode = response.code();
    if (responseCode >= 300) {
      throwError(response, responseCode);
      return response;
    } else {
      return response;
    }
  }

  private void throwError(Response response, int responseCode) throws IOException {
//    String responseBody = response.body().string();
//    AVException avException = ErrorUtils.propagateException(responseBody);
//    throw avException;
  }
}
