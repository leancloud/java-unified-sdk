package cn.leancloud;

import com.hihonor.push.sdk.HonorMessageService;
import com.hihonor.push.sdk.HonorPushDataMsg;

import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.honor.LCMixPushManager;
import cn.leancloud.push.AndroidNotificationManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

public class LCHonorMessageService extends HonorMessageService {
    static final LCLogger LOGGER = LogUtil.getLogger(LCHonorMessageService.class);

    static final String MIXPUSH_PRIFILE = "deviceProfile";
    static final String VENDOR = "honor";

    @Override
    public void onNewToken(String token) {
        updateAVInstallation(token);
    }

    @Override
    public void onMessageReceived(HonorPushDataMsg msg) {
        try {
            AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
            String message = msg.getData();
            if (!StringUtil.isEmpty(message)) {
                LOGGER.d("received passthrough(data) message: " + message);
                androidNotificationManager.processMixPushMessage(message);
            } else {
                LOGGER.e("unknown passthrough message: " + msg.toString());
            }
        } catch (Exception ex) {
            LOGGER.e("failed to process PushMessage.", ex);
        }
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
        if (!localProfile.equals(LCMixPushManager.deviceProfile)) {
            installation.put(LCMixPushManager.MIXPUSH_PROFILE, LCMixPushManager.deviceProfile);
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
