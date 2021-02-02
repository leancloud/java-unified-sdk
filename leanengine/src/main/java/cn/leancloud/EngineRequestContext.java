package cn.leancloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 在云代码函数中获取请求相关的额外属性
 *
 * @author lbt05
 *
 */
public class EngineRequestContext {
  public static final String ATTRIBUTE_KEY_AUTHENTICATION = "requestAuth";
  public static final String ATTRIBUTE_KEY_SESSION_TOKEN = "userSessionToken";

  private static final String UPDATED_KEYS = "_updatedKeys";
  private static final String REMOTE_ADDRESS = "_remoteAddress";
  private static final String SESSION_TOKEN = "_sessionToken";
  private static final String AUTHENTICATED_USER = "_authenticatedUser";
  private static final String BEFORE_KEYS = "__before";
  private static final String AFTER_KEYS = "__after";
  static ThreadLocal<Map<String, Object>> localMeta = new ThreadLocal<Map<String, Object>>();

  @Deprecated
  public static Map<String, Object> getMeta() {
    return localMeta.get();
  }

  /**
   * 在 beforeUpdate 函数中调用可以查看 avobject 的哪些属性被更新了
   * 
   * @return 被更新的属性
   */
  public static List<String> getUpdateKeys() {
    return (List) get(UPDATED_KEYS);
  }

  /**
   * 获取发起请求的 IP 地址
   * 
   * @return 发起请求的 IP 地址
   */
  public static String getRemoteAddress() {
    return (String) get(REMOTE_ADDRESS);
  }

  protected static void parseMetaData(Map<String, Object> objectProperties) {
    Map<String, Object> meta = new HashMap<String, Object>();
    if (objectProperties == null) {
      return;
    }
    if (objectProperties.containsKey(UPDATED_KEYS)) {
      Object updateValues = objectProperties.remove(UPDATED_KEYS);
      meta.put(UPDATED_KEYS, updateValues);
    }
    if (objectProperties.containsKey(BEFORE_KEYS)) {
      Object beforeValues = objectProperties.remove(BEFORE_KEYS);
      meta.put(BEFORE_KEYS, beforeValues);
    }

    if (objectProperties.containsKey(AFTER_KEYS)) {
      Object afterValues = objectProperties.remove(AFTER_KEYS);
      meta.put(AFTER_KEYS, afterValues);
    }

    Map<String, Object> existingMeta = localMeta.get();
    if (existingMeta != null) {
      existingMeta.clear();
      existingMeta.putAll(meta);
    } else {
      localMeta.set(meta);
    }
  }

  protected static void setRemoteAddress(String ip) {
    put(REMOTE_ADDRESS, ip);
  }

  public static void setSessionToken(String sessionToken) {
    put(SESSION_TOKEN, sessionToken);
  }

  /**
   * get authenticated user's session token of current request.
   * @return session token
   *
   * keep it just for compatible with old code.
   */
  public static String getSessionToken() {
    return (String) get(SESSION_TOKEN);
  }

  /**
   * set current authenticated user.
   * @param currentUser current user.
   */
  public static void setAuthenticatedUser(AVUser currentUser) {
    put(AUTHENTICATED_USER, currentUser);
  }

  /**
   * get authenticated user of current request.
   * @return AVUser instance
   *
   * keep it just for compatible with old code.
   */
  public static AVUser getAuthenticatedUser() {
    return (AVUser) get(AUTHENTICATED_USER);
  }

  public static void clean() {
    localMeta.set(null);
  }

  protected static void put(String key, Object value) {
    Map<String, Object> meta = localMeta.get();
    if (meta == null) {
      meta = new HashMap<>();
      localMeta.set(meta);
    }
    meta.put(key, value);
  }

  protected static Object get(String key) {
    Map<String, Object> meta = localMeta.get();
    if (meta != null && meta.containsKey(key)) {
      return meta.get(key);
    }
    return null;
  }

}
