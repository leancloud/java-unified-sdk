package cn.leancloud;

import cn.leancloud.core.PaasClient;
import io.reactivex.Observable;

import java.util.Map;

public class AVCloud {
  private static boolean isProduction = true;
  /**
   * 设置调用云代码函数的测试环境或者生产环境，默认为true，也就是生产环境。
   *
   * @param productionMode
   */
  public static void setProductionMode(boolean productionMode) {
    isProduction = productionMode;
  }
  public static boolean isProductionMode() {return isProduction;}

  public static <T> Observable<T> callFunctionInBackground(String name, Map<String, Object> params) {
    return PaasClient.getStorageClient().callFunction(name, params);
  }

  public static <T> Observable<T> callRPCInBackground(String name, Object param) {
    return PaasClient.getStorageClient().callRPC(name, param);
  }

  private AVCloud() {
    ;
  }
}
