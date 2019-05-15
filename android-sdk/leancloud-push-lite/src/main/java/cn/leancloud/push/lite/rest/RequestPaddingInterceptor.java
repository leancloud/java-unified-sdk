package cn.leancloud.push.lite.rest;

import java.io.IOException;

import cn.leancloud.push.lite.AVOSCloud;
import cn.leancloud.push.lite.utils.StringUtil;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RequestPaddingInterceptor implements Interceptor {

  public static final String HEADER_KEY_LC_SESSIONTOKEN = "X-LC-Session";
  public static final String HEADER_KEY_LC_APPID = "X-LC-Id";
  public static final String HEADER_KEY_LC_APPKEY = "X-LC-Key";
  public static final String HEADER_KEY_LC_SIGN = "X-LC-Sign";
  private static final String HEADER_KEY_ACCEPT = "Accept";
  private static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_KEY_USER_AGENT = "User-Agent";
  private static final String DEFAULT_CONTENT_TYPE = "application/json";

  public static String requestSign() {
    return requestSign(System.currentTimeMillis(), false);
  }

  public static String requestSign(long ts, boolean useMasterKey) {
    String appKey = AVOSCloud.clientKey;
    StringBuilder builder = new StringBuilder();

    StringBuilder result = new StringBuilder();

    result.append(StringUtil.computeMD5(builder.append(ts).append(appKey).toString()).toLowerCase());
    result.append(',').append(ts);
    if (useMasterKey) {
      result.append(",master");
    }
    return result.toString();
  }

  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request originalRequest = chain.request();
    Request newRequest = originalRequest.newBuilder()
        .header(HEADER_KEY_LC_APPID, AVOSCloud.getApplicationId())
        .header(HEADER_KEY_LC_SIGN, requestSign())
        .header(HEADER_KEY_ACCEPT, DEFAULT_CONTENT_TYPE)
        .header(HEADER_KEY_CONTENT_TYPE, DEFAULT_CONTENT_TYPE)
        .header(HEADER_KEY_USER_AGENT, AVOSCloud.getUserAgent())
        .build();
    return chain.proceed(newRequest);
  }
}
