package cn.leancloud.push;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by fengjunwen on 2018/8/18.
 */

public class AndroidNotificationManager extends AVNotificationManager {
  private static final AVLogger LOGGER = LogUtil.getLogger(AndroidNotificationManager.class);
  private static final String PUSH_INTENT_KEY = "com.avoscloud.push";
  private static final Random random = new Random();
  private static final AndroidNotificationManager INSTANCE = new AndroidNotificationManager();
  private Context serviceContext;

  public static AndroidNotificationManager getInstance() {
    return INSTANCE;
  }

  private AndroidNotificationManager() {
  }

  public void setServiceContext(Context context) {
    this.serviceContext = context;
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
    if (null != AVOSCloud.getContext()) {
      updateIntent.setPackage(AVOSCloud.getContext().getPackageName());
    } else {
      updateIntent.setPackage(this.serviceContext.getPackageName());
    }
    return updateIntent;
  }

  @Override
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
    Context context = null != AVOSCloud.getContext()? AVOSCloud.getContext() : serviceContext;
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
      LOGGER.e("Class name is invalid, which must contain '.': " + clsName);
    }
  }

  @Override
  String getApplicationName() {
    Context context = null != AVOSCloud.getContext()? AVOSCloud.getContext() : serviceContext;
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

  @Override
  void sendBroadcast(String channel, String msg, String action) {
    Intent updateIntent = buildUpdateIntent(channel, msg, action);
    LOGGER.d("action: " + updateIntent.getAction());
    Context context = null != AVOSCloud.getContext()? AVOSCloud.getContext() : serviceContext;
    context.sendBroadcast(updateIntent);
    LOGGER.d("sent broadcast");
  }

  /**
   * 处理透传消息（华为只有透传）
   * @param message
   */
  public void processMixPushMessage(String message) {
    if (!StringUtil.isEmpty(message)) {
      String channel = getChannel(message);
      if (channel == null || !containsDefaultPushCallback(channel)) {
        channel = AVOSCloud.getApplicationId();
      }

      String action = getAction(message);
      boolean isSlient = getSilent(message);
      if (action != null) {
        sendBroadcast(channel, message, action);
      } else if (!isSlient) {
        sendNotification(channel, message);
      } else {
        LOGGER.e("ignore push silent message: " + message);
      }
    }
  }

  /**
   * 处理混合推送到达事件（暂只支持小米）
   * @param message
   * @param action
   */
  public void porcessMixNotificationArrived(String message, String action) {
    if (!StringUtil.isEmpty(message) && !StringUtil.isEmpty(action)) {
      String channel = getChannel(message);
      if (channel == null || !containsDefaultPushCallback(channel)) {
        channel = AVOSCloud.getApplicationId();
      }

      sendNotificationBroadcast(channel, message, action);
    }
  }

  /**
   * 处理混合推送通知栏消息点击后的事件（现在支持小米、魅族，华为不支持）
   * 处理逻辑：如果是自定义 action 的消息点击事件，则发送 broadcast，否则按照 sdk 自有逻辑打开相应的 activity
   * @param message
   */
  public void processMixNotification(String message, String defaultAction) {
    if (StringUtil.isEmpty(message)) {
      LOGGER.e("message is empty, ignore.");
    } else {
      String channel = getChannel(message);
      if (channel == null || !containsDefaultPushCallback(channel)) {
        channel = AVOSCloud.getApplicationId();
      }

      String action = getAction(message);
      if (null != action) {
        sendNotificationBroadcast(channel, message, defaultAction);
      } else {
        String clsName = getDefaultPushCallback(channel);
        if (StringUtil.isEmpty(clsName)) {
          LOGGER.e("className is empty, ignore.");
        } else {
          Context context = null != AVOSCloud.getContext()? AVOSCloud.getContext() : serviceContext;
          Intent intent = buildUpdateIntent(channel, message, null);
          ComponentName cn = new ComponentName(context, clsName);
          intent.setComponent(cn);
          intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
          PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
              PendingIntent.FLAG_UPDATE_CURRENT);
          try {
            pendingIntent.send();
          } catch (PendingIntent.CanceledException e) {
            LOGGER.e("Ocurred PendingIntent.CanceledException", e);
          }
        }
      }
    }
  }

  /**
   * 处理 GCM 的透传消息
   * @param channel
   * @param action
   * @param message
   */
  public void processGcmMessage(String channel, String action, String message) {
    if (channel == null || !containsDefaultPushCallback(channel)) {
      channel = AVOSCloud.getApplicationId();
      if (action != null) {
        sendBroadcast(channel, message, action);
      } else {
        sendNotification(channel, message);
      }
    }
  }

  /**
   * 给订阅了小米 action 的 broadcastreciver 发 broadcast
   * @param channel
   * @param msg
   */
  private void sendNotificationBroadcast(String channel, String msg, String action) {
    Intent updateIntent = buildUpdateIntent(channel, msg, action);
    LOGGER.d("action: " + updateIntent.getAction());
    Context context = null != AVOSCloud.getContext()? AVOSCloud.getContext() : serviceContext;
    context.sendBroadcast(updateIntent);
    LOGGER.d("sent broadcast");
  }
}
