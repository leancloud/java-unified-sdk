package cn.leancloud.service;

import cn.leancloud.core.*;
import cn.leancloud.im.Signature;
import cn.leancloud.im.v2.conversation.AVIMConversationMemberInfo;
import cn.leancloud.utils.ErrorUtils;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RealtimeClient {
  private static RealtimeClient instance = null;
  public static RealtimeClient getInstance() {
    if (null == instance) {
      synchronized (RealtimeClient.class) {
        if (null == instance) {
          instance = new RealtimeClient();
        }
      }
    }
    return instance;
  }

  private RealtimeService service = null;
  private boolean asynchronized = false;
  private AppConfiguration.SchedulerCreator defaultCreator = null;

  private RealtimeClient() {
    this.asynchronized = AppConfiguration.isAsynchronized();
    this.defaultCreator = AppConfiguration.getDefaultScheduler();
    final OkHttpClient httpClient = PaasClient.getGlobalOkHttpClient();
    AppRouter appRouter = AppRouter.getInstance();
    appRouter.getEndpoint(AVOSCloud.getApplicationId(), AVOSService.API).subscribe(
            new Consumer<String>() {
              @Override
              public void accept(String apiHost) throws Exception {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(apiHost)
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .client(httpClient)
                        .build();
                service = retrofit.create(RealtimeService.class);
              }
            });
  }

  private Observable wrapObservable(Observable observable) {
    if (null == observable) {
      return null;
    }
    if (asynchronized) {
      observable = observable.subscribeOn(Schedulers.io());
    }
    if (null != defaultCreator) {
      observable = observable.observeOn(defaultCreator.create());
    }
    observable = observable.onErrorResumeNext(new Function<Throwable, ObservableSource>() {
      @Override
      public ObservableSource apply(Throwable throwable) throws Exception {
        return Observable.error(ErrorUtils.propagateException(throwable));
      }
    });
    return observable;
  }

  public Observable<Signature> createSignature(Map<String, Object> params) {
    return wrapObservable(service.createSignature(new JSONObject(params)));
  }

  public Observable<List<AVIMConversationMemberInfo>> queryMemberInfo(Map<String, String> query, String rtmSessionToken) {
    return wrapObservable(service.queryMemberInfo(rtmSessionToken, query))
            .map(new Function<Map<String, List<JSONObject>>, List<AVIMConversationMemberInfo>>() {
              @Override
              public List<AVIMConversationMemberInfo> apply(Map<String, List<JSONObject>> rawResult) throws Exception {
                List<JSONObject> objects = rawResult.get("results");
                List<AVIMConversationMemberInfo> result = new LinkedList<AVIMConversationMemberInfo>();
                for (JSONObject object: objects) {
                  AVIMConversationMemberInfo tmp = AVIMConversationMemberInfo.createInstance(object);
                  result.add(tmp);
                }
                return result;
              }
            });
  }

  public Observable<JSONObject> subscribeLiveQuery(Map<String, Object> params) {
    return wrapObservable(service.subscribe(new JSONObject(params)));
  }
  public Observable<JSONObject> unsubscribeLiveQuery(Map<String, Object> params) {
    return wrapObservable(service.unsubscribe(new JSONObject(params)));
  }
}
