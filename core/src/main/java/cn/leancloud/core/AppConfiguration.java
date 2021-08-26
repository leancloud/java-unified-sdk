package cn.leancloud.core;

import cn.leancloud.LCACL;
import cn.leancloud.gson.GSONConverterFactory;
import cn.leancloud.json.ConverterFactory;
import cn.leancloud.json.JSONParser;
import cn.leancloud.logging.InternalLoggerAdapter;
import cn.leancloud.cache.InMemorySetting;
import cn.leancloud.cache.LastModifyCache;
import cn.leancloud.cache.SystemSetting;
import cn.leancloud.logging.SimpleLoggerAdapter;
import cn.leancloud.network.NetworkingDetector;
import cn.leancloud.network.SimpleNetworkingDetector;
import cn.leancloud.utils.FileUtil;
import io.reactivex.Scheduler;

import java.io.File;

public class AppConfiguration {
  public interface SchedulerCreator{
    Scheduler create();
  }
  public static final int DEFAULT_NETWORK_TIMEOUT = 30;

  private static LCACL defaultACL;
  private static int networkTimeout = DEFAULT_NETWORK_TIMEOUT;
  private static InternalLoggerAdapter logAdapter = new SimpleLoggerAdapter();
  private static boolean asynchronized = false;
  private static SchedulerCreator defaultScheduler = null;
  private static NetworkingDetector globalNetworkingDetector = new SimpleNetworkingDetector();
  private static String applicationPackageName = "";

  private static String importantFileDir = "./persistFiles/";
  private static String documentDir = "./data/";
  private static String fileCacheDir = "./file/";
  private static String commandCacheDir = "./command/";
  private static String analyticsCacheDir = "./stats/";
  private static String queryResultCacheDir = "./PaasKeyValueCache";
  private static SystemSetting defaultSetting = new InMemorySetting();

  private static boolean enableLocalCache = true;
  private static boolean incognitoMode = false;

  private static ConverterFactory converterFactory = new GSONConverterFactory();
  private static retrofit2.Converter.Factory retrofitConverterFactory = converterFactory.generateRetrofitConverterFactory();
  private static JSONParser jsonParser = converterFactory.createJSONParser();

  private static final String SDK_VERSION = "8.1.0";
  private static final String DEFAULT_USER_AGENT = "LeanCloud-Java-SDK/" + SDK_VERSION;

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

  public static LCACL getDefaultACL() {
    return defaultACL;
  }
  public static void setDefaultACL(LCACL acl) {
    defaultACL = acl;
  }

  public static void setLogAdapter(InternalLoggerAdapter adapter) {
    logAdapter = adapter;
  }
  public static InternalLoggerAdapter getLogAdapter() {
    return logAdapter;
  }
  public static String getUserAgent() {
    return DEFAULT_USER_AGENT;
  }

  public static void setIncognitoMode(boolean mode) {
    incognitoMode = mode;
  }

  public static boolean isIncognitoMode() {
    return incognitoMode;
  }

  public static void setConverterFactory(ConverterFactory cf) {
    if (null == cf) {
      return;
    }
    converterFactory = cf;
    retrofitConverterFactory = converterFactory.generateRetrofitConverterFactory();
    jsonParser = converterFactory.createJSONParser();
  }

  public static retrofit2.Converter.Factory getRetrofitConverterFactory() {
    return retrofitConverterFactory;
  }

  public static JSONParser getJsonParser() {
    return jsonParser;
  }

  public static void config(boolean asyncRequest, SchedulerCreator observerSchedulerCreator) {
    asynchronized = asyncRequest;
    defaultScheduler = observerSchedulerCreator;
  }

  public static boolean isEnableLocalCache() {
    return enableLocalCache;
  }

  /**
   * set flag to enable local cache or not.
   * @param enableLocalCache flag to enable local cache or not
   */
  public static void setEnableLocalCache(boolean enableLocalCache) {
    AppConfiguration.enableLocalCache = enableLocalCache;
  }

  private static boolean autoMergeOperationDataWhenSave = true;

  public static boolean isAutoMergeOperationDataWhenSave() {
    return autoMergeOperationDataWhenSave;
  }

  /**
   * Set default behavior for object save operation.
   *
   * @param flag flag to indicate whether enable auto merge operation data or not, default is false
   */
  public static void setAutoMergeOperationDataWhenSave(boolean flag) {
    AppConfiguration.autoMergeOperationDataWhenSave = flag;
  }

  public static boolean isAsynchronized() {
    return asynchronized;
  }

  public static SchedulerCreator getDefaultScheduler() {
    return defaultScheduler;
  }

  public static void makeSureCacheDirWorkable() {
    makeSureDirExist(importantFileDir);
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
   * @param imFileDir im file cache directory.
   * @param docDir   document cache directory.
   * @param fileDir file cache directory.
   * @param queryResultDir query result cache directory.
   * @param commandDir command cache directory.
   * @param analyticsDir analytics cache directory.
   * @param setting other settings.
   */
  public static void configCacheSettings(String imFileDir, String docDir, String fileDir, String queryResultDir,
         String commandDir, String analyticsDir, SystemSetting setting) {
    importantFileDir = imFileDir;
    if (!importantFileDir.endsWith("/")) {
      importantFileDir += "/";
    }

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
    if (!enableLocalCache) {
      return null;
    }
    makeSureDirExist(analyticsCacheDir);
    return analyticsCacheDir;
  }

  public static String getCommandCacheDir() {
    if (!enableLocalCache) {
      return null;
    }
    makeSureDirExist(commandCacheDir);
    return commandCacheDir;
  }

  public static String getImportantFileDir() {
    if (!enableLocalCache) {
      return null;
    }
    makeSureDirExist(importantFileDir);
    return importantFileDir;
  }

  public static String getDocumentDir() {
    if (!enableLocalCache) {
      return null;
    }
    makeSureDirExist(documentDir);
    return documentDir;
  }

  public static String getFileCacheDir() {
    if (!enableLocalCache) {
      return null;
    }
    makeSureDirExist(fileCacheDir);
    return fileCacheDir;
  }

  public static String getQueryResultCacheDir() {
    if (!enableLocalCache) {
      return null;
    }
    makeSureDirExist(queryResultCacheDir);
    return queryResultCacheDir;
  }

  public static String getApplicationPackageName() {
    return applicationPackageName;
  }

  public static void setApplicationPackageName(String applicationPackageName) {
    AppConfiguration.applicationPackageName = applicationPackageName;
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

  public static void setMimeTypeDetector(FileUtil.MimeTypeDetector detector) {
    if (null != detector) {
      FileUtil.config(detector);
    }
  }
}
