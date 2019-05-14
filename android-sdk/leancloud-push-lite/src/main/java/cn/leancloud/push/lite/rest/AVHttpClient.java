package cn.leancloud.push.lite.rest;

import com.alibaba.fastjson.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class AVHttpClient {
  /**
   * APP Router 默认地址
   */
  private static final String DEFAULT_APP_ROUTER = "https://app-router.leancloud.cn";

  private static Retrofit appRouterRetrofit = new Retrofit.Builder().baseUrl(DEFAULT_APP_ROUTER).build();
  private static AppRouterAPI appRouterAPI = appRouterRetrofit.create(AppRouterAPI.class);

  private Retrofit pushRouterRetrofit;
  private PushRouterRestAPI pushRouterAPI;

  private Retrofit pushAPIRetrofit;
  private PushRestAPI pushAPI;
  private volatile boolean initialized = false;

  private static AVHttpClient gInstance = new AVHttpClient();
  public static AVHttpClient getInstance() {
    return gInstance;
  }

  private AVHttpClient() {
    ;
  }

  public void initialize(String pushAPIServer, String pushRouterServer) {
    if (initialized) {
      return;
    }
    pushRouterRetrofit = new Retrofit.Builder().baseUrl(pushRouterServer).build();
    pushRouterAPI = pushRouterRetrofit.create(PushRouterRestAPI.class);

    pushAPIRetrofit = new Retrofit.Builder().baseUrl(pushAPIServer).build();
    pushAPI = pushAPIRetrofit.create(PushRestAPI.class);
    initialized = true;
  }

  public static void fetchAccessServers(String appId, Callback<JSONObject> callback) {
    Objects.requireNonNull(appId);
    Objects.requireNonNull(callback);
    Call<JSONObject> call = appRouterAPI.findServers(appId);
    call.enqueue(callback);
  }

  public void fetchPushWSServer(String appId, String installationId, int secure, Callback<JSONObject> callback) {
    Objects.requireNonNull(appId);
    Objects.requireNonNull(callback);
    Call<JSONObject> call = pushRouterAPI.getWSServer(appId, installationId, secure);
    call.enqueue(callback);
  }

  public void saveInstallation(JSONObject param, boolean fetchWhenSave, Callback<JSONObject> callback) {
    Objects.requireNonNull(param);
    Objects.requireNonNull(callback);
    Call<JSONObject> call = pushAPI.saveInstallation(param, fetchWhenSave);
    call.enqueue(callback);
  }

  public void findInstallation(String installationId, Callback<JSONObject> callback) {
    Objects.requireNonNull(installationId);
    Objects.requireNonNull(callback);
    Call<JSONObject> call = pushAPI.findInstallation(installationId);
    call.enqueue(callback);
  }
}
