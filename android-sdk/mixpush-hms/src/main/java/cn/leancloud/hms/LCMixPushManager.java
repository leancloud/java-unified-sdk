package cn.leancloud.hms;

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

import cn.leancloud.LCException;
import cn.leancloud.LCHMSMessageService;
import cn.leancloud.LCInstallation;
import cn.leancloud.LCLogger;
import cn.leancloud.LCManifestUtils;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 16/6/27.
 */
public class LCMixPushManager {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCMixPushManager.class);

  public static final String MIXPUSH_PROFILE = "deviceProfile";

  /**
   * 华为推送的 deviceProfile
   */
  public static String hwDeviceProfile = "";
  static Class hwMessageServiceClazz = LCHMSMessageService.class;

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
            LCHMSMessageService.updateAVInstallation(token);
          } catch (Exception ex) {
            LOGGER.w("failed to get hms token. cause: " + ex.getMessage());
          }
        }
      }).start();
    } else {
      try {
        String token = HmsInstanceId.getInstance(activity).getToken(huaweiAppId, HmsMessaging.DEFAULT_TOKEN_SCOPE);
        LOGGER.d("found HMS appId: " + huaweiAppId + ", token: " + token);
        LCHMSMessageService.updateAVInstallation(token);
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
  public static void turnOnHMSPush(Context context, LCCallback<Void> callback) {
    HmsMessaging.getInstance(context).turnOnPush().addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(Task<Void> task) {
        if (task.isSuccessful()) {
          callback.internalDone(null);
        } else {
          callback.internalDone(new LCException(task.getException()));
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
  public static void turnOffHMSPush(Context context, LCCallback<Void> callback) {
    HmsMessaging.getInstance(context).turnOffPush().addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(Task<Void> task) {
        if (task.isSuccessful()) {
          callback.internalDone(null);
        } else {
          callback.internalDone(new LCException(task.getException()));
        }
      }
    });
  }

  /**
   * 取消混合推送的注册
   * 取消成功后，消息会通过 LeanCloud websocket 发送
   */
  public static void unRegisterMixPush() {
    LCInstallation installation = LCInstallation.getCurrentInstallation();
    String vendor = installation.getString(LCInstallation.VENDOR);
    if (!StringUtil.isEmpty(vendor)) {
      installation.put(LCInstallation.VENDOR, "lc");
      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(LCException e) {
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

  private static boolean checkHuaweiManifest(Context context) {
    boolean result = false;
    try {
      result = LCManifestUtils.checkPermission(context, android.Manifest.permission.INTERNET)
          && LCManifestUtils.checkService(context, hwMessageServiceClazz);
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
