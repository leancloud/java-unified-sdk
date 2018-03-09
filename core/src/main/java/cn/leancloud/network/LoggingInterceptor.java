package cn.leancloud.network;

import cn.leancloud.AVLogger;
import cn.leancloud.utils.LogUtil;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class LoggingInterceptor implements Interceptor {
  private static AVLogger logger = LogUtil.getLogger(LoggingInterceptor.class);

  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    long t1 = System.nanoTime();

    System.out.print(String.format("Sending request(%s) %s %n%s%n",
                request.method(), request.url(), request.headers()));

    Response response = chain.proceed(request);

    long t2 = System.nanoTime();
    System.out.print(String.format("Received response for %s in %.1fms%nStatusCode: %d%n",
                response.request().url(), (t2 - t1) / 1e6d, response.code()));

    return response;
  }
}
