package cn.leancloud.utils;

import cn.leancloud.AVLogAdapter;
import cn.leancloud.AVLogger;
import cn.leancloud.core.AVOSCloud;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogUtil {
  private static Map<String, Object> loggerCache = new ConcurrentHashMap<String, Object>();

  public static AVLogger getLogger(Class clazz) {
    AVLogAdapter adapter = AVOSCloud.getLogAdapter();
    if (null == adapter) {
      return null;
    } else {
      return adapter.getLogger(clazz);
    }
  }
}
