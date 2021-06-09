package cn.leancloud.core;

import cn.leancloud.LCLogger;
import cn.leancloud.service.APIService;
import cn.leancloud.utils.LogUtil;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LoggingInterceptor implements Interceptor {
  private static final String CURL_COMMAND = "curl -X %s %n";
  private static final String CURL_HEADER_FORMAT = " -H %s: %s %n";
  private static LCLogger LOGGER = LogUtil.getLogger(LoggingInterceptor.class);

  private String generateCURLCommandString(Request request) {
    String url = request.url().toString();
    String method = request.method();
    Headers headers = request.headers();
    StringBuilder sb = new StringBuilder();
    sb.append(String.format(CURL_COMMAND, method));
    for (String name : headers.names()) {
      if (!LeanCloud.printAllHeaders) {
        if (RequestPaddingInterceptor.HEADER_KEY_LC_APPKEY.equals(name)) {
          sb.append(String.format(CURL_HEADER_FORMAT, name, "{your_app_key}"));
          continue;
        }

        if (APIService.HEADER_KEY_LC_SESSIONTOKEN.equals(name)) {
          sb.append(String.format(CURL_HEADER_FORMAT, name, "{your_session}"));
          continue;
        }

        if (RequestPaddingInterceptor.HEADER_KEY_LC_SIGN.equals(name)) {
          sb.append(String.format(CURL_HEADER_FORMAT, RequestPaddingInterceptor.HEADER_KEY_LC_SIGN, "{your_sign}"));
          continue;
        }
      }
      sb.append(String.format(CURL_HEADER_FORMAT, name, headers.get(name)));
    }

    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      BufferedSink sink = Okio.buffer(Okio.sink(os));
      RequestBody body = request.body();
      if (null != body) {
        body.writeTo(sink);
        sink.close();
        String bodyString = os.toString();
        sb.append(String.format("-d '%s' %n", bodyString));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    sb.append(url);
    return sb.toString();
  }

  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    Response response = chain.proceed(request);

    if (!LeanCloud.isDebugEnable()) {
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
