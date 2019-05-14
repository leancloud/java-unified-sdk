package cn.leancloud.push.lite;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.leancloud.push.lite.rest.AVHttpClient;
import cn.leancloud.push.lite.utils.AVPersistenceUtils;
import cn.leancloud.push.lite.utils.StringUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PushRouterManager {

  /**
   * Push API 默认地址
   */
  private static final String DEFAULT_CN_EAST_PUSH_API_SERVER = "https://api.leancloud.cn";
  private static final String DEFAULT_CN_NORTH_PUSH_API_SERVER = "https://api.leancloud.cn";
  private static final String DEFAULT_US_PUSH_API_SERVER = "https://api.leancloud.cn";

  /**
   * push router 默认地址
   */
  private static final String DEFAULT_CN_EAST_ROUTER_SERVER = "https://router-q0-push.leancloud.cn";
  private static final String DEFAULT_CN_NORTH_ROUTER_SERVER = "https://router-g0-push.leancloud.cn";
  private static final String DEFAULT_US_ROUTER_SERVER = "https://router-a0-push.leancloud.cn";

  /**
   * share preference 的 key 值
   */
  private static final String API_SERVER_KEY = "api_server";
  private static final String RTM_ROUTER_SERVRE_KEY = "rtm_router_server";
  private static final String PUSH_SERVRE_KEY = "push_server";
  private static final String ENGINE_SERVRE_KEY = "engine_server";
  private static final String TTL_KEY = "ttl";

  private static final String LATEST_UPDATE_TIME_KEY = "latest_update_time";

  private Map<String, String> apiMaps = new ConcurrentHashMap<>();
  private static Map<String, String> customApiMaps = new ConcurrentHashMap<>();

  private static PushRouterManager pushRouterManager;

  public synchronized static PushRouterManager getInstance() {
    if (null == pushRouterManager) {
      pushRouterManager = new PushRouterManager();
    }
    return pushRouterManager;
  }

  private PushRouterManager() {
  }

  static void setServer(AVOSCloud.SERVER_TYPE server, String host) {
    customApiMaps.put(server.name, host);
  }

  public String getPushAPIServer() {
    String result = customApiMaps.getOrDefault(AVOSCloud.SERVER_TYPE.PUSH.name, apiMaps.getOrDefault(AVOSCloud.SERVER_TYPE.PUSH.name, ""));
    if (StringUtil.isEmpty(result)) {
      switch (AVOSCloud.getRegion()) {
        case EastChina:
          result = DEFAULT_CN_EAST_PUSH_API_SERVER;
          break;
        case NorthChina:
          result = DEFAULT_CN_NORTH_PUSH_API_SERVER;
          break;
        case NorthAmerica:
          result = DEFAULT_US_PUSH_API_SERVER;
          break;
      }
    }
    return result;
  }

  public String getPushRouterServer() {
    String result = customApiMaps.getOrDefault(AVOSCloud.SERVER_TYPE.RTM.name, apiMaps.getOrDefault(AVOSCloud.SERVER_TYPE.RTM.name, ""));
    if (StringUtil.isEmpty(result)) {
      switch (AVOSCloud.getRegion()) {
        case EastChina:
          result = DEFAULT_CN_EAST_ROUTER_SERVER;
          break;
        case NorthChina:
          result = DEFAULT_CN_NORTH_ROUTER_SERVER;
          break;
        case NorthAmerica:
          result = DEFAULT_US_ROUTER_SERVER;
          break;
      }
    }
    return result;
  }

  /**
   * 更新 router url
   * 有可能因为测试或者 301 等原因需要运行过程中修改 url
   *
   * @param router
   * @param persistence 是否需要持久化存储到本地
   *                    为 true 则存到本地，app 下次打开后仍有效果，否则仅当次声明周期内有效
   */
  public void updateRtmRouterServer(String router, boolean persistence) {
    apiMaps.put(AVOSCloud.SERVER_TYPE.RTM.name, addHttpsPrefix(router));
    if (persistence) {
      AVPersistenceUtils.sharedInstance().savePersistentSettingString(
          getAppRouterSPName(), RTM_ROUTER_SERVRE_KEY, apiMaps.get(AVOSCloud.SERVER_TYPE.RTM.name));
    }
  }

  /**
   * 拉取 router 地址
   *
   * @param force 是否强制拉取，如果为 true 则强制拉取，如果为 false 则需要间隔超过 ttl 才会拉取
   */
  void fetchRouter(boolean force) {
    fetchRouter(force, null);
  }

  /**
   * 添加此函数仅仅是为了测试时使用
   * @param force
   * @param callback
   */
  void fetchRouter(boolean force, final AVCallback callback) {
    updateServers();

    Long lastTime = AVPersistenceUtils.sharedInstance().getPersistentSettingLong(
        getAppRouterSPName(), LATEST_UPDATE_TIME_KEY, 0L);

    int ttl = AVPersistenceUtils.sharedInstance().getPersistentSettingInteger(
        getAppRouterSPName(), TTL_KEY, 0);

    if (force || System.currentTimeMillis() - lastTime > ttl * 1000 || apiMaps.size() < 1) {
      AVHttpClient.fetchAccessServers(AVOSCloud.applicationId, new Callback<JSONObject>() {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
          JSONObject result = response.body();
          saveRouterResult(result);
          if (null != callback) {
            callback.internalDone(null);
          }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t) {
          if (null != callback) {
            callback.internalDone(new AVException(t));
          }
        }
      });
    } else {
      if (null != callback) {
        callback.internalDone(null);
      }
    }
  }

  private void saveRouterResult(JSONObject response) {
    if (null != response) {
      updateMapAndSaveLocal(apiMaps, response, AVOSCloud.SERVER_TYPE.RTM.name, RTM_ROUTER_SERVRE_KEY);
      updateMapAndSaveLocal(apiMaps, response, AVOSCloud.SERVER_TYPE.PUSH.name, PUSH_SERVRE_KEY);
      updateMapAndSaveLocal(apiMaps, response, AVOSCloud.SERVER_TYPE.API.name, API_SERVER_KEY);
      updateMapAndSaveLocal(apiMaps, response, AVOSCloud.SERVER_TYPE.ENGINE.name, ENGINE_SERVRE_KEY);

      if (response.containsKey(TTL_KEY)) {
        AVPersistenceUtils.sharedInstance().savePersistentSettingInteger(
            getAppRouterSPName(), TTL_KEY, response.getIntValue(TTL_KEY));
      }

      AVPersistenceUtils.sharedInstance().savePersistentSettingLong(
          getAppRouterSPName(), LATEST_UPDATE_TIME_KEY, System.currentTimeMillis());
    }
  }

  private void updateMapAndSaveLocal(Map<String, String> maps, JSONObject jsonObject, String mapKey, String jsonKey) {
    if (jsonObject.containsKey(jsonKey)) {
      String value = addHttpsPrefix(jsonObject.getString(jsonKey));
      AVPersistenceUtils.sharedInstance().savePersistentSettingString(
          getAppRouterSPName(), jsonKey, value);
      if (!StringUtil.isEmpty(value)) {
        maps.put(mapKey, value);
      }
    }
  }

  private void refreshMap(Map<String, String> maps, String mapKey, String spKey) {
    String value = AVPersistenceUtils.sharedInstance().getPersistentSettingString(
        getAppRouterSPName(), spKey, "");
    if (!StringUtil.isEmpty(value)) {
      maps.put(mapKey, value);
    }
  }

  private String getAppRouterSPName() {
    return "com.avos.avoscloud.approuter." + AVOSCloud.applicationId;
  }

  /**
   * 根据当前 appId 更新 shareprefenence 的 name
   * 这样如果运行过程中动态切换了 appId，app router 仍然可以正常 work
   */
  private void updateServers() {
    refreshMap(apiMaps, AVOSCloud.SERVER_TYPE.RTM.name, RTM_ROUTER_SERVRE_KEY);
    refreshMap(apiMaps, AVOSCloud.SERVER_TYPE.PUSH.name, PUSH_SERVRE_KEY);
    refreshMap(apiMaps, AVOSCloud.SERVER_TYPE.API.name, API_SERVER_KEY);
    refreshMap(apiMaps, AVOSCloud.SERVER_TYPE.ENGINE.name, ENGINE_SERVRE_KEY);
  }

  /**
   * 添加 https 前缀
   * 主要是因为 server 部分 url 返回数据不一致，有的有前缀，有的没有
   *
   * @param url
   * @return
   */
  private String addHttpsPrefix(String url) {
    if (!StringUtil.isEmpty(url) && !url.startsWith("http")) {
      return "https://" + url;
    }
    return url;
  }
}
