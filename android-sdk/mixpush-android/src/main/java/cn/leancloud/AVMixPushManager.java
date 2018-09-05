package cn.leancloud;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 16/6/27.
 */
public class AVMixPushManager {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVMixPushManager.class);

  static final String MIXPUSH_PRIFILE = "deviceProfile";

  /**
   * 小米推送的 deviceProfile
   */
  static String miDeviceProfile = "";

  /**
   * 华为推送的 deviceProfile
   */
  static String hwDeviceProfile = "";

  /**
   * 魅族推送的 deviceProfile
   */
  static String flymeDevicePrifile = "";
  static int flymeMStatusBarIcon = 0;

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 && MIUI && manifest 正确填写 才能注册
   *
   * @param context
   * @param miAppId
   * @param miAppKey
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey) {
    registerXiaomiPush(context, miAppId, miAppKey, "");
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 && MIUI && manifest 正确填写 才能注册
   *
   * @param context
   * @param miAppId
   * @param miAppKey
   * @param profile  小米推送配置
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey, String profile) {
    if (null == context) {
      throw new IllegalArgumentException("context cannot be null.");
    }

    if (StringUtil.isEmpty(miAppId)) {
      throw new IllegalArgumentException("miAppId cannot be null.");
    }

    if (StringUtil.isEmpty(miAppKey)) {
      throw new IllegalArgumentException("miAppKey cannot be null.");
    }

    if (!isXiaomiPhone()) {
      printErrorLog("register error, is not xiaomi phone!");
      return;
    }

    if (!checkXiaomiManifest(context)) {
      printErrorLog("register error, mainifest is incomplete!");
      return;
    }

    miDeviceProfile = profile;

    com.xiaomi.mipush.sdk.MiPushClient.registerPush(context, miAppId, miAppKey);

    LOGGER.d("start register mi push");
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application
   */
  public static void registerHMSPush(Application application) {
    registerHMSPush(application, "");
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application
   * @param profile 华为推送配置
   */
  public static void registerHMSPush(Application application, String profile) {
    if (null == application) {
      throw new IllegalArgumentException("[HMS] context cannot be null.");
    }

    if (!isHuaweiPhone()) {
      printErrorLog("[HMS] register error, is not huawei phone!");
      return;
    }

    if (!checkHuaweiManifest(application)) {
      printErrorLog("[HMS] register error, mainifest is incomplete!");
      return;
    }

    hwDeviceProfile = profile;
    boolean hmsInitResult = com.huawei.android.hms.agent.HMSAgent.init(application);
    if (!hmsInitResult) {
      LOGGER.e("failed to init HMSAgent.");
    }

    LOGGER.d("[HMS] start register HMS push");
  }

  /**
   * 连接HMS SDK， 可能拉起界面(包括升级引导等)，建议在第一个界面进行连接。
   * 此方法可以重复调用，没必要为了只调用一次做复杂处理
   * 方法为异步调用，调用结果在主线程回调
   *  Connecting to the HMS SDK may pull up the activity (including upgrade guard, etc.), and it is recommended that you connect in the first activity.
   *  This method can be called repeatedly, and there is no need to do complex processing for only one call at a time
   *  Method is called asynchronously, and the result is invoked in the main thread callback
   */
  public static void connectHMS(Activity activity) {
    if (null == activity) {
      throw new IllegalArgumentException("[HMS] activity cannot be null.");
    }
    com.huawei.android.hms.agent.HMSAgent.connect(activity,
        new com.huawei.android.hms.agent.common.handler.ConnectHandler() {
          @Override
          public void onConnect(int rst) {
            LOGGER.d("[HMS] connect end:" + rst);
            com.huawei.android.hms.agent.HMSAgent.Push.getToken(
                new com.huawei.android.hms.agent.push.handler.GetTokenHandler() {
                  @Override
                  public void onResult(int rst) {
                    LOGGER.d("[HMS] get token: end. returnCode=" + rst);
                  }
                }
            );
          }
        });
  }

  /**
   * 打开/关闭透传消息
   *  Turn on/off notification bar messages
   * @param enable 打开/关闭（默认为打开）
   *                Turn ON/off
   */
  public static void setHMSReceiveNormalMsg(final boolean enable) {
    com.huawei.android.hms.agent.HMSAgent.Push.enableReceiveNormalMsg(enable,
        new com.huawei.android.hms.agent.push.handler.EnableReceiveNormalMsgHandler() {
          @Override
          public void onResult(int rst) {
            LOGGER.d("[HMS] enableReceiveNormalMsg(flag=" + enable + ") returnCode=" + rst);
          }
        });
  }

  /**
   * 打开/关闭通知栏消息
   *  Turn on/off notification bar messages
   * @param enable 打开/关闭（默认为打开）
   *                Turn ON/off
   */
  public static void setHMSReceiveNotifyMsg(final boolean enable) {
    com.huawei.android.hms.agent.HMSAgent.Push.enableReceiveNotifyMsg(enable,
        new com.huawei.android.hms.agent.push.handler.EnableReceiveNotifyMsgHandler() {
          @Override
          public void onResult(int rst) {
            LOGGER.d("[HMS] enableReceiveNotifyMsg(flag=" + enable + ") returnCode=" + rst);
          }
        });
  }

  /**
   * 请求push协议展示
   *  Request Push Protocol Display
   */
  public static void showHMSAgreement() {
    com.huawei.android.hms.agent.HMSAgent.Push.queryAgreement(new com.huawei.android.hms.agent.push.handler.QueryAgreementHandler() {
      @Override
      public void onResult(int rst) {
        LOGGER.d("[HMS] query agreement result: " + rst);
      }
    });
  }

  /**
   * 注册魅族推送
   * @param context
   * @param flymeId
   * @param flymeKey
   * @param profile 魅族推送配置
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey, String profile) {
    if (null == context) {
      printErrorLog("register error, context is null!");
      return false;
    }
    boolean result = false;
    if (!com.meizu.cloud.pushsdk.util.MzSystemUtils.isBrandMeizu(context)) {
      printErrorLog("register error, is not flyme phone!");
    } else {
      if (!checkFlymeManifest(context)) {
        printErrorLog("register error, mainifest is incomplete!");
      } else {
        flymeDevicePrifile = profile;
        com.meizu.cloud.pushsdk.PushManager.register(context, flymeId, flymeKey);
        result = true;
        LOGGER.d("start register flyme push");
      }
    }
    return result;
  }

  /**
   * 注册魅族推送
   * @param context
   * @param flymeId
   * @param flymeKey
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey) {
    return registerFlymePush(context, flymeId, flymeKey, "");
  }

  public static void setFlymeMStatusbarIcon(int icon) {
    flymeMStatusBarIcon = icon;
  }

  /**
   * 取消混合推送的注册
   * 取消成功后，消息会通过 LeanCloud websocket 发送
   */
  public static void unRegisterMixPush() {
    AVInstallation installation = AVInstallation.getCurrentInstallation();
    String vendor = installation.getString(AVInstallation.VENDOR);
    if (!StringUtil.isEmpty(vendor)) {
      installation.put(AVInstallation.VENDOR, "lc");
      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(AVException e) {
          if (null != e) {
            printErrorLog("unRegisterMixPush error!");
          } else {
            LOGGER.d("Registration canceled successfully!");
          }
        }
      }));
    }
  }

  private static boolean isHuaweiPhone() {
    final String phoneBrand = Build.BRAND;
    try {
      return (phoneBrand.equalsIgnoreCase("huawei") || phoneBrand.equalsIgnoreCase("honor"));
    } catch (Exception e) {
      return false;
    }
  }

  private static boolean isXiaomiPhone() {
    final String phoneManufacturer = Build.MANUFACTURER;
    return !StringUtil.isEmpty(phoneManufacturer)
        && phoneManufacturer.toLowerCase().contains("xiaomi");
  }

  private static boolean checkXiaomiManifest(Context context) {
    try {
      return AVManifestUtils.checkReceiver(context, AVMiPushMessageReceiver.class);
    } catch (Exception e) {
      LOGGER.d(e.getMessage());
    }
    return false;
  }

  private static boolean checkHuaweiManifest(Context context) {
    boolean result = false;
    try {
      result = AVManifestUtils.checkPermission(context, android.Manifest.permission.INTERNET)
          && AVManifestUtils.checkPermission(context, android.Manifest.permission.ACCESS_NETWORK_STATE)
          && AVManifestUtils.checkPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)
          && AVManifestUtils.checkPermission(context, android.Manifest.permission.READ_PHONE_STATE)
          && AVManifestUtils.checkReceiver(context, AVHMSPushMessageReceiver.class);
    } catch (Exception e) {
    }
    return result;
  }

  private static boolean checkFlymeManifest(Context context) {
    boolean result = false;
    try {
      result = AVManifestUtils.checkPermission(context, android.Manifest.permission.INTERNET)
          && AVManifestUtils.checkPermission(context, android.Manifest.permission.READ_PHONE_STATE)
          && AVManifestUtils.checkPermission(context, android.Manifest.permission.ACCESS_NETWORK_STATE)
          && AVManifestUtils.checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
          && AVManifestUtils.checkReceiver(context, AVFlymePushMessageReceiver.class);
    } catch (Exception e) {
    }
    return result;
  }

  private static void printErrorLog(String error) {
    if (!StringUtil.isEmpty(error)) {
      LOGGER.e(error);
    }
  }
}
