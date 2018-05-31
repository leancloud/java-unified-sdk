package cn.leancloud.core;

import cn.leancloud.AVLogger;
import cn.leancloud.utils.LogUtil;
import okhttp3.*;

import java.io.IOException;

public class LoggingInterceptor implements Interceptor {
  private static final String CURL_COMMAND = "curl -X %s \n";
  private static final String CURL_HEADER_FORMAT = " -H %s: %s \n";
  private static AVLogger LOGGER = LogUtil.getLogger(LoggingInterceptor.class);

  private String generateCURLCommandString(Request request) {
    String url = request.url().toString();
    String method = request.method();
    Headers headers = request.headers();
    StringBuilder sb = new StringBuilder();
    sb.append(String.format(CURL_COMMAND, method));
    for (String name : headers.names()) {
      if (RequestPaddingInterceptor.HEADER_KEY_LC_APPID.equals(name)) {
        sb.append(String.format(CURL_HEADER_FORMAT, name, "{your_app_id}"));
        continue;
      }
      if (RequestPaddingInterceptor.HEADER_KEY_LC_APPKEY.equals(name)) {
        sb.append(String.format(CURL_HEADER_FORMAT, name, "{your_app_key}"));
        continue;
      }
      if (RequestPaddingInterceptor.HEADER_KEY_LC_SIGN.equals(name)) {
        sb.append(String.format(CURL_HEADER_FORMAT, RequestPaddingInterceptor.HEADER_KEY_LC_APPKEY, "{your_app_key}"));
        continue;
      }
      sb.append(String.format(CURL_HEADER_FORMAT, name, headers.get(name)));
    }
    sb.append(url);
    return sb.toString();
  }

  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    Response response = chain.proceed(request);

    if (!AVOSCloud.isDebugEnable()) {
      return response;
    }

    LOGGER.d(String.format("Request: %s", generateCURLCommandString(request)));

    int responseCode = response.code();
    Headers responseHeaders = response.headers();
    String responseBody = response.body().string();

    LOGGER.d(String.format("Response: %d %n%s %n%s ", response.code(), responseHeaders, responseBody));

    return response.newBuilder()
            .code(responseCode)
            .headers(responseHeaders)
            .body(ResponseBody.create(response.body().contentType(), responseBody))
            .build();
  }
}
