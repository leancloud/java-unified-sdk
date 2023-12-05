package cn.leancloud.core;

import cn.leancloud.network.DNSDetoxicant;
import cn.leancloud.service.APIService;
import cn.leancloud.service.PushService;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

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
  private static PushService pushService = null;
  private static PushClient pushClient = null;

  public static OkHttpClient getGlobalOkHttpClient() {
    if (null == globalHttpClient) {
      globalHttpClient = new OkHttpClient.Builder()
              .connectTimeout(AppConfiguration.getNetworkTimeout(), TimeUnit.SECONDS)
              .readTimeout(AppConfiguration.getNetworkTimeout(), TimeUnit.SECONDS)
              .writeTimeout(AppConfiguration.getNetworkTimeout(), TimeUnit.SECONDS)
              .addInterceptor(new RequestPaddingInterceptor())
              .addInterceptor(new LoggingInterceptor())
              .dns(new DNSDetoxicant())
              .build();
    }
    return globalHttpClient;
  }

  static void initializeGlobalClient() {
    if (null == apiService) {
      AppRouter appRouter = AppRouter.getInstance();
      appRouter.getEndpoint(LeanCloud.getApplicationId(), LeanService.API).subscribe(
              new Consumer<String>() {
                @Override
                public void accept(String apiHost) throws Exception {
                  OkHttpClient okHttpClient = getGlobalOkHttpClient();
                  Retrofit retrofit = new Retrofit.Builder()
                          .baseUrl(apiHost)
                          .addConverterFactory(AppConfiguration.getRetrofitConverterFactory())
                          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                          .client(okHttpClient)
                          .build();
                  apiService = retrofit.create(APIService.class);
                  storageClient = new StorageClient(apiService, AppConfiguration.isAsynchronized(), AppConfiguration.getDefaultScheduler());
                }
              });
    }
  }

  public static StorageClient getStorageClient () {
    if (null == apiService) {
      OkHttpClient okHttpClient = getGlobalOkHttpClient();
      AppRouter appRouter = AppRouter.getInstance();
      String apiHost = appRouter.getEndpoint(LeanCloud.getApplicationId(), LeanService.API).blockingFirst();// donot block current thread.
      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl(apiHost)
              .addConverterFactory(AppConfiguration.getRetrofitConverterFactory())
              .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
              .client(okHttpClient)
              .build();
      apiService = retrofit.create(APIService.class);
      storageClient = new StorageClient(apiService, AppConfiguration.isAsynchronized(), AppConfiguration.getDefaultScheduler());
    }
    return storageClient;
  }

  public static PushClient getPushClient() {
    if (null == pushService) {
      OkHttpClient okHttpClient = getGlobalOkHttpClient();
      AppRouter appRouter = AppRouter.getInstance();
      String apiHost = appRouter.getEndpoint(LeanCloud.getApplicationId(), LeanService.PUSH).blockingFirst();// donot block current thread.
      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl(apiHost)
              .addConverterFactory(AppConfiguration.getRetrofitConverterFactory())
              .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
              .client(okHttpClient)
              .build();
      pushService = retrofit.create(PushService.class);
      pushClient = new PushClient(pushService, AppConfiguration.isAsynchronized(), AppConfiguration.getDefaultScheduler());
    }
    return pushClient;
  }

  // add method for unit tests in LeanCloudTest.java
  static void cleanup() {
	  apiService = null;
	  storageClient = null;
	  pushService = null;
	  pushClient = null;
	  globalHttpClient = null;
  }
}
