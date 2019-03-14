package cn.leancloud;

import android.content.Context;

import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.callback.SaveCallback;

public abstract class AVVIVOPushMessageReceiver extends com.vivo.push.sdk.OpenClientPushMessageReceiver {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVVIVOPushMessageReceiver.class);
  private final String VIVO_VERDOR = "vivo";

  public abstract void onNotificationMessageClicked(Context var1, com.vivo.push.model.UPSNotificationMessage var2);

  public void onReceiveRegId(Context var1, final String regId) {
    if (StringUtil.isEmpty(regId)) {
      LOGGER.e("received empty regId from VIVO server.");
    } else {
      AVInstallation installation = AVInstallation.getCurrentInstallation();

      if (!VIVO_VERDOR.equals(installation.getString(AVInstallation.VENDOR))) {
        installation.put(AVInstallation.VENDOR, VIVO_VERDOR);
      }
      if (!regId.equals(installation.getString(AVInstallation.REGISTRATION_ID))) {
        installation.put(AVInstallation.REGISTRATION_ID, regId);
      }

      String localProfile = installation.getString(AVMixPushManager.MIXPUSH_PROFILE);
      localProfile = (null != localProfile ? localProfile : "");
      if (!localProfile.equals(AVMixPushManager.vivoDeviceProfile)) {
        installation.put(AVMixPushManager.MIXPUSH_PROFILE, AVMixPushManager.vivoDeviceProfile);
      }

      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(AVException e) {
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
