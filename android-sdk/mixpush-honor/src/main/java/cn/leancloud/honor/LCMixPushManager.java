package cn.leancloud.honor;

import android.app.Application;

import com.hihonor.push.sdk.HonorPushClient;

import cn.leancloud.LCHonorMessageService;

public class LCMixPushManager {
    public static final String MIXPUSH_PROFILE = "deviceProfile";

    /**
     * 华为推送的 deviceProfile
     */
    public static String deviceProfile = "";
    static Class hwMessageServiceClazz = LCHonorMessageService.class;

    /**
     * 初始化方法，建议在 Application onCreate 里面调用
     *
     * @param application 应用实例
     */
    public static void registerHonorPush(Application application) {
        HonorPushClient.getInstance().init(application, true);
    }
}
