package cn.leancloud.core;

import cn.leancloud.AVLogAdapter;
import cn.leancloud.AVLogger;
import cn.leancloud.AVObject;
import cn.leancloud.AVStatus;
import cn.leancloud.cache.LastModifyCache;
import cn.leancloud.logging.SimpleLoggerAdapter;

/**
 * we should set following variables:
 * 0. app region(one of EastChina, NorthChina, NorthAmerica)
 * 1. appid/appKey
 * 2. log level
 * 3. log adapter
 */
public class AVOSCloud {
  public enum REGION {
    EastChina, NorthChina, NorthAmerica
  }

  public static void setRegion(REGION region) {
    defaultRegion = region;
  }

  public static REGION getRegion() {
    return defaultRegion;
  }


  public static void setLogLevel(AVLogger.Level level) {
    logLevel = level;
  }
  public static AVLogger.Level getLogLevel() {
    return logLevel;
  }
  public static boolean isDebugEnable() {
    return logLevel.intLevel() >= AVLogger.Level.DEBUG.intLevel();
  }

  public static void initialize(String appId, String appKey) {
    applicationId = appId;
    applicationKey = appKey;
    AVObject.registerSubclass(AVStatus.class);

    AppConfiguration.getLogAdapter().setLevel(logLevel);
  }

  public static void setLastModifyEnabled(boolean val) {
    LastModifyCache.getInstance().setLastModifyEnabled(val);
  }

  public static boolean isLastModifyEnabled() {
    return LastModifyCache.getInstance().isLastModifyEnabled();
  }

  public static String getApplicationId() {
    return applicationId;
  }
  public static String getApplicationKey() {
    return applicationKey;
  }

  private static REGION defaultRegion = REGION.NorthChina;
  private static String applicationId = "";
  private static String applicationKey = "";
  private static AVLogger.Level logLevel = AVLogger.Level.INFO;
}
