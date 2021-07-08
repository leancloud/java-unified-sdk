package cn.leancloud.vivo;

import android.app.Application;
import android.content.Context;

import java.util.List;

import cn.leancloud.LCException;
import cn.leancloud.LCInstallation;
import cn.leancloud.LCLogger;
import cn.leancloud.LeanCloud;
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

  public static String vivoDeviceProfile = "";

  /**
   * VIVO push
   */

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application application
   * @return true - succeed, false - failed.
   */
  public static boolean registerVIVOPush(Application application) {
    return LCMixPushManager.registerVIVOPush(application, "");
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application application
   * @param profile profile
   * @return true - succeed, false - failed.
   */
  public static boolean registerVIVOPush(Application application, String profile) {
    vivoDeviceProfile = profile;
    com.vivo.push.PushClient client = com.vivo.push.PushClient.getInstance(application.getApplicationContext());
    try {
      client.checkManifest();
      if (!isSupportVIVOPush(application)) {
        printErrorLog("current device doesn't support VIVO Push.");
        return false;
      }
      client.initialize();
      return true;
    } catch (com.vivo.push.util.VivoPushException ex) {
      printErrorLog("register error, mainifest is incomplete! details=" + ex.getMessage());
      return false;
    }
  }

  /**
   * turn off VIVO push.
   * @param callback callback function.
   */
  public static void turnOffVIVOPush(final LCCallback<Boolean> callback) {
    com.vivo.push.PushClient.getInstance(LeanCloud.getContext()).turnOffPush(new com.vivo.push.IPushActionListener() {
      public void onStateChanged(int state) {
        if (null == callback) {
          LCException exception = null;
          if (0 != state) {
            exception = new LCException(LCException.UNKNOWN, "VIVO server internal error, state=" + state);
          }
          callback.internalDone(null == exception, exception);
        }
      }
    });
  }

  /**
   * turn on VIVO push.
   * @param callback callback function.
   */
  public static void turnOnVIVOPush(final LCCallback<Boolean> callback) {
    com.vivo.push.PushClient.getInstance(LeanCloud.getContext()).turnOnPush(new com.vivo.push.IPushActionListener() {
      public void onStateChanged(int state) {
        if (null == callback) {
          LCException exception = null;
          if (0 != state) {
            exception = new LCException(LCException.UNKNOWN, "VIVO server internal error, state=" + state);
          }
          callback.internalDone(null == exception, exception);
        }
      }
    });
  }

  /**
   * current device support VIVO push or not.
   *
   * @param context context
   * @return true or false.
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
   * @param context context
   * @param alias alias
   * @param callback callback function
   */
  public static void bindVIVOAlias(Context context, String alias, final LCCallback<Boolean> callback) {
    if (null == context) {
      if (null != callback) {
        callback.internalDone(false, new LCException(LCException.VALIDATION_ERROR, "context is null"));
      }
    } else {
      com.vivo.push.PushClient.getInstance(context).bindAlias(alias, new com.vivo.push.IPushActionListener() {
        public void onStateChanged(int state) {
          if (null == callback) {
            LCException exception = null;
            if (0 != state) {
              exception = new LCException(LCException.UNKNOWN, "VIVO server internal error, state=" + state);
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
   * @param context context
   * @param alias alias
   * @param callback callback function
   */
  public static void unbindVIVOAlias(Context context, String alias, final LCCallback<Boolean> callback) {
    if (null == context) {
      if (null != callback) {
        callback.internalDone(false, new LCException(LCException.VALIDATION_ERROR, "context is null"));
      }
    } else {
      com.vivo.push.PushClient.getInstance(context).unBindAlias(alias, new com.vivo.push.IPushActionListener() {
        public void onStateChanged(int state) {
          if (null == callback) {
            LCException exception = null;
            if (0 != state) {
              exception = new LCException(LCException.UNKNOWN, "VIVO server internal error, state=" + state);
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
   * @param context context
   * @return alias
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
   * @param context context
   * @param topic topic
   * @param callback callback function
   */
  public static void setVIVOTopic(Context context, String topic, final LCCallback<Boolean> callback) {
    if (null == context) {
      if (null != callback) {
        callback.internalDone(false, new LCException(LCException.VALIDATION_ERROR, "context is null"));
      }
    } else {
      com.vivo.push.PushClient.getInstance(context).setTopic(topic, new com.vivo.push.IPushActionListener() {
        public void onStateChanged(int state) {
          if (null == callback) {
            LCException exception = null;
            if (0 != state) {
              exception = new LCException(LCException.UNKNOWN, "VIVO server internal error, state=" + state);
            }
            callback.internalDone(null == exception, exception);
          }
        }
      });
    }
  }

  /**
   * delete vivo topic
   * @param context context
   * @param alias alias
   * @param callback callback function
   */
  public static void delVIVOTopic(Context context, String alias, final LCCallback<Boolean> callback) {
    if (null == context) {
      if (null != callback) {
        callback.internalDone(false, new LCException(LCException.VALIDATION_ERROR, "context is null"));
      }
    } else {
      com.vivo.push.PushClient.getInstance(context).delTopic(alias, new com.vivo.push.IPushActionListener() {
        public void onStateChanged(int state) {
          if (null == callback) {
            LCException exception = null;
            if (0 != state) {
              exception = new LCException(LCException.UNKNOWN, "VIVO server internal error, state=" + state);
            }
            callback.internalDone(null == exception, exception);
          }
        }
      });
    }
  }

  /**
   * get vivo topics
   * @param context context
   * @return topic list.
   */
  public static List<String> getVIVOTopics(Context context) {
    if (null == context) {
      return null;
    }
    return com.vivo.push.PushClient.getInstance(context).getTopics();
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

  private static void printErrorLog(String error) {
    if (!StringUtil.isEmpty(error)) {
      LOGGER.e(error);
    }
  }
}
