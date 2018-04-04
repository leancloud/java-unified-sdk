package cn.leancloud.cache;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class LastModifyCache {
  private static LastModifyCache INSTANCE = null;
  public static LastModifyCache getInstance() {
    if (null == INSTANCE) {
      synchronized (LastModifyCache.class) {
        if (null == INSTANCE) {
          INSTANCE = new LastModifyCache();
        }
      }
    }
    return INSTANCE;
  }

  private boolean lastModifyEnabled = false;
  private Map<String, String> lastModifyMap = Collections
          .synchronizedMap(new WeakHashMap<String, String>());

  private LastModifyCache() {
  }

  public boolean isLastModifyEnabled() {
    return lastModifyEnabled;
  }

  public void setLastModifyEnabled(boolean lastModifyEnabled) {
    this.lastModifyEnabled = lastModifyEnabled;
  }
}
