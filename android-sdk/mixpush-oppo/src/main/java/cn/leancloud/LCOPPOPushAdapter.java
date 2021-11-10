package cn.leancloud;

import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.oppo.LCMixPushManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.callback.SaveCallback;

/**
 * OPPO推送暂时只支持通知栏消息的推送。消息下发到OS系统模块并由系统通知模块展示，在用户点击通知前，不启动应用。
 * 参考：https://open.oppomobile.com/wiki/doc#id=10196
 */
public class LCOPPOPushAdapter implements com.heytap.msp.push.callback.ICallBackResultService {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCOPPOPushAdapter.class);
  private static final String VENDOR_OPPO = "oppo";

  private void updateLCInstallation(String registerId) {
    if (!StringUtil.isEmpty(registerId)) {
      LCInstallation installation = LCInstallation.getCurrentInstallation();

      if (!VENDOR_OPPO.equals(installation.getString(LCInstallation.VENDOR))) {
        installation.put(LCInstallation.VENDOR, VENDOR_OPPO);
      }
      if (!registerId.equals(installation.getString(LCInstallation.REGISTRATION_ID))) {
        installation.put(LCInstallation.REGISTRATION_ID, registerId);
      }
      String localProfile = installation.getString(LCMixPushManager.MIXPUSH_PROFILE);
      localProfile = (null != localProfile ? localProfile : "");
      if (!localProfile.equals(LCMixPushManager.oppoDeviceProfile)) {
        installation.put(LCMixPushManager.MIXPUSH_PROFILE, LCMixPushManager.oppoDeviceProfile);
      }
      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(LCException e) {
          if (null != e) {
            LOGGER.e("update installation error!", e);
          } else {
            LOGGER.d("oppo push registration successful!");
          }
        }
      }));
    }
  }

  public void onRegister(int responseCode, String registerID) {
    if (responseCode != 0) {
      LOGGER.e("failed to register device. errorCode: " + responseCode);
      return;
    }
    if (StringUtil.isEmpty(registerID)) {
      LOGGER.e("oppo register id is empty.");
      return;
    }
    updateLCInstallation(registerID);
  }

  public void onUnRegister(int responseCode) {
    if (responseCode != 0) {
      LOGGER.e("failed to unregister device. errorCode: " + responseCode);
    } else {
      LOGGER.i("succeeded to unregister device.");
    }
  }

  public void onSetPushTime(int responseCode, String var2) {
    if (responseCode != 0) {
      LOGGER.e("failed to setPushTime. errorCode: " + responseCode);
    } else {
      LOGGER.i("succeeded to setPushTime.");
    }
  }

  public void onGetPushStatus(int responseCode, int status) {
    if (responseCode != 0) {
      LOGGER.e("failed to getPushStatus. errorCode: " + responseCode);
    } else {
      LOGGER.i("succeeded to getPushStatus.");
    }
  }

  public void onGetNotificationStatus(int responseCode, int status) {
    if (responseCode != 0) {
      LOGGER.e("failed to getNotificationStatus. errorCode: " + responseCode);
    } else {
      LOGGER.i("succeeded to getNotificationStatus.");
    }
  }

  @Override
  public void onError(int i, String s) {
    LOGGER.w("error occurred. code: " + i + ", cause: " + s);
  }
}
