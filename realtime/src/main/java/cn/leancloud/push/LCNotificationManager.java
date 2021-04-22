package cn.leancloud.push;

import cn.leancloud.LCLogger;
import cn.leancloud.cache.SystemSetting;
import cn.leancloud.core.LeanCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class LCNotificationManager {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCNotificationManager.class);

  private static final String PUSH_MESSAGE_DEPOT = "com.avos.push.message";

  private static final String AV_PUSH_SERVICE_APP_DATA = "AV_PUSH_SERVICE_APP_DATA";
  private static final String ICON_KEY = "_notification_icon";

  private final ConcurrentMap<String, String> defaultPushCallback =
          new ConcurrentHashMap<String, String>();
  protected final Map<String, String> processedMessages = new ConcurrentHashMap<>();
  private int notificationIcon = 0;

  LCNotificationManager() {
    readDataFromCache();
  }

  private void readDataFromCache() {
    SystemSetting setting = AppConfiguration.getDefaultSetting();
    for (Map.Entry entry : setting.getAll(AV_PUSH_SERVICE_APP_DATA).entrySet()) {
      String channel = (String) entry.getKey();
      if (channel.equals(ICON_KEY)) {
        try {
          notificationIcon = Integer.parseInt((String) entry.getValue());
        } catch (Exception e) {
          // ignore;
          LOGGER.w(e);
        }
      } else {
        String defaultCls = String.valueOf(entry.getValue());
        defaultPushCallback.put(channel, defaultCls);
      }
    }
  }

  int getNotificationIcon() {
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

  boolean containsDefaultPushCallback(String channel) {
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

  static String getChannel(String msg) {
    return getJSONValue(msg, "_channel");
  }

  static String getAction(String msg) {
    return getJSONValue(msg, "action");
  }

  String getTitle(String msg) {
    return getValue(msg, "title");
  }

  String getSound(String msg) {
    return getValue(msg, "sound");
  }

  private String getValue(String msg, String key) {
    String result = getJSONValue(msg, key);
    if (!StringUtil.isEmpty(result)) {
      return result;
    } else {
      Map<String, Object> jsonMap = JSON.parseObject(msg, HashMap.class);
      if (jsonMap == null || jsonMap.isEmpty()) return getApplicationName();

      Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
      if (data == null || data.isEmpty()) {
        return getApplicationName();
      }
      Object val = data.get(key);
      if (val != null) {
        return val.toString();
      } else {
        return getApplicationName();
      }
    }
  }

  static String getText(String msg) {
    String text = getJSONValue(msg, "alert");
    if (text != null && text.trim().length() > 0) {
      return text;
    } else {
      Map<String, Object> jsonMap = JSON.parseObject(msg, HashMap.class);
      if (jsonMap == null || jsonMap.isEmpty()) return null;

      Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
      if (data == null || data.isEmpty()) {
        return null;
      }
      Object val = data.get("message");
      if (val != null) {
        return val.toString();
      } else {
        return null;
      }
    }
  }
  /**
   * 是否为静默推送
   * 默认值为 false，及如果 server 并没有传 silent 字段，则默认为通知栏推送
   * @param message
   * @return
   */
  static boolean getSilent(String message) {
    if (!StringUtil.isEmpty(message)) {
      try {
        JSONObject object = JSON.parseObject(message);
        return object.containsKey("silent")? object.getBooleanValue("silent"): false;
      } catch (Exception e) {
        LOGGER.e("failed to parse JSON.", e);
      }
    }
    return false;
  }

  static Date getExpiration(String msg) {
    String result = "";
    try {
      JSONObject object = JSON.parseObject(msg);
      result = object.getString("_expiration_time");
    } catch (Exception e) {
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
    if (processedMessages.containsKey(messageId)) {
      LOGGER.w("duplicated push message, ignore it. data=" + message);
      return;
    } else {
      processedMessages.put(messageId, "");
    }
    try {
      String channel = getChannel(message);
      if (channel == null || !containsDefaultPushCallback(channel)) {
        channel = LeanCloud.getApplicationId();
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

  abstract String getApplicationName();

  abstract void sendNotification(String from, String msg) throws IllegalArgumentException;

  abstract void sendBroadcast(String channel, String msg, String action);
}
