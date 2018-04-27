package cn.leancloud.core;

import cn.leancloud.AVACL;
import cn.leancloud.network.DNSDetoxicant;
import cn.leancloud.service.APIService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.*;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import cn.leancloud.core.AppConfiguration.SchedulerCreator;

import java.util.concurrent.TimeUnit;

/**
 * we must config following variables:
 * 1. async request or not
 * 2. SchedulerCreator.
 * 3. default ACL
 */
public class PaasClient {
  private static APIService apiService = null;
  private static StorageClient storageClient = null;
  private static OkHttpClient globalHttpClient = null;
  static SchedulerCreator defaultScheduler = null;
  static boolean asynchronized = false;

  /**
   * configure run-time env.
   *
   * @param asyncRequest
   * @param observerSchedulerCreator
   */
  public static void config(boolean asyncRequest, SchedulerCreator observerSchedulerCreator) {
    asynchronized = asyncRequest;
    defaultScheduler = observerSchedulerCreator;
  }

  public static OkHttpClient getGlobalOkHttpClient() {
    if (null == globalHttpClient) {
      globalHttpClient = new OkHttpClient.Builder()
              .connectTimeout(15, TimeUnit.SECONDS)
              .readTimeout(10, TimeUnit.SECONDS)
              .writeTimeout(10, TimeUnit.SECONDS)
              .addInterceptor(new RequestPaddingInterceptor())
              .addInterceptor(new LoggingInterceptor())
              .dns(new DNSDetoxicant())
              .build();
    }
    return globalHttpClient;
  }

  public static StorageClient getStorageClient () {
    if (null == apiService) {
      OkHttpClient okHttpClient = getGlobalOkHttpClient();
      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl("https://api.leancloud.cn")
              .addConverterFactory(FastJsonConverterFactory.create())
              .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
              .client(okHttpClient)
              .build();
      apiService = retrofit.create(APIService.class);
      storageClient = new StorageClient(apiService, asynchronized, defaultScheduler);
    }
    return storageClient;
  }
}