package cn.leancloud;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import java.util.List;

import cn.leancloud.callback.AVCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 16/6/27.
 */
public class AVMixPushManager {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVMixPushManager.class);

  static final String MIXPUSH_PROFILE = "deviceProfile";

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
  static String flymeDeviceProfile = "";
  static int flymeMStatusBarIcon = 0;

  static String vivoDeviceProfile = "";
  static String oppoDeviceProfile = "";

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
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
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
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
        flymeDeviceProfile = profile;
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
   * VIVO push
   */

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application
   */
  public static boolean registerVIVOPush(Application application) {
    return AVMixPushManager.registerVIVOPush(application, "");
  }
  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application
   */
  public static boolean registerVIVOPush(Application application, String profile) {
    vivoDeviceProfile = profile;
    com.vivo.push.PushClient client = com.vivo.push.PushClient.getInstance(application.getApplicationContext());
    try {
      client.checkManifest();
      client.initialize();
      return true;
    } catch (com.vivo.push.util.VivoPushException ex) {
      printErrorLog("register error, mainifest is incomplete! details=" + ex.getMessage());
      return false;
    }
  }

  /**
   * turn off VIVO push.
   */
  public static void turnOffVIVOPush(final AVCallback<Boolean> callback) {
    com.vivo.push.PushClient.getInstance(AVOSCloud.getContext()).turnOffPush(new com.vivo.push.IPushActionListener() {
      public void onStateChanged(int state) {
        if (null == callback) {
          AVException exception = null;
          if (0 != state) {
            exception = new AVException(AVException.UNKNOWN, "VIVO server internal error, state=" + state);
          }
          callback.internalDone(null == exception, exception);
        }
      }
    });
  }

  /**
   * turn on VIVO push.
   */
  public static void turnOnVIVOPush(final AVCallback<Boolean> callback) {
    com.vivo.push.PushClient.getInstance(AVOSCloud.getContext()).turnOnPush(new com.vivo.push.IPushActionListener() {
      public void onStateChanged(int state) {
        if (null == callback) {
          AVException exception = null;
          if (0 != state) {
            exception = new AVException(AVException.UNKNOWN, "VIVO server internal error, state=" + state);
          }
          callback.internalDone(null == exception, exception);
        }
      }
    });
  }

  /**
   * current device support VIVO push or not.
   *
   * @param context
   * @return
   */
  public static boolean isSupportVIVOPush(Context context) {
    com.vivo.push.PushClient client = com.vivo.push.PushClient.getInstance(context);
    if (null == client) {
      return false;
    }
    return client.isSupport();
  }

  /**
   * bind vivo alias
   *
   * @param context
   * @param alias
   * @param callback
   */
  public static void bindVIVOAlias(Context context, String alias, final AVCallback<Boolean> callback) {
    if (null == context) {
      if (null != callback) {
        callback.internalDone(false, new AVException(AVException.VALIDATION_ERROR, "context is null"));
      }
    } else {
      com.vivo.push.PushClient.getInstance(context).bindAlias(alias, new com.vivo.push.IPushActionListener() {
        public void onStateChanged(int state) {
          if (null == callback) {
            AVException exception = null;
            if (0 != state) {
              exception = new AVException(AVException.UNKNOWN, "VIVO server internal error, state=" + state);
            }
            callback.internalDone(null == exception, exception);
          }
        }
      });
    }
  }

  /**
   * unbind vivo alias
   *
   * @param context
   * @param alias
   * @param callback
   */
  public static void unbindVIVOAlias(Context context, String alias, final AVCallback<Boolean> callback) {
    if (null == context) {
      if (null != callback) {
        callback.internalDone(false, new AVException(AVException.VALIDATION_ERROR, "context is null"));
      }
    } else {
      com.vivo.push.PushClient.getInstance(context).unBindAlias(alias, new com.vivo.push.IPushActionListener() {
        public void onStateChanged(int state) {
          if (null == callback) {
            AVException exception = null;
            if (0 != state) {
              exception = new AVException(AVException.UNKNOWN, "VIVO server internal error, state=" + state);
            }
            callback.internalDone(null == exception, exception);
          }
        }
      });
    }
  }

  /**
   * get vivo alias
   *
   * @param context
   * @return
   */
  public static String getVIVOAlias(Context context) {
    if (null == context) {
      return null;
    }
    return com.vivo.push.PushClient.getInstance(context).getAlias();
  }

  /**
   * set vivo topic
   *
   * @param context
   * @param topic
   * @param callback
   */
  public static void setVIVOTopic(Context context, String topic, final AVCallback<Boolean> callback) {
    if (null == context) {
      if (null != callback) {
        callback.internalDone(false, new AVException(AVException.VALIDATION_ERROR, "context is null"));
      }
    } else {
      com.vivo.push.PushClient.getInstance(context).setTopic(topic, new com.vivo.push.IPushActionListener() {
        public void onStateChanged(int state) {
          if (null == callback) {
            AVException exception = null;
            if (0 != state) {
              exception = new AVException(AVException.UNKNOWN, "VIVO server internal error, state=" + state);
            }
            callback.internalDone(null == exception, exception);
          }
        }
      });
    }
  }

  /**
   * delete vivo topic
   * @param context
   * @param alias
   * @param callback
   */
  public static void delVIVOTopic(Context context, String alias, final AVCallback<Boolean> callback) {
    if (null == context) {
      if (null != callback) {
        callback.internalDone(false, new AVException(AVException.VALIDATION_ERROR, "context is null"));
      }
    } else {
      com.vivo.push.PushClient.getInstance(context).delTopic(alias, new com.vivo.push.IPushActionListener() {
        public void onStateChanged(int state) {
          if (null == callback) {
            AVException exception = null;
            if (0 != state) {
              exception = new AVException(AVException.UNKNOWN, "VIVO server internal error, state=" + state);
            }
            callback.internalDone(null == exception, exception);
          }
        }
      });
    }
  }

  /**
   * get vivo topics
   * @param context
   * @return
   */
  public static List<String> getVIVOTopics(Context context) {
    if (null == context) {
      return null;
    }
    return com.vivo.push.PushClient.getInstance(context).getTopics();
  }

  /**
   * Oppo push
   */

  /**
   * register Oppo Push.
   *
   * @param context
   * @param appKey
   * @param appSecret
   * @param callback
   * @return
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         AVOPPOPushAdapter callback) {
    if (!isSupportOppoPush(context)) {
      return false;
    }
    com.coloros.mcssdk.PushManager.getInstance().register(context, appKey, appSecret, callback);
    return true;
  }

  /**
   * judgement if support oppo push or not.
   *
   * @param context
   * @return
   */
  public static boolean isSupportOppoPush(Context context) {
    return com.coloros.mcssdk.PushManager.isSupportPush(context);
  }

  /**
   * pause oppo push
   */
  public static void pauseOppoPush() {
    com.coloros.mcssdk.PushManager.getInstance().pausePush();
  }

  /**
   * resume oppo push
   */
  public static void resumeOppoPush() {
    com.coloros.mcssdk.PushManager.getInstance().resumePush();
  }

  /**
   * set oppo push time.
   * @param weekDays
   * @param startHour
   * @param startMinute
   * @param endHour
   * @param endMinute
   */
  public static void setOppoPushTime(List<Integer> weekDays, int startHour, int startMinute,
                                     int endHour, int endMinute) {
    com.coloros.mcssdk.PushManager.getInstance().setPushTime(weekDays, startHour, startMinute,
        endHour, endMinute);
  }

  /**
   * retrieve oppo push time.
   */
  public static void getOppoPushTime() {
    com.coloros.mcssdk.PushManager.getInstance().getPushTime();
  }

  /**
   * set oppo push aliases.
   * @param aliases
   */
  public static void setOppoAliases(List<String> aliases) {
    com.coloros.mcssdk.PushManager.getInstance().setAliases(aliases);
  }

  /**
   * unset oppo push aliases.
   * @param alias
   */
  public static void unsetOppoAlias(String alias) {
    com.coloros.mcssdk.PushManager.getInstance().unsetAlias(alias);
  }

  /**
   * get oppo aliases.
   */
  public static void getOppoAliases() {
    com.coloros.mcssdk.PushManager.getInstance().getAliases();
  }

  /**
   * set oppo push account.
   * @param account
   */
  public static void setOppoUserAccount(String account) {
    com.coloros.mcssdk.PushManager.getInstance().setUserAccount(account);
  }

  /**
   * unset oppo push accounts.
   * @param accounts
   */
  public static void unsetOppoUserAccouts(List<String> accounts) {
    com.coloros.mcssdk.PushManager.getInstance().unsetUserAccounts(accounts);
  }

  /**
   * get oppo push accounts.
   */
  public static void getOppoUserAccounts() {
    com.coloros.mcssdk.PushManager.getInstance().getUserAccounts();
  }

  /**
   * set oppo push tags.
   * @param tags
   */
  public static void setOppoTags(List<String> tags) {
    com.coloros.mcssdk.PushManager.getInstance().setTags(tags);
  }

  /**
   * unset oppo push tags.
   * @param tags
   */
  public static void unsetOppoTags(List<String> tags) {
    com.coloros.mcssdk.PushManager.getInstance().unsetTags(tags);
  }

  /**
   * retrieve oppo push tags.
   */
  public static void getOppoTags() {
    com.coloros.mcssdk.PushManager.getInstance().getTags();
  }

  /**
   * get oppo push status
   */
  public static void getOppoPushStatus() {
    com.coloros.mcssdk.PushManager.getInstance().getPushStatus();
  }

  /**
   * get oppo notification status.
   */
  public static void getOppoNotificationStatus() {
    com.coloros.mcssdk.PushManager.getInstance().getNotificationStatus();
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
