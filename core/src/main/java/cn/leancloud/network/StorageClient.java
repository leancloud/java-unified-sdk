package cn.leancloud.network;

import cn.leancloud.core.AVObject;
import cn.leancloud.core.service.APIService;
import cn.leancloud.core.types.AVDate;
import cn.leancloud.internal.FileUploadToken;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;

public class StorageClient {
  private APIService apiService = null;
  private boolean asynchronized = false;
  private PaasClient.SchedulerCreator defaultCreator = null;
  public StorageClient(APIService apiService, boolean asyncRequest, PaasClient.SchedulerCreator observerSchedulerCreator) {
    this.apiService = apiService;
    this.asynchronized = asyncRequest;
    this.defaultCreator = observerSchedulerCreator;
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

  public Observable<AVDate> getServerTime() {
    Observable<AVDate> date = wrappObservable(apiService.currentTimeMillis());
    return date;
  }

  public Observable<AVObject> fetchObject(final String className, String objectId) {
    Observable<AVObject> object = wrappObservable(apiService.fetchObject(className, objectId));
    return object.map(new Function<AVObject, AVObject>() {
              public AVObject apply(AVObject avObject) throws Exception {
                avObject.setClassName(className);
                return avObject;
              }
            });
  }

  public Observable<FileUploadToken> newUploadToken() {
    Observable<FileUploadToken> token = wrappObservable(apiService.createUploadToken());
    return token;
  }

  public Observable<Void> batchSave(JSONObject parameter) {
    Observable<Void> result = wrappObservable(apiService.batchSave(parameter));
    return result;
  }
}
