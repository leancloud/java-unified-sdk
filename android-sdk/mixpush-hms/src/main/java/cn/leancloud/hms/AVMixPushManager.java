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
import com.huawei.hms.support.api.push.service.HmsMsgService;

import cn.leancloud.AVException;
import cn.leancloud.AVHMSMessageService;
import cn.leancloud.AVInstallation;
import cn.leancloud.AVLogger;
import cn.leancloud.AVManifestUtils;
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

  public static final String MIXPUSH_PROFILE = "deviceProfile";

  /**
   * 华为推送的 deviceProfile
   */
  public static String hwDeviceProfile = "";
  static Class hwMessageServiceClazz = AVHMSMessageService.class;

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

  private static boolean checkHuaweiManifest(Context context) {
    boolean result = false;
    try {
      result = AVManifestUtils.checkPermission(context, android.Manifest.permission.INTERNET)
          && AVManifestUtils.checkService(context, hwMessageServiceClazz);
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
