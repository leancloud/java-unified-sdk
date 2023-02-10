package cn.leancloud.honor;

import android.content.Context;

import com.hihonor.push.sdk.HonorPushCallback;
import com.hihonor.push.sdk.HonorPushClient;

import cn.leancloud.LCException;
import cn.leancloud.LCHonorMessageService;
import cn.leancloud.LCLogger;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.utils.LogUtil;

public class LCMixPushManager {
    private static final LCLogger LOGGER = LogUtil.getLogger(LCMixPushManager.class);

    public static final String MIXPUSH_PROFILE = "deviceProfile";

    /**
     * 华为推送的 deviceProfile
     */
    public static String deviceProfile = "";
    static Class messageServiceClazz = LCHonorMessageService.class;

    /**
     * 初始化方法，建议在 Application onCreate 里面调用
     *
     * @param application 应用实例
     */
    public static void registerHonorPush(Context application) {
        HonorPushClient.getInstance().init(application, true);
    }

    /**
     * 初始化方法，建议在 Application onCreate 里面调用
     * @param application 应用实例
     * @param profile 推送配置
     */
    public static void registerHonorPush(Context application, String profile) {
        registerHonorPush(application);
        deviceProfile = profile;
    }

    /**
     * 打开通知栏状态
     * @param callback 回调函数
     */
    public static void turnOnHonorPush(LCCallback<Void> callback) {
        HonorPushClient.getInstance().turnOnNotificationCenter(new HonorPushCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (null != callback) {
                    callback.internalDone(null);
                }
            }

            @Override
            public void onFailure(int i, String s) {
                LOGGER.w("failed to turnOn Push. code: " + i + ", message: " + s);
                if (null != callback) {
                    callback.internalDone(new LCException(i, s));
                }
            }
        });
    }

    /**
     * 关闭通知栏状态
     * @param callback 回调函数
     */
    public static void turnOffHonorPush(LCCallback<Void> callback) {
        HonorPushClient.getInstance().turnOffNotificationCenter(new HonorPushCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (null != callback) {
                    callback.internalDone(null);
                }
            }

            @Override
            public void onFailure(int i, String s) {
                LOGGER.w("failed to turnOff Push. code: " + i + ", message: " + s);
                if (null != callback) {
                    callback.internalDone(new LCException(i, s));
                }
            }
        });
    }

    /**
     * 校验当前系统是否支持 PUSH
     * @return true or false.
     */
    public static boolean isSupportHonorPush() {
        return HonorPushClient.getInstance().checkSupportHonorPush();
    }

    /**
     * 查询应用是否允许显示通知栏消息，可以通过 getHonorPushStatus 查询当前应用通知栏状态。
     * @param callback 回调函数
     */
    public static void getHonorPushStatus(LCCallback<Boolean> callback) {
        HonorPushClient.getInstance().getNotificationCenterStatus(new HonorPushCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (null != callback) {
                    callback.internalDone(aBoolean, null);
                }
            }

            @Override
            public void onFailure(int i, String s) {
                LOGGER.w("failed to get notification status. code: " + i + ", message: " + s);
                if (null != callback) {
                    callback.internalDone(new LCException(i, s));
                }
            }
        });
    }

    /**
     * 获取 PushToken
     * @param callback 回调函数
     */
    public static void getHonorPushToken(LCCallback<String> callback) {
        HonorPushClient.getInstance().getPushToken(new HonorPushCallback<String>() {
            @Override
            public void onSuccess(String s) {
                if (null != callback) {
                    callback.internalDone(s, null);
                }
            }

            @Override
            public void onFailure(int i, String s) {
                LOGGER.w("failed to get Push Token. code: " + i + ", message: " + s);
                if (null != callback) {
                    callback.internalDone(new LCException(i, s));
                }
            }
        });
    }

    /**
     * 注销 PushToken
     * 用户拒绝接受您应用的使用协议和隐私声明后，可以调用deletePushToken方法注销PushToken，注销成功后，客户端将不再接收到消息。
     * @param callback 回调函数
     */
    public static void deleteHonorPushToken(LCCallback<Void> callback) {
        HonorPushClient.getInstance().deletePushToken(new HonorPushCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (null != callback) {
                    callback.internalDone(unused, null);
                }
            }

            @Override
            public void onFailure(int i, String s) {
                LOGGER.w("failed to delete Push Token. code: " + i + ", message: " + s);
                if (null != callback) {
                    callback.internalDone(new LCException(i, s));
                }
            }
        });
    }
}
