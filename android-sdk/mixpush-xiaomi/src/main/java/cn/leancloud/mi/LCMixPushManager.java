package cn.leancloud.mi;

import android.content.Context;
import android.os.Build;

import cn.leancloud.LCLogger;
import cn.leancloud.LCManifestUtils;
import cn.leancloud.LCMiPushMessageReceiver;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 16/6/27.
 */
public class LCMixPushManager {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCMixPushManager.class);

  public static final String MIXPUSH_PROFILE = "deviceProfile";

  /**
   * 小米推送的 deviceProfile
   */
  public static String miDeviceProfile = "";
  public static Class miPushReceiverClazz = LCMiPushMessageReceiver.class;

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey) {
    registerXiaomiPush(context, miAppId, miAppKey, "");
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param customizedReceiver 自定义 receiver
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey,
                                        Class customizedReceiver) {
    registerXiaomiPush(context, miAppId, miAppKey, "", customizedReceiver);
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param profile  小米推送配置
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey, String profile) {
    registerXiaomiPush(context, miAppId, miAppKey, profile, null);
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param profile  小米推送配置
   * @param customizedReceiver 自定义 receiver
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey, String profile,
                                        Class customizedReceiver) {
    if (null == context) {
      throw new IllegalArgumentException("context cannot be null.");
    }

    if (StringUtil.isEmpty(miAppId)) {
      throw new IllegalArgumentException("miAppId cannot be null.");
    }

    if (StringUtil.isEmpty(miAppKey)) {
      throw new IllegalArgumentException("miAppKey cannot be null.");
    }

    if (null != customizedReceiver) {
      miPushReceiverClazz = customizedReceiver;
    }

    if (!isXiaomiPhone()) {
      printErrorLog("register error, current device is not a xiaomi phone!");
      return;
    }

    if (!checkXiaomiManifest(context)) {
      printErrorLog("register error, mainifest is incomplete(receiver not found: "
          + miPushReceiverClazz.getSimpleName() + ")!");
      return;
    }

    miDeviceProfile = profile;

    com.xiaomi.mipush.sdk.MiPushClient.registerPush(context, miAppId, miAppKey);

    LOGGER.d("finished to register mi push");
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param profile  小米推送配置
   * @param isInternationalVendor  是否为小米国际版设备
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey,
                                        String profile, boolean isInternationalVendor) {
    registerXiaomiPush(context, miAppId, miAppKey, profile, isInternationalVendor, null);
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param profile  小米推送配置
   * @param isInternationalVendor  是否为小米国际版设备
   * @param customizedReceiver 自定义 receiver
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey,
                                        String profile, boolean isInternationalVendor, Class customizedReceiver) {
    LCMiPushMessageReceiver.setInternationalVendor(isInternationalVendor);
    registerXiaomiPush(context, miAppId, miAppKey, profile, customizedReceiver);
  }


  private static boolean isXiaomiPhone() {
    final String phoneManufacturer = Build.MANUFACTURER;
    return !StringUtil.isEmpty(phoneManufacturer)
        && phoneManufacturer.toLowerCase().contains("xiaomi");
  }

  private static boolean checkXiaomiManifest(Context context) {
    try {
      return LCManifestUtils.checkReceiver(context, miPushReceiverClazz);
    } catch (Exception e) {
      LOGGER.d(e.getMessage());
    }
    return false;
  }

  private static void printErrorLog(String error) {
    if (!StringUtil.isEmpty(error)) {
      LOGGER.e(error);
    }
  }
}
