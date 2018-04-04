package cn.leancloud.core;

import cn.leancloud.core.AVOSCloud;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class RequestPaddingInterceptor implements Interceptor {
  private static final String HEADER_KEY_LC_SESSIONTOKEN = "X-LC-Session";
  private static final String HEADER_KEY_LC_APPID = "X-LC-Id";
  private static final String HEADER_KEY_LC_APPKEY = "X-LC-Key";
  private static final String HEADER_KEY_LC_PROD_MODE = "X-LC-Prod";
  private static final String HEADER_KEY_LC_SIGN = "X-LC-Sign";
  private static final String HEADER_KEY_ACCEPT = "Accept";
  private static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_KEY_USER_AGENT = "User-Agent";
  private static final String DEFAULT_CONTENT_TYPE = "application/json";
  private static final String SDK_VERSION = "5.0.0";
  private static final String DEFAULT_USER_AGENT = "AVOS Cloud Android-" + SDK_VERSION + " SDK";

  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request originalRequest = chain.request();
    Request newRequest = originalRequest.newBuilder()
            .header(HEADER_KEY_LC_PROD_MODE, AVOSCloud.isProductionMode()?"1":"0")
            .header(HEADER_KEY_LC_APPID, AVOSCloud.getApplicationId())
            .header(HEADER_KEY_LC_APPKEY, AVOSCloud.getApplicationKey())
            .header(HEADER_KEY_ACCEPT, DEFAULT_CONTENT_TYPE)
            .header(HEADER_KEY_CONTENT_TYPE, DEFAULT_CONTENT_TYPE)
            .header(HEADER_KEY_USER_AGENT, DEFAULT_USER_AGENT)
            .header(HEADER_KEY_LC_SESSIONTOKEN, "")
            .build();
    return chain.proceed(newRequest);
  }
}
