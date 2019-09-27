package cn.leancloud.push.lite;

import android.content.Context;
import android.os.Handler;

import cn.leancloud.push.lite.rest.AVHttpClient;
import cn.leancloud.push.lite.utils.AVPersistenceUtils;
import cn.leancloud.push.lite.utils.AVUtils;
import cn.leancloud.push.lite.utils.StringUtil;

public class AVOSCloud {
  public enum REGION {
    EastChina, NorthChina, NorthAmerica
  }

  public static final int LOG_LEVEL_VERBOSE = 1 << 1;
  public static final int LOG_LEVEL_DEBUG = 1 << 2;
  public static final int LOG_LEVEL_INFO = 1 << 3;
  public static final int LOG_LEVEL_WARNING = 1 << 4;
  public static final int LOG_LEVEL_ERROR = 1 << 5;
  public static final int LOG_LEVEL_NONE = 1 << 16;

  private static final String SDK_VERSION = "6.0.0";
  private static final String DEFAULT_USER_AGENT = "LeanCloud Push(lite) SDK v" + SDK_VERSION;

  /**
   * 服务区分，注意 name 值不能随意修改修改，要根据这个值来拼 host
   * RTM is indicating router server.
   */
  enum SERVER_TYPE {
    API("api"), PUSH("push"), RTM("rtm"), STATS("stats"), ENGINE("engine");
    public final String name;

    SERVER_TYPE(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }
  }

  private static int logLevel = LOG_LEVEL_NONE;

  public static Context applicationContext = null;
  public static String applicationId = null;
  public static String clientKey = null;
  protected static Handler handler = null;

  private static REGION defaultRegion = REGION.NorthChina;

  public static final int DEFAULT_NETWORK_TIMEOUT = 15000;

  private static int networkTimeoutInMills = DEFAULT_NETWORK_TIMEOUT;

  public static void setNetworkTimeout(int timeoutInMills) {
    networkTimeoutInMills = timeoutInMills;
  }

  static void setServer(SERVER_TYPE serverType, String host) {
    PushRouterManager.setServer(serverType, host);
  }

  public static void setRegion(REGION region) {
    defaultRegion = region;
  }

  public static REGION getRegion() {
    return defaultRegion;
  }

  public static void initialize(Context context, String applicationId, String clientKey) {
    if (handler == null && !AVUtils.isMainThread()) {
      throw new IllegalStateException("Please call AVOSCloud.initialize in main thread.");
    }
    if (null == context || StringUtil.isEmpty(applicationId) || StringUtil.isEmpty(clientKey)) {
      throw new IllegalArgumentException("Parameter(context or applicationId or clientKey) is illegal.");
    }
    if (null != AVOSCloud.applicationContext) {
      if (applicationId.equals(AVOSCloud.applicationId) && clientKey.equals(AVOSCloud.clientKey)) {
        // ignore duplicated init.
        return;
      } else {
        throw new IllegalStateException("Can't initialize more than once.");
      }
    }
    AVOSCloud.applicationId = applicationId;
    AVOSCloud.clientKey = clientKey;
    AVOSCloud.applicationContext = context;

    if (handler == null) {
      AVOSCloud.handler = new Handler();
    }
    new Thread(new Runnable() {
      @Override
      public void run() {
        initialize();
      }
    }).start();
  }

  private static void initialize() {
    AVPersistenceUtils.initAppInfo(applicationId, applicationContext);

    final PushRouterManager pushRouterManager = PushRouterManager.getInstance();
    pushRouterManager.fetchRouter(false, new AVCallback<Void>() {
      @Override
      protected boolean mustRunOnUIThread() {
        return false;
      }
      @Override
      protected void internalDone0(Void o, AVException avException) {
        String pushAPIServer = pushRouterManager.getPushAPIServer();
        String pushRouterServer = pushRouterManager.getPushRouterServer();

        AVHttpClient.getInstance().initialize(pushAPIServer, pushRouterServer);
      }
    });
  }

  public static String getApplicationId() {
    return applicationId;
  }

  public static Context getContext() {
    return applicationContext;
  }

  public static int getLogLevel() {
    return logLevel;
  }

  public static void setLogLevel(int logLevel) {
    AVOSCloud.logLevel = logLevel;
  }

  // for compatible with old version.
  public static void setDebugLogEnabled(boolean enable) {
    if (enable) {
      setLogLevel(LOG_LEVEL_DEBUG);
    } else {
      setLogLevel(LOG_LEVEL_INFO);
    }
  }

  // for compatible with old version.
  public static boolean isDebugLogEnabled() {
    return logLevel <= LOG_LEVEL_DEBUG;
  }

  // for compatible with old version.
  public static boolean showInternalDebugLog() {
    return isDebugLogEnabled();
  }

  public static String getUserAgent(){
    return DEFAULT_USER_AGENT;
  }
}
