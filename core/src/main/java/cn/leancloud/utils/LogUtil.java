package cn.leancloud.utils;

import cn.leancloud.AVLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogUtil {
  private static Map<String, AVLogger> loggerCache = new ConcurrentHashMap<>();

  public static AVLogger getLogger(Class clazz) {
    if (null == clazz) {
      return null;
    }
    if (loggerCache.containsKey(clazz.getCanonicalName())) {
      return loggerCache.get(clazz.getCanonicalName());
    }
    AVLogger ret = new AVLogger(clazz.getSimpleName());
    loggerCache.put(clazz.getCanonicalName(), ret);
    return ret;
  }
}
