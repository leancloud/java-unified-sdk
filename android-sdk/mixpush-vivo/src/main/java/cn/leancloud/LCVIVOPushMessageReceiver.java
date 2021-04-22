package cn.leancloud;

import android.content.Context;

import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.vivo.LCMixPushManager;

public abstract class LCVIVOPushMessageReceiver extends com.vivo.push.sdk.OpenClientPushMessageReceiver {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCVIVOPushMessageReceiver.class);
  private final String VIVO_VERDOR = "vivo";

  public abstract void onNotificationMessageClicked(Context var1, com.vivo.push.model.UPSNotificationMessage var2);

  public void onReceiveRegId(Context var1, final String regId) {
    if (StringUtil.isEmpty(regId)) {
      LOGGER.e("received empty regId from VIVO server.");
    } else {
      LCInstallation installation = LCInstallation.getCurrentInstallation();

      if (!VIVO_VERDOR.equals(installation.getString(LCInstallation.VENDOR))) {
        installation.put(LCInstallation.VENDOR, VIVO_VERDOR);
      }
      if (!regId.equals(installation.getString(LCInstallation.REGISTRATION_ID))) {
        installation.put(LCInstallation.REGISTRATION_ID, regId);
      }

      String localProfile = installation.getString(LCMixPushManager.MIXPUSH_PROFILE);
      localProfile = (null != localProfile ? localProfile : "");
      if (!localProfile.equals(LCMixPushManager.vivoDeviceProfile)) {
        installation.put(LCMixPushManager.MIXPUSH_PROFILE, LCMixPushManager.vivoDeviceProfile);
      }

      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(LCException e) {
          if (null != e) {
            LOGGER.e("update installation(for vivo) error!", e);
          } else {
            LOGGER.d("vivo push registration successful! regId=" + regId);
          }
        }
      }));
    }
  }
}
