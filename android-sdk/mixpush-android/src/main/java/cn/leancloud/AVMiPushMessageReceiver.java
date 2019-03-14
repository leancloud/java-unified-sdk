package cn.leancloud;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.push.AndroidNotificationManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 16/6/22.
 * 该回调运行在非 UI 线程
 */
public class AVMiPushMessageReceiver extends com.xiaomi.mipush.sdk.PushMessageReceiver {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVMiPushMessageReceiver.class);
  private static final String AV_MIXPUSH_MI_NOTIFICATION_ACTION = "com.avos.avoscloud.mi_notification_action";
  private static final String AV_MIXPUSH_MI_NOTIFICATION_ARRIVED_ACTION = "com.avos.avoscloud.mi_notification_arrived_action";

  private void updateAVInstallation(String miRegId) {
    if (!StringUtil.isEmpty(miRegId)) {
      AVInstallation installation = AVInstallation.getCurrentInstallation();

      if (!"mi".equals(installation.getString(AVInstallation.VENDOR))) {
        installation.put(AVInstallation.VENDOR, "mi");
      }
      if (!miRegId.equals(installation.getString(AVInstallation.REGISTRATION_ID))) {
        installation.put(AVInstallation.REGISTRATION_ID, miRegId);
      }
      String localProfile = installation.getString(AVMixPushManager.MIXPUSH_PROFILE);
      localProfile = (null != localProfile ? localProfile : "");
      if (!localProfile.equals(AVMixPushManager.miDeviceProfile)) {
        installation.put(AVMixPushManager.MIXPUSH_PROFILE, AVMixPushManager.miDeviceProfile);
      }
      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(AVException e) {
          if (null != e) {
            LOGGER.e("update installation error!", e);
          } else {
            LOGGER.d("Xiaomi push registration successful!");
          }
        }
      }));
    }
  }

  /**
   * 处理小米推送的透传消息
   * @param miPushMessage
   */
  private void processMiPushMessage(com.xiaomi.mipush.sdk.MiPushMessage miPushMessage) {
    if (null != miPushMessage) {
      String title = miPushMessage.getTitle();
      String description = miPushMessage.getDescription();
      String content = miPushMessage.getContent();

      JSONObject jsonObject = null;
      if (!TextUtils.isEmpty(content)) {
        try {
          jsonObject = JSON.parseObject(content);
        } catch (Exception exception) {
          LOGGER.e("Parsing json data error, " + content, exception);
        }
      }
      if (null == jsonObject) {
        jsonObject = new JSONObject();
      }

      if (!StringUtil.isEmpty(title)) {
        jsonObject.put("title", title);
      }
      if (!StringUtil.isEmpty(description)) {
        jsonObject.put("alert", description);
      }
      AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
      androidNotificationManager.processMixPushMessage(jsonObject.toJSONString());
    }
  }

  /**
   * 处理小米推送点击事件
   * @param miPushMessage
   */
  private void processMiNotification(com.xiaomi.mipush.sdk.MiPushMessage miPushMessage) {
    if (null != miPushMessage) {
      String content = miPushMessage.getContent();
      if (!StringUtil.isEmpty(content)) {
        AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
        androidNotificationManager.processMixNotification(content, AV_MIXPUSH_MI_NOTIFICATION_ACTION);
      }
    }
  }

  /**
   * 注册结果
   */
  @Override
  public void onReceiveRegisterResult(Context context, com.xiaomi.mipush.sdk.MiPushCommandMessage miPushCommandMessage) {
    super.onReceiveRegisterResult(context, miPushCommandMessage);
    String command = miPushCommandMessage.getCommand();
    List<String> arguments = miPushCommandMessage.getCommandArguments();
    String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
    if (com.xiaomi.mipush.sdk.MiPushClient.COMMAND_REGISTER.equals(command)) {
      if (miPushCommandMessage.getResultCode() == com.xiaomi.mipush.sdk.ErrorCode.SUCCESS) {
        updateAVInstallation(cmdArg1);
      } else {
        LOGGER.d("register error, " + miPushCommandMessage.toString());
      }
    } else {
    }
  }

  /**
   * 通知栏消息到达事件
   * @param context
   * @param miPushMessage
   */
  @Override
  public void onNotificationMessageArrived(Context context, com.xiaomi.mipush.sdk.MiPushMessage miPushMessage) {
    if (null != miPushMessage) {
      String content = miPushMessage.getContent();
      if (!StringUtil.isEmpty(content)) {
        AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
        androidNotificationManager.porcessMixNotificationArrived(content, AV_MIXPUSH_MI_NOTIFICATION_ARRIVED_ACTION);
      }
    }
  }

  /**
   * 透传消息
   * @param context
   * @param miPushMessage
   */
  @Override
  public void onReceivePassThroughMessage(Context context, com.xiaomi.mipush.sdk.MiPushMessage miPushMessage) {
    processMiPushMessage(miPushMessage);
  }

  /**
   * 通知栏消息，用户手动点击后触发
   * @param context
   * @param miPushMessage
   */
  @Override
  public void onNotificationMessageClicked(Context context, com.xiaomi.mipush.sdk.MiPushMessage miPushMessage) {
    processMiNotification(miPushMessage);
  }
}
