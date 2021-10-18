package cn.leancloud;

import java.lang.reflect.Method;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cn.leancloud.core.LeanCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.core.GeneralRequestSignature;
import cn.leancloud.logging.Log4jAdapter;
import cn.leancloud.utils.StringUtil;
import org.apache.commons.codec.binary.Hex;

import cn.leancloud.impl.EnvFirstAppRouter;

public class LeanEngine {

  static volatile boolean httpsRedirectionEnabled = false;

  static EngineAppConfiguration appConf;

  private static EnvFirstAppRouter appRouter;

  static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";


  /**
   * <p>
   * Authenticates this client as belonging to your application. This must be called before your
   * application can use the AVOSCloud library. The recommended way is to put a call to
   * LeanEngine.initialize in each of your onCreate methods.
   * </p>
   * 
   * @param applicationId The application id provided in the AVOSCloud dashboard.
   * @param clientKey The client key provided in the AVOSCloud dashboard.
   * @param masterKey The master key provided in the AVOSCloud dashboard.
   * @param hookKey lean engine hook key.
   */
  public static void initialize(String applicationId, String clientKey, String masterKey, String hookKey) {
    String androidKey = EngineAppConfiguration.getEnvOrProperty(EngineAppConfiguration.SYSTEM_ATTR_ANDX_KEY);
    initialize(applicationId, clientKey, masterKey, hookKey, androidKey);
  }

  public static void initializeWithServerUrl(String applicationId, String clientKey, String masterKey, String hookKey,
                                String serverUrl) {
    initializeWithServerUrl(applicationId, clientKey, masterKey, hookKey, null, serverUrl);
  }

  protected static void initializeWithServerUrl(String applicationId, String clientKey, String masterKey, String hookKey,
                                   String androidxKey, String serverUrl) {
    LeanCloud.setLogLevel(LCLogger.Level.ALL); // let log4j make decision.
    AppConfiguration.setLogAdapter(new Log4jAdapter());
    AppConfiguration.setEnableLocalCache(false);
    AppConfiguration.setIncognitoMode(true);   // always ignore current user's sessionToken in requests.
    if (StringUtil.isEmpty(serverUrl)) {
      LeanCloud.initialize(applicationId, clientKey);
    } else {
      LeanCloud.initialize(applicationId, clientKey, serverUrl);
    }
    if (!StringUtil.isEmpty(hookKey)) {
      LeanCloud.setHookKey(hookKey);
    }
    // always remove current user cache.
    LCUser.changeCurrentUser(null, true);

    Map<String, String> affiliatedKeys = null;
    if (!StringUtil.isEmpty(androidxKey)) {
      affiliatedKeys = new HashMap<>();
      affiliatedKeys.put(PlatformRequestAuthentication.ANDROID_AFFILIATED_SUFFIX, androidxKey);
    }
    appConf = EngineAppConfiguration.instance(applicationId, clientKey, masterKey, affiliatedKeys);
    appRouter = new EnvFirstAppRouter();
    appRouter.fetchServerHostsInBackground(applicationId).blockingSingle();
  }

  protected static void initialize(String applicationId, String clientKey, String masterKey, String hookKey,
                                   String androidxKey) {
    initializeWithServerUrl(applicationId, clientKey, masterKey, hookKey, androidxKey, null);
  }

  private static Map<String, EngineHandlerInfo> funcs = new HashMap<String, EngineHandlerInfo>();

  private static EngineSessionCookie sessionCookie;

  /**
   * 请在ServletContextListener.contextInitialized中注册所有的云函数定义类
   * 
   * @param clazz 需要注册的云函数定义类
   */
  public static void register(Class<?> clazz) {
    for (Method m : clazz.getDeclaredMethods()) {
      EngineFunction func = m.getAnnotation(EngineFunction.class);
      if (func != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, func);
        if (info != null) {

          funcs.put(info.getEndPoint(), info);
        }
        continue;
      }
      EngineHook hook = m.getAnnotation(EngineHook.class);
      if (hook != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, hook);
        if (info != null) {
          funcs.put(info.getEndPoint(), info);
        }
      }

      IMHook imHook = m.getAnnotation(IMHook.class);
      if (imHook != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, imHook);
        if (info != null) {
          funcs.put(info.getEndPoint(), info);
        }
      }
    }
  }

  static EngineHandlerInfo getHandler(String key) {
    return funcs.get(key);
  }

  /**
   * 设置sessionCookie的实例
   * 
   * @param sessionCookie sessionCookie
   */

  public static void addSessionCookie(EngineSessionCookie sessionCookie) {
    LeanEngine.sessionCookie = sessionCookie;
  }

  public static EngineSessionCookie getSessionCookie() {
    return sessionCookie;
  }

  /**
   * 本方法用于本地调试期间，设置为 true 后所有的云函数调用都直接调用本地而非 LeanCloud 上已经部署的项目
   * 
   * @param enabled true 为调用本地云函数; false 为调用服务端云函数
   */
  public static void setLocalEngineCallEnabled(boolean enabled) {
    appRouter.setLocalEngineCallEnabled(enabled);
  }

  /**
   * 设置是否打开 HTTPS 自动跳转
   * 
   * @param enabled true 为打开 HTTPS 自动跳转
   */
  public static void setHttpsRedirectEnabled(boolean enabled) {
    httpsRedirectionEnabled = enabled;
  }

  public static String hmacSha1(String value, String key) {
    try {
      byte[] keyBytes = key.getBytes("UTF-8");
      SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(signingKey);
      byte[] rawHmac = mac.doFinal(value.getBytes());
      byte[] hexBytes = new Hex().encode(rawHmac);
      return new String(hexBytes, "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 设置在与 LeanCloud 服务器进行沟通的时候是否使用 masterKey
   * 
   * 使用 masterKey 时， API 将拥有全部权限，不再受到权限的限制
   * 
   * @param useMasterKey true 为使用 masterKey 发送请求
   */
  public static void setUseMasterKey(boolean useMasterKey) {
    if (useMasterKey) {
      GeneralRequestSignature.setMasterKey(appConf.getMasterKey());
    } else {
      GeneralRequestSignature.setMasterKey(null);
    }
  }

  protected static Set<String> getMetaData() {
    return funcs.keySet();
  }

  public static String getAppId() {
    return appConf.getApplicationId();
  }

  public static String getAppKey() {
    return appConf.getClientKey();
  }

  public static String getMasterKey() {
    return appConf.getMasterKey();
  }

  public static String getAndroidKey() {
    Map<String, String> affiliatedKeys = appConf.getAffiliatedKeys();
    if (null == affiliatedKeys) {
      return null;
    }
    if (affiliatedKeys.containsKey(PlatformRequestAuthentication.ANDROID_AFFILIATED_SUFFIX)) {
      return affiliatedKeys.get(PlatformRequestAuthentication.ANDROID_AFFILIATED_SUFFIX);
    } else {
      return null;
    }
  }

  public static String getAppEnv() {
    return appConf.getAppEnv();
  }

}
