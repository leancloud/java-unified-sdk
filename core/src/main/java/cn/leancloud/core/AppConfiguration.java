package cn.leancloud.core;

import cn.leancloud.AVACL;
import cn.leancloud.AVLogAdapter;
import cn.leancloud.cache.InMemorySetting;
import cn.leancloud.cache.LastModifyCache;
import cn.leancloud.cache.SystemSetting;
import cn.leancloud.logging.SimpleLoggerAdapter;
import cn.leancloud.network.NetworkingDetector;
import cn.leancloud.network.SimpleNetworkingDetector;
import io.reactivex.Scheduler;

import java.io.File;

public class AppConfiguration {
  public interface SchedulerCreator{
    Scheduler create();
  }
  public static final int DEFAULT_NETWORK_TIMEOUT = 30;

  private static AVACL defaultACL;
  private static int networkTimeout = DEFAULT_NETWORK_TIMEOUT;
  private static AVLogAdapter logAdapter = new SimpleLoggerAdapter();
  private static boolean asynchronized = false;
  private static SchedulerCreator defaultScheduler = null;
  private static NetworkingDetector globalNetworkingDetector = new SimpleNetworkingDetector();

  private static String documentDir = "./data/";
  private static String fileCacheDir = "./file/";
  private static String commandCacheDir = "./command/";
  private static String analyticsCacheDir = "./stats/";
  private static String queryResultCacheDir = "./PaasKeyValueCache";
  private static SystemSetting defaultSetting = new InMemorySetting();

  private static final String SDK_VERSION = "5.0.0";
  private static final String DEFAULT_USER_AGENT = "LeanCloud SDK v" + SDK_VERSION;

  public static void setNetworkTimeout(int seconds) {
    networkTimeout = seconds;
  }
  public static int getNetworkTimeout() {
    return networkTimeout;
  }

  public static void setLastModifyEnabled(boolean val) {
    LastModifyCache.getInstance().setLastModifyEnabled(val);
  }

  public static boolean isLastModifyEnabled() {
    return LastModifyCache.getInstance().isLastModifyEnabled();
  }

  public static AVACL getDefaultACL() {
    return defaultACL;
  }
  public static void setDefaultACL(AVACL acl) {
    defaultACL = acl;
  }

  public static void setLogAdapter(AVLogAdapter adapter) {
    logAdapter = adapter;
  }
  public static AVLogAdapter getLogAdapter() {
    return logAdapter;
  }
  public static String getUserAgent() {
    return DEFAULT_USER_AGENT;
  }

  public static void config(boolean asyncRequest, SchedulerCreator observerSchedulerCreator) {
    asynchronized = asyncRequest;
    defaultScheduler = observerSchedulerCreator;
  }

  public static boolean isAsynchronized() {
    return asynchronized;
  }

  public static SchedulerCreator getDefaultScheduler() {
    return defaultScheduler;
  }

  public static void makeSureCacheDirWorkable() {
    makeSureDirExist(documentDir);
    makeSureDirExist(fileCacheDir);
    makeSureDirExist(queryResultCacheDir);
    makeSureDirExist(commandCacheDir);
    makeSureDirExist(analyticsCacheDir);
  }

  private static void makeSureDirExist(String dirPath) {
    File dirFile = new File(dirPath);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }
  }

  /**
   * config local cache setting.
   * @param docDir
   * @param fileDir
   * @param queryResultDir
   * @param commandDir
   * @param analyticsDir
   * @param setting
   */
  public static void configCacheSettings(String docDir, String fileDir, String queryResultDir,
         String commandDir, String analyticsDir,
         SystemSetting setting) {
    documentDir = docDir;
    if (!documentDir.endsWith("/")) {
      documentDir += "/";
    }

    fileCacheDir = fileDir;
    if (!fileCacheDir.endsWith("/")) {
      fileCacheDir += "/";
    }

    queryResultCacheDir = queryResultDir;
    if (!queryResultCacheDir.endsWith("/")) {
      queryResultCacheDir += "/";
    }

    commandCacheDir = commandDir;
    if (!commandCacheDir.endsWith("/")) {
      commandCacheDir += "/";
    }

    analyticsCacheDir = analyticsDir;
    if (!analyticsCacheDir.endsWith("/")) {
      analyticsCacheDir += "/";
    }

    makeSureCacheDirWorkable();
    defaultSetting = setting;
  }

  public static String getAnalyticsCacheDir() {
    makeSureDirExist(analyticsCacheDir);
    return analyticsCacheDir;
  }

  public static String getCommandCacheDir() {
    makeSureDirExist(commandCacheDir);
    return commandCacheDir;
  }

  public static String getDocumentDir() {
    makeSureDirExist(documentDir);
    return documentDir;
  }

  public static String getFileCacheDir() {
    makeSureDirExist(fileCacheDir);
    return fileCacheDir;
  }

  public static String getQueryResultCacheDir() {
    makeSureDirExist(queryResultCacheDir);
    return queryResultCacheDir;
  }

  public static SystemSetting getDefaultSetting() {
    return defaultSetting;
  }

  public static NetworkingDetector getGlobalNetworkingDetector() {
    return globalNetworkingDetector;
  }

  public static void setGlobalNetworkingDetector(NetworkingDetector globalNetworkingDetector) {
    AppConfiguration.globalNetworkingDetector = globalNetworkingDetector;
  }
}
