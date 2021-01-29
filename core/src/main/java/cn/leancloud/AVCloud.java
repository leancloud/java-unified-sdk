package cn.leancloud;

import cn.leancloud.core.PaasClient;
import cn.leancloud.ops.Utils;
import io.reactivex.Observable;

import java.util.Map;

public class AVCloud {
  private static boolean isProduction = true;
  /**
   * 设置调用云代码函数的测试环境或者生产环境，默认为true，也就是生产环境。
   * @param productionMode flag to production mode.
   */
  public static void setProductionMode(boolean productionMode) {
    isProduction = productionMode;
  }

  /**
   * Whether current mode is production or not.
   * @return flag to production mode.
   */
  public static boolean isProductionMode() {return isProduction;}

  /**
   * Call Cloud Function in Background.
   * @param name function name.
   * @param params invoke parameters.
   * @param <T> template type.
   * @return observable instance.
   */
  public static <T> Observable<T> callFunctionInBackground(String name, Map<String, Object> params) {
    return callFunctionInBackground(null, name, params);
  }

  /**
   * Call Cloud Function in Background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param name function name.
   * @param params invoke parameters.
   * @param <T> template type.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public static <T> Observable<T> callFunctionInBackground(AVUser asAuthenticatedUser,
                                                           String name, Map<String, Object> params) {
    return PaasClient.getStorageClient().callFunction(asAuthenticatedUser, name, Utils.getParsedMap(params));
  }

  /**
   * call cloud funtion with cache policy.
   * @param name function name.
   * @param params parameters.
   * @param cachePolicy cache policy same as AVQuery.
   * @param maxCacheAge max age in milliseconds.
   * @param <T> template type of result.
   * @return observable instance.
   */
  public static <T> Observable<T> callFunctionWithCacheInBackground(String name, Map<String, Object> params,
                                                                    AVQuery.CachePolicy cachePolicy, long maxCacheAge) {
    return callFunctionWithCacheInBackground(null, name, params, cachePolicy, maxCacheAge);
  }

  /**
   * call cloud funtion with cache policy.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param name function name.
   * @param params parameters.
   * @param cachePolicy cache policy same as AVQuery.
   * @param maxCacheAge max age in milliseconds.
   * @param <T> template type of result.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public static <T> Observable<T> callFunctionWithCacheInBackground(AVUser asAuthenticatedUser,
                                                                    String name, Map<String, Object> params,
                                                                    AVQuery.CachePolicy cachePolicy, long maxCacheAge) {
    return PaasClient.getStorageClient().callFunctionWithCachePolicy(asAuthenticatedUser, name,
            Utils.getParsedMap(params), cachePolicy, maxCacheAge);
  }

  /**
   * Call Cloud RPC Function in Background.
   * @param name function name.
   * @param params invoke parameters.
   * @param <T> template type.
   * @return observable instance.
   */
  public static <T> Observable<T> callRPCInBackground(String name, Object params) {
    return callRPCInBackground(null, name, params);
  }

  /**
   * Call Cloud RPC Function in Background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param name function name.
   * @param params invoke parameters.
   * @param <T> template type.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public static <T> Observable<T> callRPCInBackground(AVUser asAuthenticatedUser, String name, Object params) {
    return PaasClient.getStorageClient().callRPC(asAuthenticatedUser, name, Utils.getParsedObject(params));
  }

  /**
   * Call Cloud RPC Function with cache policy in Background.
   * @param name function name.
   * @param params invoke parameters.
   * @param cachePolicy cache policy same as AVQuery
   * @param maxCacheAge max cache age in milliseconds.
   * @param <T>template type.
   * @return observable instance.
   */
  public static <T> Observable<T> callRPCWithCacheInBackground(String name, Map<String, Object> params,
                                                               AVQuery.CachePolicy cachePolicy, long maxCacheAge) {
    return callRPCWithCacheInBackground(null, name, params, cachePolicy, maxCacheAge);
  }

  /**
   * Call Cloud RPC Function with cache policy in Background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param name function name.
   * @param params invoke parameters.
   * @param cachePolicy cache policy same as AVQuery
   * @param maxCacheAge max cache age in milliseconds.
   * @param <T>template type.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public static <T> Observable<T> callRPCWithCacheInBackground(AVUser asAuthenticatedUser,
                                                               String name, Map<String, Object> params,
                                                               AVQuery.CachePolicy cachePolicy, long maxCacheAge){
    return PaasClient.getStorageClient().callRPCWithCachePolicy(asAuthenticatedUser,name,
            Utils.getParsedMap(params), cachePolicy, maxCacheAge);
  }

  private AVCloud() {
  }
}
