package cn.leancloud.service;

import cn.leancloud.core.*;
import cn.leancloud.im.v2.conversation.AVIMConversationMemberInfo;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
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
    OkHttpClient httpClient = PaasClient.getGlobalOkHttpClient();
    AppRouter appRouter = AppRouter.getInstance();
    appRouter.getEndpoint(AVOSCloud.getApplicationId(), AVOSService.API, false).subscribe(
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

  public Observable<List<AVIMConversationMemberInfo>> queryMemberInfo(Map<String, String> query, String rtmSessionToken) {
    return wrappObservable(service.queryMemberInfo(rtmSessionToken, query))
            .map(new Function<List<JSONObject>, List<AVIMConversationMemberInfo>>() {
              @Override
              public List<AVIMConversationMemberInfo> apply(List<JSONObject> objects) throws Exception {
                List<AVIMConversationMemberInfo> result = new LinkedList<AVIMConversationMemberInfo>();
                if (null != objects) {
                  for (JSONObject object: objects) {
                    AVIMConversationMemberInfo tmp = AVIMConversationMemberInfo.createInstance(object);
                    result.add(tmp);
                  }
                }
                return result;
              }
            });
  }
}
