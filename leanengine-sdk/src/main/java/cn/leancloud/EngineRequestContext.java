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

  private static final String UPDATED_KEYS = "_updatedKeys";
  private static final String REMOTE_ADDRESS = "_remoteAddress";
  private static final String BEFORE_KEYS = "__before";
  private static final String AFTER_KEYS = "__after";
  static ThreadLocal<Map<String, Object>> localMeta = new ThreadLocal<Map<String, Object>>();

  public static Map<String, Object> getMeta() {
    return localMeta.get();
  }

  /**
   * 在 beforeUpdate 函数中调用可以查看 avobject 的哪些属性被更新了
   * 
   * @return 被更新的属性
   */
  public static List<String> getUpdateKeys() {
    Map<String, Object> meta = getMeta();
    if (meta != null && meta.containsKey(UPDATED_KEYS)) {
      return (List) meta.get(UPDATED_KEYS);
    }
    return null;
  }

  /**
   * 获取发起请求的 IP 地址
   * 
   * @return 发起请求的 IP 地址
   */
  public static String getRemoteAddress() {
    Map<String, Object> meta = getMeta();
    if (meta != null && meta.containsKey(REMOTE_ADDRESS)) {
      return (String) meta.get(REMOTE_ADDRESS);
    }
    return null;
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
      existingMeta.putAll(meta);
    } else {
      localMeta.set(meta);
    }
  }

  protected static void setRemoteAddress(String ip) {
    Map<String, Object> existingMeta = localMeta.get();
    if (existingMeta != null) {
      existingMeta.put(REMOTE_ADDRESS, ip);
    } else {
      Map<String, Object> meta = new HashMap<String, Object>();
      meta.put(REMOTE_ADDRESS, ip);
      localMeta.set(meta);
    }
  }

  public static void clean() {
    localMeta.set(null);
  }
}
