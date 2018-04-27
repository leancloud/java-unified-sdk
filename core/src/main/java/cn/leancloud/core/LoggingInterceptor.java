package cn.leancloud.core;

import cn.leancloud.AVLogger;
import cn.leancloud.utils.LogUtil;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class LoggingInterceptor implements Interceptor {
  private static AVLogger LOGGER = LogUtil.getLogger(LoggingInterceptor.class);

  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    long t1 = System.nanoTime();

    System.out.print(String.format("[LoggingInterceptor] [Thread:%d] Sending request(%s) %s %n%s%n",Thread.currentThread().getId(),
                request.method(), request.url(), request.headers()));

    Response response = chain.proceed(request);

    long t2 = System.nanoTime();
    System.out.print(String.format("[LoggingInterceptor] Received response for %s in %.1fms%nStatusCode: %d%n",
                response.request().url(), (t2 - t1) / 1e6d, response.code()));

    return response;
  }
}