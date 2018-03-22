package cn.leancloud.network;

import cn.leancloud.AVLogger;
import cn.leancloud.core.AVFile;
import cn.leancloud.core.AVObject;
import cn.leancloud.core.AVUser;
import cn.leancloud.core.service.APIService;
import cn.leancloud.core.types.AVDate;
import cn.leancloud.upload.FileUploadToken;
import cn.leancloud.utils.LogUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;

public class StorageClient {
  private static AVLogger LOGGER = LogUtil.getLogger(StorageClient.class);

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

  public Observable<Void> deleteObject(final String className, String objectId) {
    return wrappObservable(apiService.deleteObject(className, objectId));
  }

  public Observable<? extends AVObject> createObject(final String className, JSONObject data) {
    Observable<? extends AVObject> object = wrappObservable(apiService.createObject(className, data));
    return object.map(new Function<AVObject, AVObject>() {
      public AVObject apply(AVObject avObject) {
        avObject.setClassName(className);
        return avObject;
      }
    });
  }

  public Observable<? extends AVObject> saveObject(final String className, String objectId, JSONObject data) {
    Observable<? extends AVObject> object = wrappObservable(apiService.updateObject(className, objectId, data));
    return object.map(new Function<AVObject, AVObject>() {
      public AVObject apply(AVObject avObject) {
        avObject.setClassName(className);
        return avObject;
      }
    });
  }

  public Observable<AVFile> fetchFile(String objectId) {
    Observable<AVFile> object = wrappObservable(apiService.fetchFile(objectId));
    return object.map(new Function<AVFile, AVFile>() {
      public AVFile apply(AVFile avFile) throws Exception {
        avFile.setClassName(AVFile.CLASS_NAME);
        return avFile;
      }
    });
  }

  public Observable<FileUploadToken> newUploadToken(JSONObject fileData) {
    return wrappObservable(apiService.createUploadToken(fileData));//.subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
  }

  public void fileCallback(JSONObject result) throws IOException {
    apiService.fileCallback(result).execute();
    return;
  }

  public Observable<Void> batchSave(JSONObject parameter) {
    Observable<Void> result = wrappObservable(apiService.batchSave(parameter));
    return result;
  }

  public Observable<AVUser> signUp(JSONObject data) {
    return wrappObservable(apiService.signup(data));
  }

  public <T extends AVUser> Observable<T> logIn(JSONObject data, final Class clazz) {
    Observable<JSONObject> object = wrappObservable(apiService.login(data));
    return object.map(new Function<JSONObject, T>() {
      public T apply(JSONObject object) throws Exception {
        LOGGER.d("convert JSONObject to target Class:" + clazz.getCanonicalName());
        T result = (T) JSON.parseObject(object.toJSONString(), clazz);
        LOGGER.d("result:" + result);
        return result;
      }
    });
  }
}
