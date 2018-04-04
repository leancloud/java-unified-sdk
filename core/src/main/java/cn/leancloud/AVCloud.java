package cn.leancloud;

import cn.leancloud.core.PaasClient;
import io.reactivex.Observable;

import java.util.Map;

public class AVCloud {
  public static <T> Observable<T> callFunctionInBackground(String name, Map<String, Object> params) {
    return PaasClient.getStorageClient().callFunction(name, params);
  }

  public static <T> Observable<T> callRPCInBackground(String name, Object param) {
    return PaasClient.getStorageClient().callRPC(name, param);
  }
}
