package cn.leancloud.service;

import cn.leancloud.core.*;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.util.Map;

public class PushClient {
  private static PushClient instance = null;
  public static PushClient getInstance() {
    if (null == instance) {
      synchronized (PushClient.class) {
        if (null == instance) {
          instance = new PushClient();
        }
      }
    }
    return instance;
  }

  private PushService service = null;
  private boolean asynchronized = false;
  private AppConfiguration.SchedulerCreator defaultCreator = null;

  private PushClient() {
    this.asynchronized = AppConfiguration.isAsynchronized();
    this.defaultCreator = AppConfiguration.getDefaultScheduler();
    final OkHttpClient httpClient = PaasClient.getGlobalOkHttpClient();
    AppRouter appRouter = AppRouter.getInstance();
    appRouter.getEndpoint(AVOSCloud.getApplicationId(), AVOSService.PUSH, false).subscribe(
            new Consumer<String>() {
              @Override
              public void accept(String serverHost) throws Exception {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(serverHost)
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .client(httpClient)
                        .build();
                service = retrofit.create(PushService.class);
              }
            });
  }

  public Observable<JSONObject> sendPushRequest(Map<String, Object> param) {
    return wrappObservable(service.sendPushRequest(new JSONObject(param)));
  }

  private Observable wrappObservable(Observable observable) {
    if (null == observable) {
      return null;
    }
    if (asynchronized) {
      observable = observable.subscribeOn(Schedulers.io());
    }
    if (null != defaultCreator) {
      observable = observable.observeOn(defaultCreator.create());
    }
    return observable;
  }
}
