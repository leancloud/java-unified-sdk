package cn.leancloud;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by fengjunwen on 2018/8/28.
 */

public class AVFirebaseInstanceIDService extends FirebaseInstanceIdService {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVFirebaseInstanceIDService.class);
  private final String VENDOR = "fcm";

  @Override
  public void onTokenRefresh() {
    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    sendRegistrationToServer(refreshedToken);
    LOGGER.d("refreshed token: " + refreshedToken);
  }

  private void sendRegistrationToServer(String refreshedToken) {
    if (StringUtil.isEmpty(refreshedToken)) {
      return;
    }
    AVInstallation installation = AVInstallation.getCurrentInstallation();
    if (!VENDOR.equals(installation.getString(AVInstallation.VENDOR))) {
      installation.put(AVInstallation.VENDOR, VENDOR);
    }
    if (!refreshedToken.equals(installation.getString(AVInstallation.REGISTRATION_ID))) {
      installation.put(AVInstallation.REGISTRATION_ID, refreshedToken);
    }
    installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
      @Override
      public void done(AVException e) {
        if (null != e) {
          LOGGER.e("failed to update installation.", e);
        } else {
          LOGGER.d("succeed to update installation.");
        }
      }
    }));

    LOGGER.d("FCM registration success! registrationId=" + refreshedToken);
  }
}