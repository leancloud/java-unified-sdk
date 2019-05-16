package cn.leancloud.push.lite.rest;

import com.alibaba.fastjson.JSONObject;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cn.leancloud.push.lite.PushRouterManager;
import cn.leancloud.push.lite.utils.StringUtil;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

public class AVHttpClient {
  /**
   * APP Router 默认地址
   */
  private static final String DEFAULT_APP_ROUTER = "https://app-router.leancloud.cn/";

  private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS)
      .addInterceptor(new LoggingInterceptor())
      .addInterceptor(new RequestPaddingInterceptor())
      .build();

  private static Retrofit appRouterRetrofit = new Retrofit.Builder().baseUrl(DEFAULT_APP_ROUTER)
      .addConverterFactory(FastJsonConverterFactory.create())
      .client(okHttpClient).build();
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

  private String currentPushAPIServer = "";
  private String currentPushRouterServer = "";



  private AVHttpClient() {
  }

  /**
   * initialize push api and router server.
   * this method is called by PushRouterManager after calling app router.
   *
   * @param pushAPIServer
   * @param pushRouterServer
   */
  public synchronized void initialize(String pushAPIServer, String pushRouterServer) {
    if (initialized) {
      return;
    }

    createRetrofitClient(pushAPIServer, pushRouterServer);
    initialized = true;
  }

  private void createRetrofitClient(String pushAPIServer, String pushRouterServer) {
    if (!StringUtil.isEmpty(pushAPIServer) && !currentPushAPIServer.equals(pushAPIServer)) {
      pushRouterRetrofit = new Retrofit.Builder().baseUrl(pushRouterServer)
          .addConverterFactory(FastJsonConverterFactory.create())
          .client(okHttpClient)
          .build();
      pushRouterAPI = pushRouterRetrofit.create(PushRouterRestAPI.class);
      currentPushAPIServer = pushAPIServer;
    }

    if (!StringUtil.isEmpty(pushRouterServer) && !currentPushRouterServer.equals(pushRouterServer)) {
      pushAPIRetrofit = new Retrofit.Builder().baseUrl(pushAPIServer)
          .addConverterFactory(FastJsonConverterFactory.create())
          .client(okHttpClient)
          .build();
      pushAPI = pushAPIRetrofit.create(PushRestAPI.class);
      currentPushRouterServer = pushRouterServer;
    }
  }

  private synchronized void makeSurePushRetrofit() {
    if (initialized) {
      return;
    }
    String pushAPIServer = PushRouterManager.getInstance().getPushAPIServer();
    String pushRouterServer = PushRouterManager.getInstance().getPushRouterServer();
    createRetrofitClient(pushAPIServer, pushRouterServer);
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
    makeSurePushRetrofit();
    Call<JSONObject> call = pushRouterAPI.getWSServer(appId, installationId, secure);
    call.enqueue(callback);
  }

  public void saveInstallation(JSONObject param, boolean fetchWhenSave, Callback<JSONObject> callback) {
    Objects.requireNonNull(param);
    Objects.requireNonNull(callback);
    makeSurePushRetrofit();
    Call<JSONObject> call = pushAPI.saveInstallation(param, fetchWhenSave);
    call.enqueue(callback);
  }

  public void findInstallation(String installationId, Callback<JSONObject> callback) {
    Objects.requireNonNull(installationId);
    Objects.requireNonNull(callback);
    makeSurePushRetrofit();
    Call<JSONObject> call = pushAPI.findInstallation(installationId);
    call.enqueue(callback);
  }
}
