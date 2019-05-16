package cn.leancloud.push.lite;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.leancloud.push.lite.rest.AVHttpClient;
import cn.leancloud.push.lite.utils.AVPersistenceUtils;
import cn.leancloud.push.lite.utils.StringUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PushRouterManager {
  private static final String TAG = PushRouterManager.class.getSimpleName();

  /**
   * Push API 默认地址
   */
  private static final String CN_EAST_PUSH_API_SERVER_FORMAT = "https://%s.push.lncldapi.com";
  private static final String CN_NOTRH_PUSH_API_SERVER_FORMAT = "https://%s.push.lncld.net";
  private static final String US_PUSH_API_SERVER_FORMAT = "https://%s.push.lncldglobal.com";

  /**
   * push router 默认地址
   */
  private static final String CN_EAST_PUSH_ROUTER_SERVER_FORMAT = "https://%s.rtm.lncldapi.com";
  private static final String CN_NOTRH_PUSH_ROUTER_SERVER_FORMAT = "https://%s.rtm.lncld.net";
  private static final String US_PUSH_ROUTER_SERVER_FORMAT = "https://%s.rtm.lncldglobal.com";

  /**
   * share preference 的 key 值
   */
  private static final String RTM_ROUTER_SERVRE_KEY = "rtm_router_server";
  private static final String PUSH_SERVRE_KEY = "push_server";
  private static final String TTL_KEY = "ttl";
  private static final String LATEST_UPDATE_TIME_KEY = "latest_update_time";

  private Map<String, String> apiMaps = new ConcurrentHashMap<>();
  private static Map<String, String> customApiMaps = new ConcurrentHashMap<>();

  private static PushRouterManager pushRouterManager;

  public static PushRouterManager getInstance() {
    if (null == pushRouterManager) {
      pushRouterManager = new PushRouterManager();
    }
    return pushRouterManager;
  }

  private PushRouterManager() {
    String appidPrefix = AVPersistenceUtils.getCurrentAppPrefix();
    String defaultPushServer = "";
    String defaultPushRouterServer = "";
    if (StringUtil.isEmpty(appidPrefix)) {
      Log.w(TAG, "invalid appId. AVOSCloud#initialize should be invoke at first!!");
    } else if (AVOSCloud.getRegion() == AVOSCloud.REGION.NorthAmerica) {
      defaultPushRouterServer = String.format(US_PUSH_ROUTER_SERVER_FORMAT, appidPrefix);
      defaultPushServer = String.format(US_PUSH_API_SERVER_FORMAT, appidPrefix);
    } else if (AVOSCloud.getRegion() == AVOSCloud.REGION.NorthChina) {
      defaultPushRouterServer = String.format(CN_NOTRH_PUSH_ROUTER_SERVER_FORMAT, appidPrefix);
      defaultPushServer = String.format(CN_NOTRH_PUSH_API_SERVER_FORMAT, appidPrefix);
    } else if (AVOSCloud.getRegion() == AVOSCloud.REGION.EastChina) {
      defaultPushRouterServer = String.format(CN_EAST_PUSH_ROUTER_SERVER_FORMAT, appidPrefix);
      defaultPushServer = String.format(CN_EAST_PUSH_API_SERVER_FORMAT, appidPrefix);
    } else {
      Log.w(TAG, "invalid REGION:" + AVOSCloud.getRegion());
    }
    if (!StringUtil.isEmpty(defaultPushServer)) {
      apiMaps.put(AVOSCloud.SERVER_TYPE.PUSH.name, defaultPushServer);
    }
    if (!StringUtil.isEmpty(defaultPushRouterServer)) {
      apiMaps.put(AVOSCloud.SERVER_TYPE.RTM.name, defaultPushRouterServer);
    }
  }

  static void setServer(AVOSCloud.SERVER_TYPE server, String host) {
    customApiMaps.put(server.name, host);
  }

  public String getPushAPIServer() {
    String result = customApiMaps.get(AVOSCloud.SERVER_TYPE.PUSH.name);
    if (StringUtil.isEmpty(result)) {
      result = apiMaps.get(AVOSCloud.SERVER_TYPE.PUSH.name);
    }
    return result;
  }

  public String getPushRouterServer() {
    String result = customApiMaps.get(AVOSCloud.SERVER_TYPE.RTM.name);
    if (StringUtil.isEmpty(result)) {
      result = apiMaps.get(AVOSCloud.SERVER_TYPE.RTM.name);
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
  void fetchRouter(boolean force, final AVCallback<Void> callback) {
    if (force || apiMaps.size() < 1) {
      String keyZone = getAppRouterSPName();
      AVPersistenceUtils persistenceUtils = AVPersistenceUtils.sharedInstance();
      String routerServer = persistenceUtils.getPersistentSettingString(keyZone, RTM_ROUTER_SERVRE_KEY, "");
      if (!StringUtil.isEmpty(routerServer)) {
        apiMaps.put(AVOSCloud.SERVER_TYPE.RTM.name, routerServer);
      }
      String pushServer = persistenceUtils.getPersistentSettingString(keyZone, PUSH_SERVRE_KEY, "");
      if (!StringUtil.isEmpty(pushServer)) {
        apiMaps.put(AVOSCloud.SERVER_TYPE.PUSH.name, pushServer);
      }
      Long lastTime = persistenceUtils.getPersistentSettingLong(keyZone, LATEST_UPDATE_TIME_KEY, 0L);
      int ttl = persistenceUtils.getPersistentSettingInteger(keyZone, TTL_KEY, 0);

      if (force || System.currentTimeMillis() - lastTime > ttl * 1000) {
        if (AVOSCloud.isDebugLogEnabled()) {
          Log.d(TAG, "begin to fetch app router.");
        }
        AVHttpClient.fetchAccessServers(AVOSCloud.applicationId, new Callback<JSONObject>() {
          @Override
          public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
            JSONObject result = response.body();
            saveRouterResult(result);
            if (AVOSCloud.isDebugLogEnabled()) {
              Log.d(TAG, "fetch app router result: " + result.toJSONString());
            }
            if (null != callback) {
              callback.internalDone(null);
            }
          }

          @Override
          public void onFailure(Call<JSONObject> call, Throwable t) {
            Log.w(TAG, "failed to fetch app router. cause: ", t);
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
    } else {
      if (null != callback) {
        callback.internalDone(null);
      }
    }
  }

  private void saveRouterResult(JSONObject response) {
    if (null != response) {
      AVPersistenceUtils persistenceUtils = AVPersistenceUtils.sharedInstance();
      updateMapAndSaveLocal(persistenceUtils, apiMaps, response, AVOSCloud.SERVER_TYPE.RTM.name, RTM_ROUTER_SERVRE_KEY);
      updateMapAndSaveLocal(persistenceUtils, apiMaps, response, AVOSCloud.SERVER_TYPE.PUSH.name, PUSH_SERVRE_KEY);

      if (response.containsKey(TTL_KEY)) {
        persistenceUtils.savePersistentSettingInteger(
            getAppRouterSPName(), TTL_KEY, response.getIntValue(TTL_KEY));
      }

      persistenceUtils.savePersistentSettingLong(
          getAppRouterSPName(), LATEST_UPDATE_TIME_KEY, System.currentTimeMillis());
    }
  }

  private void updateMapAndSaveLocal(AVPersistenceUtils persistenceUtils, Map<String, String> maps,
                                     JSONObject jsonObject, String mapKey, String jsonKey) {
    if (jsonObject.containsKey(jsonKey)) {
      String value = addHttpsPrefix(jsonObject.getString(jsonKey));
      persistenceUtils.savePersistentSettingString(getAppRouterSPName(), jsonKey, value);
      if (!StringUtil.isEmpty(value)) {
        maps.put(mapKey, value);
      }
    }
  }

  private String getAppRouterSPName() {
    return "com.avos.avoscloud.approuter." + AVOSCloud.applicationId;
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
