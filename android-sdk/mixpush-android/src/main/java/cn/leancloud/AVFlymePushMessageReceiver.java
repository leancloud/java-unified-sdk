package cn.leancloud;

import android.content.Context;
import android.content.Intent;

import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.push.AndroidNotificationManager;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.utils.LogUtil;

/**
 * Created by wli on 2017/2/14.
 */

public class AVFlymePushMessageReceiver extends com.meizu.cloud.pushsdk.MzPushMessageReceiver {
  private final static AVLogger LOGGER = LogUtil.getLogger(AVFlymePushMessageReceiver.class);
  private static final String AV_MIXPUSH_FLYME_NOTIFICATION_ACTION = "com.avos.avoscloud.flyme_notification_action";

  private final String FLYME_VERDOR = "mz";

  private void updateAVInstallation(String flymePushId) {
    if (!StringUtil.isEmpty(flymePushId)) {
      AVInstallation installation = AVInstallation.getCurrentInstallation();

      if (!FLYME_VERDOR.equals(installation.getString(AVInstallation.VENDOR))) {
        installation.put(AVInstallation.VENDOR, FLYME_VERDOR);
      }
      if (!flymePushId.equals(installation.getString(AVInstallation.REGISTRATION_ID))) {
        installation.put(AVInstallation.REGISTRATION_ID, flymePushId);
      }

      String localProfile = installation.getString(AVMixPushManager.MIXPUSH_PROFILE);
      localProfile = (null != localProfile ? localProfile : "");
      if (!localProfile.equals(AVMixPushManager.flymeDeviceProfile)) {
        installation.put(AVMixPushManager.MIXPUSH_PROFILE, AVMixPushManager.flymeDeviceProfile);
      }

      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(AVException e) {
          if (null != e) {
            LOGGER.e("update installation error!", e);
          } else {
            LOGGER.d("flyme push registration successful!");
          }
        }
      }));
    }
  }


  /**
   * 处理透传消息
   *
   */

  @Override
  public void onMessage(Context context, String s) {
    if (null == context || null == s) {
      return;
    }
    LOGGER.d("throughMessage coming, message=" + s);
    AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
    androidNotificationManager.processMixPushMessage(s);
  }

  @Override
  public void onMessage(Context var1, String message, String platformExtra) {
    // onMessage(Context context, String s) 实现一个即可
  }

  @Override
  public void onMessage(Context context, Intent intent) {
    // flyme3.0平台支持透传消息,只有本方法才能处理flyme3的透传消息,具体相见flyme3获取消息的方法
  }

  @Override
  public void onPushStatus(Context context, com.meizu.cloud.pushsdk.platform.message.PushSwitchStatus pushSwitchStatus) {
    //检查通知栏和透传消息开关状态回调
    if (null == context || null == pushSwitchStatus) {
      return;
    }
    LOGGER.d("switchNotificationMessage=" + pushSwitchStatus.isSwitchNotificationMessage()
        + ", switchThroughMessage=" + pushSwitchStatus.isSwitchThroughMessage() + ", pushId=" + pushSwitchStatus.getPushId());

    String pushId = pushSwitchStatus.getPushId();
    if (!StringUtil.isEmpty(pushId)
        && (pushSwitchStatus.isSwitchNotificationMessage() || pushSwitchStatus.isSwitchThroughMessage())) {
      updateAVInstallation(pushId);
    }
  }

  /**
   * 处理设备注册事件
   *
   */

  @Override
  public void onRegisterStatus(Context context, com.meizu.cloud.pushsdk.platform.message.RegisterStatus registerStatus) {
    //调用新版订阅PushManager.register(context,appId,appKey)回调
    if (null == context || null == registerStatus) {
      return;
    }
    LOGGER.d("register successed, pushId=" + registerStatus.getPushId());
    String pushId = registerStatus.getPushId();
    if (!StringUtil.isEmpty(pushId)) {
      updateAVInstallation(pushId);
    }
  }

  @Override
  public void onUnRegisterStatus(Context context, com.meizu.cloud.pushsdk.platform.message.UnRegisterStatus unRegisterStatus) {
    //新版反订阅回调
    if (null == context || null == unRegisterStatus) {
      return;
    }
    LOGGER.d("unregister successed, message=" + unRegisterStatus.getMessage());
  }

  @Override
  public void onSubTagsStatus(Context context, com.meizu.cloud.pushsdk.platform.message.SubTagsStatus subTagsStatus) {
    //标签回调
  }

  @Override
  public void onSubAliasStatus(Context context, com.meizu.cloud.pushsdk.platform.message.SubAliasStatus subAliasStatus) {
    //别名回调
  }

  @Override
  public void onUnRegister(Context var1, boolean var2) {}

  @Override
  public void onRegister(Context var1, String var2) {}

  /**
   * 处理通知栏消息
   *
   */

  @Override
  public void onUpdateNotificationBuilder(com.meizu.cloud.pushsdk.notification.PushNotificationBuilder pushNotificationBuilder) {
    //重要,详情参考应用小图标自定设置
    if (AVMixPushManager.flymeMStatusBarIcon != 0) {
      pushNotificationBuilder.setmStatusbarIcon(AVMixPushManager.flymeMStatusBarIcon);
    }
  }

  @Override
  public void onNotificationArrived(Context context, com.meizu.cloud.pushsdk.handler.MzPushMessage var2) {
    //通知栏消息到达回调，flyme6基于android6.0以上不再回调
  }

  @Override
  public void onNotificationClicked(Context context, com.meizu.cloud.pushsdk.handler.MzPushMessage var2) {
    //通知栏消息点击回调
    if (null == context || null == var2) {
      return;
    }
    LOGGER.d("notificationClicked, message=" + var2.getSelfDefineContentString());
    String selfDefineContentString = var2.getSelfDefineContentString();
    AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
    androidNotificationManager.processMixNotification(selfDefineContentString,
        AV_MIXPUSH_FLYME_NOTIFICATION_ACTION);
  }

  @Override
  public void onNotificationDeleted(Context context, com.meizu.cloud.pushsdk.handler.MzPushMessage var2) {
    //通知栏消息删除回调；flyme6基于android6.0以上不再回调
  }

  @Override
  public void onNotifyMessageArrived(Context context, String message) {
  }
}
