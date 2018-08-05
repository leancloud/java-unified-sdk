package cn.leancloud.push;

import cn.leancloud.AVLogger;
import cn.leancloud.cache.SystemSetting;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AVNotificationManager {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVNotificationManager.class);

  private static final String PUSH_INTENT_KEY = "com.avoscloud.push";
  private static final String PUSH_MESSAGE_DEPOT = "com.avos.push.message";
  private static final String LOGTAG = "AVNotificationManager";
  private static final String AV_PUSH_SERVICE_APP_DATA = "AV_PUSH_SERVICE_APP_DATA";
  private static final String ICON_KEY = "_notification_icon";

  private static final Random random = new Random();
  private final ConcurrentHashMap<String, String> defaultPushCallback =
          new ConcurrentHashMap<String, String>();
  private int notificationIcon;

  private static AVNotificationManager notificationManager;

  public synchronized static AVNotificationManager getInstance() {
    if (null == notificationManager) {
      notificationManager = new AVNotificationManager();
    }
    return notificationManager;
  }

  private AVNotificationManager() {
    readDataFromCache();
  }

  private void readDataFromCache() {
    SystemSetting setting = AppConfiguration.getDefaultSetting();
    for (Map.Entry entry : setting.getAll(AV_PUSH_SERVICE_APP_DATA).entrySet()) {
      String channel = (String) entry.getKey();
      if (channel.equals(ICON_KEY)) {
        try {
          notificationIcon = Integer.valueOf((String) entry.getValue());
        } catch (Exception e) {
          // ignore;
        }
      } else {
        String defaultCls = String.valueOf(entry.getValue());
        defaultPushCallback.put(channel, defaultCls);
      }
    }
  }

  private int getNotificationIcon() {
    return notificationIcon;
  }

  void setNotificationIcon(int icon) {
    notificationIcon = icon;
    SystemSetting setting = AppConfiguration.getDefaultSetting();
    setting.saveString(AV_PUSH_SERVICE_APP_DATA,
            ICON_KEY, String.valueOf(icon));
  }

  void addDefaultPushCallback(String channel, String clsName) {
    defaultPushCallback.put(channel, clsName);
    SystemSetting setting = AppConfiguration.getDefaultSetting();
    setting.saveString(AV_PUSH_SERVICE_APP_DATA, channel, String.valueOf(clsName));
  }

  void removeDefaultPushCallback(String channel) {
    defaultPushCallback.remove(channel);
    SystemSetting setting = AppConfiguration.getDefaultSetting();
    setting.removeKey(AV_PUSH_SERVICE_APP_DATA, channel);
  }

  private boolean containsDefaultPushCallback(String channel) {
    return defaultPushCallback.containsKey(channel);
  }

  String getDefaultPushCallback(String channel) {
    return StringUtil.isEmpty(channel) ? null : defaultPushCallback.get(channel);
  }

  public int size() {
    return defaultPushCallback.size();
  }

  static String getJSONValue(String msg, String key) {
    Map<String, Object> jsonMap = JSON.parseObject(msg, HashMap.class);
    if (jsonMap == null || jsonMap.isEmpty()) return null;

    Object action = jsonMap.get(key);
    return action != null ? action.toString() : null;
  }

  private String getChannel(String msg) {
    return getJSONValue(msg, "_channel");
  }

  private String getAction(String msg) {
    return getJSONValue(msg, "action");
  }

  /**
   * 是否为静默推送
   * 默认值为 false，及如果 server 并没有传 silent 字段，则默认为通知栏推送
   * @param message
   * @return
   */
  private boolean getSilent(String message) {
    if (!StringUtil.isEmpty(message)) {
      try {
        JSONObject object = JSON.parseObject(message);
        return object.containsKey("silent")? object.getBooleanValue("silent"): false;
      } catch (JSONException e) {
        LOGGER.e("failed to parse JSON.", e);
      }
    }
    return false;
  }

  private Date getExpiration(String msg) {
    String result = "";
    try {
      JSONObject object = JSON.parseObject(msg);
      result = object.getString("_expiration_time");
    } catch (JSONException e) {
      // LogUtil.avlog.i(e);
      // 不应该当做一个Error发出来，既然expire仅仅是一个option的数据
      // Log.e(LOGTAG, "Get expiration date error.", e);
    }
    if (StringUtil.isEmpty(result)) {
      return null;
    }
    Date date = StringUtil.dateFromString(result);
    return date;
  }

  public void processPushMessage(String message, String messageId) {
    try {
      String channel = getChannel(message);
      if (channel == null || !containsDefaultPushCallback(channel)) {
        channel = AVOSCloud.getApplicationId();
      }

      Date expiration = getExpiration(message);
      if (expiration != null) {
        if (expiration.before(new Date())) {
          LOGGER.d("message expired:" + message);
          return;
        }
      }

      String action = getAction(message);
      if (action != null) {
        sendBroadcast(channel, message, action);
      } else {
        sendNotification(channel, message);
      }
    } catch (Exception e) {
      LOGGER.e("Process notification failed.", e);
    }
  }

  private void sendNotification(String from, String msg) throws IllegalArgumentException {
    ;
  }

  private void sendBroadcast(String channel, String msg, String action) {
    ;
  }
}
