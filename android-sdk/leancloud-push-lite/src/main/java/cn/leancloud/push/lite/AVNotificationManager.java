package cn.leancloud.push.lite;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.leancloud.push.lite.utils.AVPersistenceUtils;
import cn.leancloud.push.lite.utils.StringUtil;

public class AVNotificationManager {
  private static final String TAG = AVNotificationManager.class.getSimpleName();

  private static final String PUSH_INTENT_KEY = "com.avoscloud.push";
  private static final String AV_PUSH_SERVICE_APP_DATA = "AV_PUSH_SERVICE_APP_DATA";
  private static final String ICON_KEY = "_notification_icon";

  private static final Random random = new Random();

  private final ConcurrentMap<String, String> defaultPushCallback =
      new ConcurrentHashMap<String, String>();
  private final ConcurrentMap<String, String> processedMessages = new ConcurrentHashMap<>();
  private int notificationIcon = 0;
  private Context context = null;

  private static AVNotificationManager INSTANCE = null;

  public synchronized static AVNotificationManager getInstance() {
    if (null == INSTANCE) {
      INSTANCE = new AVNotificationManager(AVOSCloud.applicationContext);
    }
    return INSTANCE;
  }

  private AVNotificationManager(Context context) {
    if (null != context) {
      this.notificationIcon = context.getApplicationInfo().icon;
      this.context = context;
      readDataFromCache();
    } else {
      Log.w(TAG, "Context is null, please call AVOSCloud#initialize at first.");
    }
  }

  private void readDataFromCache() {
    Map<String, ?> data = AVPersistenceUtils.sharedInstance().getPersistentSetting(AV_PUSH_SERVICE_APP_DATA);
    for (Map.Entry entry : data.entrySet()) {
      String channel = (String) entry.getKey();
      if (channel.equals(ICON_KEY)) {
        try {
          notificationIcon = Integer.parseInt((String) entry.getValue());
        } catch (Exception e) {
          // ignore;
          Log.w(TAG, e);
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
    AVPersistenceUtils.sharedInstance().savePersistentSettingString(AV_PUSH_SERVICE_APP_DATA,
        ICON_KEY, String.valueOf(icon));
  }

  void addDefaultPushCallback(String channel, String clsName) {
    defaultPushCallback.put(channel, clsName);
    AVPersistenceUtils.sharedInstance().savePersistentSettingString(AV_PUSH_SERVICE_APP_DATA, channel,
        String.valueOf(clsName));
  }

  void removeDefaultPushCallback(String channel) {
    defaultPushCallback.remove(channel);
    AVPersistenceUtils.sharedInstance().removePersistentSettingString(AV_PUSH_SERVICE_APP_DATA, channel);
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
      } catch (JSONException e) {
        Log.e(TAG,"failed to parse JSON.", e);
      }
    }
    return false;
  }

  static Date getExpiration(String msg) {
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
    if (processedMessages.containsKey(messageId)) {
      Log.w(TAG, "duplicated push message: " + message);
      return;
    }
    processedMessages.put(messageId, "");

    try {
      String channel = getChannel(message);
      if (channel == null || !containsDefaultPushCallback(channel)) {
        channel = AVOSCloud.getApplicationId();
      }

      Date expiration = getExpiration(message);
      if (expiration != null) {
        if (expiration.before(new Date())) {
          Log.d(TAG, "message expired:" + message);
          return;
        }
      }

      String action = getAction(message);
      if (AVOSCloud.isDebugLogEnabled()) {
        Log.d(TAG, "process push message:" + message + ", channel:" + channel + ", action:" + action);
      }
      if (action != null) {
        sendBroadcast(channel, message, action);
      } else {
        sendNotification(channel, message);
      }
    } catch (Exception e) {
      Log.e(TAG,"Process notification failed. cause: " + e.getMessage());
    }
  }

  private Intent buildUpdateIntent(String channel, String msg, String action) {
    Intent updateIntent = new Intent();
    if (action != null) {
      updateIntent.setAction(action);
    }
    updateIntent.putExtra(PUSH_INTENT_KEY, 1);
    updateIntent.putExtra("com.avos.avoscloud.Channel", channel);
    updateIntent.putExtra("com.avoscloud.Channel", channel);
    updateIntent.putExtra("com.avos.avoscloud.Data", msg);
    updateIntent.putExtra("com.avoscloud.Data", msg);
    updateIntent.setPackage(this.context.getPackageName());
    return updateIntent;
  }

  void sendNotification(String from, String msg) throws IllegalArgumentException {
    Intent resultIntent = buildUpdateIntent(from, msg, null);
    sendNotification(from, msg, resultIntent);
  }

  @TargetApi(Build.VERSION_CODES.O)
  private void sendNotification(String from, String msg, Intent resultIntent) {
    String clsName = getDefaultPushCallback(from);
    if (StringUtil.isEmpty(clsName)) {
      throw new IllegalArgumentException(
          "No default callback found, did you forget to invoke setDefaultPushCallback?");
    }
    int lastIndex = clsName.lastIndexOf(".");
    if (lastIndex != -1) {
      // String packageName = clsName.substring(0, lastIndex);
      // Log.d(LOGTAG, "packageName: " + packageName);
      int notificationId = random.nextInt();
      ComponentName cn = new ComponentName(context, clsName);
      resultIntent.setComponent(cn);
      PendingIntent contentIntent =
          PendingIntent.getActivity(context, notificationId, resultIntent, 0);
      String sound = getSound(msg);
      Notification notification = null;
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(getTitle(msg)).setAutoCancel(true).setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setContentText(getText(msg));
        notification = mBuilder.build();
      } else {
        Notification.Builder builder = new Notification.Builder(context)
            .setSmallIcon(getNotificationIcon())
            .setContentTitle(getTitle(msg))
            .setAutoCancel(true).setContentIntent(contentIntent)
            .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
            .setContentText(getText(msg))
            .setChannelId(PushService.DefaultChannelId);

        notification = builder.build();
      }
      if (sound != null && sound.trim().length() > 0) {
        notification.sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + sound);
      }
      NotificationManager manager =
          (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      manager.notify(notificationId, notification);
    } else {
      Log.e(TAG,"Class name is invalid, which must contain '.': " + clsName);
    }
  }

  String getApplicationName() {
    final PackageManager pm = context.getPackageManager();
    ApplicationInfo ai;
    try {
      ai = pm.getApplicationInfo(context.getPackageName(), 0);
    } catch (final PackageManager.NameNotFoundException e) {
      ai = null;
    }
    final String applicationName =
        (String) (ai != null ? pm.getApplicationLabel(ai) : "Notification");
    return applicationName;
  }

  void sendBroadcast(String channel, String msg, String action) {
    Intent updateIntent = buildUpdateIntent(channel, msg, action);
    Log.d(TAG, "action: " + updateIntent.getAction());
    context.sendBroadcast(updateIntent);
    Log.d(TAG, "sent broadcast");
  }

  /**
   * 处理透传消息（华为只有透传）
   * @param message
   */
//  public void processMixPushMessage(String message) {
//    if (!StringUtil.isEmpty(message)) {
//      String channel = getChannel(message);
//      if (channel == null || !containsDefaultPushCallback(channel)) {
//        channel = AVOSCloud.getApplicationId();
//      }
//
//      String action = getAction(message);
//      boolean isSlient = getSilent(message);
//      if (action != null) {
//        sendBroadcast(channel, message, action);
//      } else if (!isSlient) {
//        sendNotification(channel, message);
//      } else {
//        Log.e(TAG, "ignore push silent message: " + message);
//      }
//    }
//  }

  /**
   * 处理混合推送到达事件（暂只支持小米）
   * @param message
   * @param action
   */
//  public void porcessMixNotificationArrived(String message, String action) {
//    if (!StringUtil.isEmpty(message) && !StringUtil.isEmpty(action)) {
//      String channel = getChannel(message);
//      if (channel == null || !containsDefaultPushCallback(channel)) {
//        channel = AVOSCloud.getApplicationId();
//      }
//
//      sendNotificationBroadcast(channel, message, action);
//    }
//  }

  /**
   * 处理混合推送通知栏消息点击后的事件（现在支持小米、魅族，华为不支持）
   * 处理逻辑：如果是自定义 action 的消息点击事件，则发送 broadcast，否则按照 sdk 自有逻辑打开相应的 activity
   * @param message
   */
//  public void processMixNotification(String message, String defaultAction) {
//    if (StringUtil.isEmpty(message)) {
//      Log.e(TAG, "message is empty, ignore.");
//    } else {
//      String channel = getChannel(message);
//      if (channel == null || !containsDefaultPushCallback(channel)) {
//        channel = AVOSCloud.getApplicationId();
//      }
//
//      String action = getAction(message);
//      if (null != action) {
//        sendNotificationBroadcast(channel, message, defaultAction);
//      } else {
//        String clsName = getDefaultPushCallback(channel);
//        if (StringUtil.isEmpty(clsName)) {
//          Log.e(TAG, "className is empty, ignore.");
//        } else {
//          Intent intent = buildUpdateIntent(channel, message, null);
//          ComponentName cn = new ComponentName(context, clsName);
//          intent.setComponent(cn);
//          intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//          PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//          try {
//            pendingIntent.send();
//          } catch (PendingIntent.CanceledException e) {
//            Log.e(TAG, "Ocurred PendingIntent.CanceledException", e);
//          }
//        }
//      }
//    }
//  }

  /**
   * 处理 GCM 的透传消息
   * @param channel
   * @param action
   * @param message
   */
//  public void processGcmMessage(String channel, String action, String message) {
//    if (channel == null || !containsDefaultPushCallback(channel)) {
//      channel = AVOSCloud.getApplicationId();
//      if (action != null) {
//        sendBroadcast(channel, message, action);
//      } else {
//        sendNotification(channel, message);
//      }
//    }
//  }

  /**
   * 给订阅了小米 action 的 broadcastreciver 发 broadcast
   * @param channel
   * @param msg
   */
//  private void sendNotificationBroadcast(String channel, String msg, String action) {
//    Intent updateIntent = buildUpdateIntent(channel, msg, action);
//    Log.d(TAG, "action: " + updateIntent.getAction());
//    context.sendBroadcast(updateIntent);
//    Log.d(TAG, "sent broadcast");
//  }
}
