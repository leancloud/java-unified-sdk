package cn.leancloud.network;

import cn.leancloud.core.AVObject;
import cn.leancloud.core.service.APIService;
import cn.leancloud.core.types.AVDate;
import cn.leancloud.internal.FileUploadToken;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;

public class StorageClient {
  private APIService apiService = null;
  public StorageClient(APIService apiService) {
    this.apiService = apiService;
  }

  public Observable<AVDate> getServerTime() {
    return apiService.currentTimeMillis().observeOn(Schedulers.single());
  }

  public Observable<AVObject> fetchObject(String className, String objectId) {
    return apiService.fetchObject(className, objectId).subscribeOn(Schedulers.io()).observeOn(Schedulers.single());
  }

  public Observable<FileUploadToken> newUploadToken() {
    return apiService.createUploadToken().subscribeOn(Schedulers.io()).observeOn(Schedulers.single());
  }
}
