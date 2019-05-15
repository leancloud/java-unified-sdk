package cn.leancloud.push.lite.rest;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cn.leancloud.push.lite.AVOSCloud;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

public class LoggingInterceptor implements Interceptor {
  private static final String CURL_COMMAND = "curl -X %s \n";
  private static final String CURL_HEADER_FORMAT = " -H %s: %s \n";

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

      if (RequestPaddingInterceptor.HEADER_KEY_LC_SESSIONTOKEN.equals(name)) {
        sb.append(String.format(CURL_HEADER_FORMAT, name, "{your_session}"));
        continue;
      }
      if (RequestPaddingInterceptor.HEADER_KEY_LC_SIGN.equals(name)) {
        sb.append(String.format(CURL_HEADER_FORMAT, RequestPaddingInterceptor.HEADER_KEY_LC_APPKEY, "{your_app_key}"));
        continue;
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
        sb.append(String.format("-d '%s' \n", bodyString));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    sb.append(url);
    return sb.toString();
  }

  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    Response response = chain.proceed(request);

    if (!AVOSCloud.isDebugLogEnabled()) {
      return response;
    }

    Log.d("LeanCloud", String.format("Request: %s", generateCURLCommandString(request)));

    int responseCode = response.code();
    Headers responseHeaders = response.headers();
    String responseBody = response.body().string();

    Log.d("LeanCloud", String.format("Response: %d %n%s %n%s ", response.code(), responseHeaders, responseBody));

    return response.newBuilder()
        .code(responseCode)
        .headers(responseHeaders)
        .body(ResponseBody.create(response.body().contentType(), responseBody))
        .build();
  }
}
