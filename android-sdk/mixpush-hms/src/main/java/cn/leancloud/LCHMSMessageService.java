package cn.leancloud;

import cn.leancloud.json.JSON;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.hms.LCMixPushManager;
import cn.leancloud.push.AndroidNotificationManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

public class LCHMSMessageService extends HmsMessageService {
  static final LCLogger LOGGER = LogUtil.getLogger(LCHMSMessageService.class);

  static final String MIXPUSH_PRIFILE = "deviceProfile";
  static final String VENDOR = "HMS";

  public LCHMSMessageService() {
    super();
  }

  /**
   * 收到透传消息
   *
   * @param remoteMessage remote message
   */
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    try {
      AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
      String message = remoteMessage.getData();
      if (!StringUtil.isEmpty(message)) {
        LOGGER.d("received passthrough(data) message: " + message);
        androidNotificationManager.processMixPushMessage(message);
      } else if (null != remoteMessage.getNotification()) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String notifyString = JSON.toJSONString(notification);
        LOGGER.d("received passthrough(notification) message: " + notifyString);
        androidNotificationManager.processMixPushMessage(notifyString);
      } else {
        LOGGER.e("unknown passthrough message: " + remoteMessage.toString());
      }
    } catch (Exception ex) {
      LOGGER.e("failed to process PushMessage.", ex);
    }
  }

  /**
   * 服务端更新token回调方法。
   * 参考文档：https://developer.huawei.com/consumer/cn/doc/development/HMS-References/push-HmsMessageService-cls#onMessageReceived
   * APP调用getToken接口向服务端申请token，如果服务端当次没有返回token值，后续服务端返回token通过此接口返回。主要包含如下三种场景：
   * 1、申请Token如果当次调用失败，PUSH会自动重试申请，成功后则以onNewToken接口返回。
   * 2、如果服务端识别token过期，服务端刷新token也会以onNewToken方式返回。
   * 3、华为设备上EMUI版本低于10.0申请token时，以onNewToken方式返回。
   *
   * @param token push token
   */
  @Override
  public void onNewToken(String token) {
    updateAVInstallation(token);
  }

  /**
   * 申请token失败回调方法
   * @param exception exception
   */
  @Override
  public void onTokenError(Exception exception) {
    LOGGER.w("failed to apply token. cause: " + exception.getMessage());
  }

  public static void updateAVInstallation(String hwToken) {
    if (StringUtil.isEmpty(hwToken)) {
      return;
    }
    LCInstallation installation = LCInstallation.getCurrentInstallation();
    if (!VENDOR.equals(installation.getString(LCInstallation.VENDOR))) {
      installation.put(LCInstallation.VENDOR, VENDOR);
    }
    if (!hwToken.equals(installation.getString(LCInstallation.REGISTRATION_ID))) {
      installation.put(LCInstallation.REGISTRATION_ID, hwToken);
    }
    String localProfile = installation.getString(MIXPUSH_PRIFILE);
    if (null == localProfile) {
      localProfile = "";
    }
    if (!localProfile.equals(LCMixPushManager.hwDeviceProfile)) {
      installation.put(LCMixPushManager.MIXPUSH_PROFILE, LCMixPushManager.hwDeviceProfile);
    }

    installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
      @Override
      public void done(LCException e) {
        if (null != e) {
          LOGGER.e("update installation error!", e);
        } else {
          LOGGER.d("Huawei push registration successful!");
        }
      }
    }));
  }

}
