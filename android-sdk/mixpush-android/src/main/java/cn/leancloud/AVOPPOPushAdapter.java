package cn.leancloud;

import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.callback.SaveCallback;

/**
 * OPPO推送暂时只支持通知栏消息的推送。消息下发到OS系统模块并由系统通知模块展示，在用户点击通知前，不启动应用。
 * 参考：https://open.oppomobile.com/wiki/doc#id=10196
 */
public class AVOPPOPushAdapter extends com.coloros.mcssdk.callback.PushAdapter {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVOPPOPushAdapter.class);
  private static final String VENDOR_OPPO = "oppo";

  private void updateAVInstallation(String registerId) {
    if (!StringUtil.isEmpty(registerId)) {
      AVInstallation installation = AVInstallation.getCurrentInstallation();

      if (!VENDOR_OPPO.equals(installation.getString(AVInstallation.VENDOR))) {
        installation.put(AVInstallation.VENDOR, VENDOR_OPPO);
      }
      if (!registerId.equals(installation.getString(AVInstallation.REGISTRATION_ID))) {
        installation.put(AVInstallation.REGISTRATION_ID, registerId);
      }
      String localProfile = installation.getString(AVMixPushManager.MIXPUSH_PROFILE);
      localProfile = (null != localProfile ? localProfile : "");
      if (!localProfile.equals(AVMixPushManager.oppoDeviceProfile)) {
        installation.put(AVMixPushManager.MIXPUSH_PROFILE, AVMixPushManager.oppoDeviceProfile);
      }
      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(AVException e) {
          if (null != e) {
            LOGGER.e("update installation error!", e);
          } else {
            LOGGER.d("oppo push registration successful!");
          }
        }
      }));
    }
  }

  @Override
  public void onRegister(int responseCode, String registerID) {
    if (responseCode != com.coloros.mcssdk.mode.ErrorCode.SUCCESS) {
      LOGGER.e("failed to register device. errorCode: " + responseCode);
      return;
    }
    if (StringUtil.isEmpty(registerID)) {
      LOGGER.e("oppo register id is empty.");
      return;
    }
    updateAVInstallation(registerID);
  }
}
