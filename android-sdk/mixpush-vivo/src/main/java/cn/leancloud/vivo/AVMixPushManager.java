package cn.leancloud.vivo;

import android.app.Application;
import android.content.Context;

import java.util.List;

import cn.leancloud.AVException;
import cn.leancloud.AVInstallation;
import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
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

  public static String vivoDeviceProfile = "";

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

  private static void printErrorLog(String error) {
    if (!StringUtil.isEmpty(error)) {
      LOGGER.e(error);
    }
  }
}
