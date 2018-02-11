package cn.leancloud.network;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class LoggingInterceptor implements Interceptor {
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    long t1 = System.nanoTime();
    System.out.println(String.format("Sending request %s %n%s",
                request.url(), request.headers()));

    Response response = chain.proceed(request);

//    long t2 = System.nanoTime();
//    System.out.println(String.format("Received response for %s in %.1fms%nStatusCode: %d%nResponse: %s",
//                response.request().url(), (t2 - t1) / 1e6d, response.code(), response.body().string()));

    return response;
  }
}
