package cn.leancloud.core;

import cn.leancloud.AVLogAdapter;
import cn.leancloud.AVLogger;
import cn.leancloud.AVObject;
import cn.leancloud.AVStatus;
import cn.leancloud.cache.LastModifyCache;
import cn.leancloud.logging.SimpleLoggerAdapter;

public class AVOSCloud {
  public static enum REGION {
    EastChina, NorthChina, NorthAmerica
  }

  public static void setRegion(REGION region) {
    defaultRegion = region;
  }
  public static REGION getRegion() {
    return defaultRegion;
  }

  public static void setLogAdapter(AVLogAdapter adapter) {
    logAdapter = adapter;
    logAdapter.setLevel(logLevel);
  }
  public static AVLogAdapter getLogAdapter() {
    return logAdapter;
  }
  public static void setLogLevel(AVLogger.Level level) {
    logLevel = level;
    if (null != logAdapter) {
      logAdapter.setLevel(level);
    }
  }
  public static boolean isDebugEnable() {
    return logLevel.intLevel() >= AVLogger.Level.DEBUG.intLevel();
  }

  public static void initialize(String appId, String appKey) {
    applicationId = appId;
    applicationKey = appKey;
    AVObject.registerSubclass(AVStatus.class);
  }

  public void setLastModifyEnabled(boolean val) {
    LastModifyCache.getInstance().setLastModifyEnabled(val);
  }

  public boolean isLastModifyEnabled() {
    return LastModifyCache.getInstance().isLastModifyEnabled();
  }
  public static void setProductionMode(boolean productionMode) {
    isProduction = productionMode;
  }
  public static boolean isProductionMode() {return isProduction;}

  public static String getApplicationId() {
    return applicationId;
  }
  public static String getApplicationKey() {
    return applicationKey;
  }

  private static REGION defaultRegion = REGION.NorthChina;
  private static String applicationId = "";
  private static String applicationKey = "";
  private static AVLogAdapter logAdapter = new SimpleLoggerAdapter();
  private static AVLogger.Level logLevel = AVLogger.Level.INFO;
  private static boolean isProduction = true;
}
