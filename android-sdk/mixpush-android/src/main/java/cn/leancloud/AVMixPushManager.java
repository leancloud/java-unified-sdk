package cn.leancloud;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Looper;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.push.HmsMessaging;
import com.huawei.hms.support.api.push.service.HmsMsgService;

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
  static Class miPushReceiverClazz = AVMiPushMessageReceiver.class;

  /**
   * 华为推送的 deviceProfile
   */
  static String hwDeviceProfile = "";
  static Class hwMessageServiceClazz = AVHMSMessageService.class;

  /**
   * 魅族推送的 deviceProfile
   */
  static String flymeDeviceProfile = "";
  static int flymeMStatusBarIcon = 0;
  static Class flymePushReceiverClazz = AVFlymePushMessageReceiver.class;

  static String vivoDeviceProfile = "";
  static String oppoDeviceProfile = "";

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
    AVMiPushMessageReceiver.setInternationalVendor(isInternationalVendor);
    registerXiaomiPush(context, miAppId, miAppKey, profile, customizedReceiver);
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   *
   * @param application 应用实例
   */
  public static void registerHMSPush(Application application) {
    registerHMSPush(application, "");
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   *
   * @param application 应用实例
   * @param profile 华为推送配置
   */
  public static void registerHMSPush(Application application, String profile) {
    registerHMSPush(application, profile, null);
  }

  public static void registerHMSPush(Application application, String profile, Class customMessageServiceClazz) {
    if (null == application) {
      throw new IllegalArgumentException("[HMS] context cannot be null.");
    }

    if (!isHuaweiPhone()) {
      printErrorLog("[HMS] register error, is not huawei phone!");
      return;
    }

    if (null != customMessageServiceClazz) {
      hwMessageServiceClazz = customMessageServiceClazz;
    }

    if (!checkHuaweiManifest(application)) {
      printErrorLog("[HMS] register error, mainifest is incomplete!");
      return;
    }

    hwDeviceProfile = profile;

    LOGGER.d("[HMS] start register HMS push");
  }

  /**
   *  Connecting to the HMS SDK may pull up the activity (including upgrade guard, etc.), and it is
   *  recommended that you connect in the first activity.
   *  This method can be called repeatedly, and there is no need to do complex processing
   *  for only one call at a time
   *  Method is called asynchronously, and the result is invoked in the main thread callback
   *
   * @param activity activity
   */
  public static void connectHMS(Activity activity) {
    if (null == activity) {
      throw new IllegalArgumentException("[HMS] activity cannot be null.");
    }
    String appId = AGConnectServicesConfig.fromContext(activity).getString("client/app_id");
    connectHMS(activity, appId);
  }

  /**
   *  Connecting to the HMS SDK may pull up the activity (including upgrade guard, etc.), and it is
   *  recommended that you connect in the first activity.
   *  This method can be called repeatedly, and there is no need to do complex processing
   *  for only one call at a time
   *  Method is called asynchronously, and the result is invoked in the main thread callback
   *
   * @param activity activity
   * @param huaweiAppId huawei app id
   */
  public static void connectHMS(Activity activity, String huaweiAppId) {
    if (null == activity) {
      throw new IllegalArgumentException("[HMS] activity cannot be null.");
    }
    if (Looper.getMainLooper() == Looper.myLooper()) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            String token = HmsInstanceId.getInstance(activity).getToken(huaweiAppId, HmsMessaging.DEFAULT_TOKEN_SCOPE);
            LOGGER.d("found HMS appId: " + huaweiAppId + ", token: " + token);
            AVHMSMessageService.updateAVInstallation(token);
          } catch (Exception ex) {
            LOGGER.w("failed to get hms token. cause: " + ex.getMessage());
          }
        }
      }).start();
    } else {
      try {
        String token = HmsInstanceId.getInstance(activity).getToken(huaweiAppId, HmsMessaging.DEFAULT_TOKEN_SCOPE);
        LOGGER.d("found HMS appId: " + huaweiAppId + ", token: " + token);
        AVHMSMessageService.updateAVInstallation(token);
      } catch (Exception ex) {
        LOGGER.w("failed to get hms token. cause: " + ex.getMessage());
      }
    }

  }

  /**
   * 开启华为 HMS 推送
   *
   * @param context context
   * @param callback callback function
   */
  public static void turnOnHMSPush(Context context, AVCallback<Void> callback) {
    HmsMessaging.getInstance(context).turnOnPush().addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(Task<Void> task) {
        if (task.isSuccessful()) {
          callback.internalDone(null);
        } else {
          callback.internalDone(new AVException(task.getException()));
        }
      }
    });
  }

  /**
   * 关闭华为 HMS 推送
   *
   * @param context context
   * @param callback callback function
   */
  public static void turnOffHMSPush(Context context, AVCallback<Void> callback) {
    HmsMessaging.getInstance(context).turnOffPush().addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(Task<Void> task) {
        if (task.isSuccessful()) {
          callback.internalDone(null);
        } else {
          callback.internalDone(new AVException(task.getException()));
        }
      }
    });
  }

  /**
   * 注册魅族推送
   * @param context 上下文
   * @param flymeId flyme app id
   * @param flymeKey flyme app key
   * @param profile 魅族推送配置
   * @return true - register succeed
   *         false - register failed
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey,
                                          String profile) {
    return registerFlymePush(context, flymeId, flymeKey, profile, null);
  }

  /**
   * 注册魅族推送
   * @param context 上下文
   * @param flymeId flyme app id
   * @param flymeKey flyme app key
   * @param profile 魅族推送配置
   * @param customizedReceiver 自定义 receiver
   * @return true - register succeed
   *         false - register failed
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey,
                                          String profile, Class customizedReceiver) {
    if (null == context) {
      printErrorLog("register error, context is null!");
      return false;
    }
    boolean result = false;
    if (!com.meizu.cloud.pushsdk.util.MzSystemUtils.isBrandMeizu(context)) {
      printErrorLog("register error, is not flyme phone!");
    } else {
      if (null != customizedReceiver) {
        flymePushReceiverClazz = customizedReceiver;
      }
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
   * @param context 上下文
   * @param flymeId flyme app id
   * @param flymeKey flyme app key
   * @return true - register succeed
   *         false - register failed
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey) {
    return registerFlymePush(context, flymeId, flymeKey, "", null);
  }

  /**
   * 注册魅族推送
   * @param context 上下文
   * @param flymeId flyme app id
   * @param flymeKey flyme app key
   * @param customizedReceiver 自定义 receiver
   * @return true - register succeed
   *         false - register failed
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey,
                                          Class customizedReceiver) {
    return registerFlymePush(context, flymeId, flymeKey, "", customizedReceiver);
  }

  /**
   * set flyme MStatus bar icon.
   *
   * @param icon icon resource id.
   */
  public static void setFlymeMStatusbarIcon(int icon) {
    flymeMStatusBarIcon = icon;
  }

  /**
   * VIVO push
   */

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application application
   */
  public static boolean registerVIVOPush(Application application) {
    return AVMixPushManager.registerVIVOPush(application, "");
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application application
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
   * @param context context
   * @param appKey oppo application key
   * @param appSecret oppo application secret
   * @param callback callback
   * @return
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         AVOPPOPushAdapter callback) {
    if (!isSupportOppoPush(context)) {
      return false;
    }
    com.heytap.mcssdk.PushManager.getInstance().register(context, appKey, appSecret, callback);
    return true;
  }

  /**
   * register oppo push
   * @param context context
   * @param appKey oppo application key
   * @param appSecret oppo application secret
   * @param profile profile string.
   * @param callback callback.
   * @return
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         String profile,
                                         AVOPPOPushAdapter callback) {
    oppoDeviceProfile = profile;
    return registerOppoPush(context, appKey, appSecret, callback);
  }


  /**
   * judgement if support oppo push or not.
   *
   * @param context
   * @return
   */
  public static boolean isSupportOppoPush(Context context) {
    return com.heytap.mcssdk.PushManager.isSupportPush(context);
  }

  /**
   * pause oppo push
   */
  public static void pauseOppoPush() {
    com.heytap.mcssdk.PushManager.getInstance().pausePush();
  }

  /**
   * resume oppo push
   */
  public static void resumeOppoPush() {
    com.heytap.mcssdk.PushManager.getInstance().resumePush();
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
    com.heytap.mcssdk.PushManager.getInstance().setPushTime(weekDays, startHour, startMinute,
        endHour, endMinute);
  }

  /**
   * set oppo push aliases.
   * @param aliases
   */
  public static void setOppoAliases(List<String> aliases) {
    com.heytap.mcssdk.PushManager.getInstance().setAliases(aliases);
  }

  /**
   * unset oppo push aliases.
   * @param alias
   */
  public static void unsetOppoAlias(String alias) {
    com.heytap.mcssdk.PushManager.getInstance().unsetAlias(alias);
  }

  /**
   * get oppo aliases.
   */
  public static void getOppoAliases() {
    com.heytap.mcssdk.PushManager.getInstance().getAliases();
  }

  /**
   * set oppo push account.
   * @param account
   */
  public static void setOppoUserAccount(String account) {
    com.heytap.mcssdk.PushManager.getInstance().setUserAccount(account);
  }

  /**
   * unset oppo push accounts.
   * @param accounts
   */
  public static void unsetOppoUserAccouts(List<String> accounts) {
    com.heytap.mcssdk.PushManager.getInstance().unsetUserAccounts(accounts);
  }

  /**
   * get oppo push accounts.
   */
  public static void getOppoUserAccounts() {
    com.heytap.mcssdk.PushManager.getInstance().getUserAccounts();
  }

  /**
   * set oppo push tags.
   * @param tags
   */
  public static void setOppoTags(List<String> tags) {
    com.heytap.mcssdk.PushManager.getInstance().setTags(tags);
  }

  /**
   * unset oppo push tags.
   * @param tags
   */
  public static void unsetOppoTags(List<String> tags) {
    com.heytap.mcssdk.PushManager.getInstance().unsetTags(tags);
  }

  /**
   * retrieve oppo push tags.
   */
  public static void getOppoTags() {
    com.heytap.mcssdk.PushManager.getInstance().getTags();
  }

  /**
   * get oppo push status
   */
  public static void getOppoPushStatus() {
    com.heytap.mcssdk.PushManager.getInstance().getPushStatus();
  }

  /**
   * get oppo notification status.
   */
  public static void getOppoNotificationStatus() {
    com.heytap.mcssdk.PushManager.getInstance().getNotificationStatus();
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
      return AVManifestUtils.checkReceiver(context, miPushReceiverClazz);
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
          && AVManifestUtils.checkService(context, HmsMsgService.class)
          && AVManifestUtils.checkService(context, hwMessageServiceClazz);
    } catch (Exception e) {
      LOGGER.d(e.getMessage());
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
          && AVManifestUtils.checkReceiver(context, flymePushReceiverClazz);
    } catch (Exception e) {
      LOGGER.d(e.getMessage());
    }
    return result;
  }

  private static void printErrorLog(String error) {
    if (!StringUtil.isEmpty(error)) {
      LOGGER.e(error);
    }
  }
}
