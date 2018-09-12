package cn.leancloud.core;

import cn.leancloud.service.PushService;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

import java.util.Map;

public class PushClient {
  private PushService service = null;
  private boolean asynchronized = false;
  private AppConfiguration.SchedulerCreator defaultCreator = null;

  public PushClient(PushService service, boolean asyncRequest, AppConfiguration.SchedulerCreator observerSchedulerCreator) {
    this.service = service;
    this.asynchronized = AppConfiguration.isAsynchronized();
    this.defaultCreator = AppConfiguration.getDefaultScheduler();
    final OkHttpClient httpClient = PaasClient.getGlobalOkHttpClient();
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
