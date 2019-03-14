package cn.leancloud;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.push.AndroidNotificationManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by fengjunwen on 2018/4/24.
 */

public class AVHMSPushMessageReceiver extends com.huawei.hms.support.api.push.PushReceiver{
  static final AVLogger LOGGER = LogUtil.getLogger(AVHMSPushMessageReceiver.class);

  static final String MIXPUSH_PRIFILE = "deviceProfile";
  static final String VENDOR = "HMS";

  private void updateAVInstallation(String hwToken) {
    if (StringUtil.isEmpty(hwToken)) {
      return;
    }
    AVInstallation installation = AVInstallation.getCurrentInstallation();
    if (!VENDOR.equals(installation.getString(AVInstallation.VENDOR))) {
      installation.put(AVInstallation.VENDOR, VENDOR);
    }
    if (!hwToken.equals(installation.getString(AVInstallation.REGISTRATION_ID))) {
      installation.put(AVInstallation.REGISTRATION_ID, hwToken);
    }
    String localProfile = installation.getString(MIXPUSH_PRIFILE);
    if (null == localProfile) {
      localProfile = "";
    }
    if (!localProfile.equals(AVMixPushManager.hwDeviceProfile)) {
      installation.put(AVMixPushManager.MIXPUSH_PROFILE, AVMixPushManager.hwDeviceProfile);
    }

    installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
      @Override
      public void done(AVException e) {
        if (null != e) {
          LOGGER.e("update installation error!", e);
        } else {
          LOGGER.d("Huawei push registration successful!");
        }
      }
    }));
  }

  /**
   * 响应 token 通知。
   *
   * @param context
   * @param token
   * @param bundle
   */
  @Override
  public void onToken(Context context, String token, Bundle bundle) {
    updateAVInstallation(token);
  }

  /**
   * 收到透传消息
   *
   * 消息格式类似于：
   *      {"alert":"", "title":"", "action":"", "silent":true}
   * SDK 内部会转换成 {"content":\\"{"alert":"", "title":"", "action":"", "silent":true}\\"}
   * 再发送给本地的 Receiver。
   *
   * 所以，开发者如果想自己处理透传消息，则需要从 Receiver#onReceive(Context context, Intent intent) 的 intent 中通过
   * getStringExtra("content") 获取到实际的数据。
   *
   * @param var1
   * @param var2
   * @param var3
   */
  @Override
  public void onPushMsg(Context var1, byte[] var2, String var3) {
    try {
      String message = new String(var2, "UTF-8");
      AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
      androidNotificationManager.processMixPushMessage(message);
    } catch (Exception ex) {
      LOGGER.e("failed to process PushMessage.", ex);
    }
  }

  /**
   * 响应通知栏点击事件
   * 注意：这一机制基本上是失效的，华为官方不推荐使用这一接口来响应不同的通知内容。
   *
   * @param context
   * @param event
   * @param extras
   */
  @Override
  public void onEvent(Context context, Event event, Bundle extras) {
    LOGGER.d("received Notify Event. Event=" + event);
    if (Event.NOTIFICATION_CLICK_BTN.equals(event) || Event.NOTIFICATION_OPENED.equals(event)) {
      int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
      LOGGER.d("received Push Event. notifyId:" + notifyId);
      if (0 != notifyId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notifyId);
      }
    } else {
      LOGGER.d("unknow event.");
    }
    super.onEvent(context, event, extras);
  }

  /**
   * 响应推送状态变化通知。
   *
   * @param context
   * @param pushState
   */
  @Override
  public void onPushState(Context context, boolean pushState) {
    LOGGER.d("pushState changed, current=" + pushState);
  }

}