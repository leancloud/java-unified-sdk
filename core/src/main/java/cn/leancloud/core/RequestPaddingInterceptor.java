package cn.leancloud.core;

import cn.leancloud.AVCloud;
import cn.leancloud.AVUser;
import cn.leancloud.core.AVOSCloud;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class RequestPaddingInterceptor implements Interceptor {
  public static final String HEADER_KEY_LC_SESSIONTOKEN = "X-LC-Session";
  public static final String HEADER_KEY_LC_APPID = "X-LC-Id";
  public static final String HEADER_KEY_LC_APPKEY = "X-LC-Key";
  private static final String HEADER_KEY_LC_PROD_MODE = "X-LC-Prod";
  public static final String HEADER_KEY_LC_SIGN = "X-LC-Sign";
  private static final String HEADER_KEY_ACCEPT = "Accept";
  private static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_KEY_USER_AGENT = "User-Agent";
  private static final String DEFAULT_CONTENT_TYPE = "application/json";

  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request originalRequest = chain.request();
    String sessionToken = null == AVUser.getCurrentUser()? "" : AVUser.getCurrentUser().getSessionToken();
    Request newRequest = originalRequest.newBuilder()
            .header(HEADER_KEY_LC_PROD_MODE, AVCloud.isProductionMode()?"1":"0")
            .header(HEADER_KEY_LC_APPID, AVOSCloud.getApplicationId())
            .header(HEADER_KEY_LC_SIGN, RequestSignImplementation.requestSign())
            .header(HEADER_KEY_ACCEPT, DEFAULT_CONTENT_TYPE)
            .header(HEADER_KEY_CONTENT_TYPE, DEFAULT_CONTENT_TYPE)
            .header(HEADER_KEY_USER_AGENT, AppConfiguration.getUserAgent())
            .header(HEADER_KEY_LC_SESSIONTOKEN, null == sessionToken ? "":sessionToken)
            .build();
    return chain.proceed(newRequest);
  }
}
