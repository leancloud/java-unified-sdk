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
  public StorageClient(APIService apiService) {
    this.apiService = apiService;
  }

  public Observable<AVDate> getServerTime() {
    return apiService.currentTimeMillis().observeOn(PaasClient.defaultScheduler.create());
  }

  public Observable<AVObject> fetchObject(final String className, String objectId) {
    return apiService.fetchObject(className, objectId)
            .subscribeOn(Schedulers.io())
            .observeOn(PaasClient.defaultScheduler.create())
            .map(new Function<AVObject, AVObject>() {
              public AVObject apply(AVObject avObject) throws Exception {
                avObject.setClassName(className);
                return avObject;
              }
            });
  }

  public Observable<FileUploadToken> newUploadToken() {
    return apiService.createUploadToken()
            .subscribeOn(Schedulers.io())
            .observeOn(PaasClient.defaultScheduler.create());
  }

  public Observable<Void> batchSave(JSONObject parameter) {
    return apiService.batchSave(parameter)
            .subscribeOn(Schedulers.io())
            .observeOn(PaasClient.defaultScheduler.create());
  }
}
