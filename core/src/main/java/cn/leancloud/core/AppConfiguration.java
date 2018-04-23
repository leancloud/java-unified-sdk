package cn.leancloud.core;

import cn.leancloud.AVACL;
import cn.leancloud.AVLogAdapter;
import cn.leancloud.cache.InMemorySetting;
import cn.leancloud.cache.SystemSetting;
import cn.leancloud.logging.SimpleLoggerAdapter;
import io.reactivex.Scheduler;

import java.io.File;

public class AppConfiguration {
  public interface SchedulerCreator{
    Scheduler create();
  }

  private static AVACL defaultACL;
  private static AVLogAdapter logAdapter = new SimpleLoggerAdapter();
  private static boolean asynchronized = false;
  private static SchedulerCreator defaultScheduler = null;

  private static String documentDir = "./data/";
  private static String fileCacheDir = "./file/";
  private static String commandCacheDir = "./command/";
  private static String analyticsCacheDir = "./stats/";
  private static String queryResultCacheDir = "./PaasKeyValueCache";
  private static SystemSetting defaultSetting = new InMemorySetting();

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
    File dirFile = new File(documentDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }

    fileCacheDir = fileDir;
    if (!fileCacheDir.endsWith("/")) {
      fileCacheDir += "/";
    }
    dirFile = new File(fileCacheDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }

    queryResultCacheDir = queryResultDir;
    if (!queryResultCacheDir.endsWith("/")) {
      queryResultCacheDir += "/";
    }
    dirFile = new File(queryResultCacheDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }


    commandCacheDir = commandDir;
    if (!commandCacheDir.endsWith("/")) {
      commandCacheDir += "/";
    }
    dirFile = new File(commandCacheDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }

    analyticsCacheDir = analyticsDir;
    if (!analyticsCacheDir.endsWith("/")) {
      analyticsCacheDir += "/";
    }
    dirFile = new File(analyticsCacheDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }

    defaultSetting = setting;
  }

  public static String getAnalyticsCacheDir() {
    return analyticsCacheDir;
  }

  public static String getCommandCacheDir() {
    return commandCacheDir;
  }

  public static String getDocumentDir() {
    return documentDir;
  }

  public static String getFileCacheDir() {
    return fileCacheDir;
  }

  public static String getQueryResultCacheDir() {
    return queryResultCacheDir;
  }

  public static SystemSetting getDefaultSetting() {
    return defaultSetting;
  }
}
