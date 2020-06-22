package cn.leancloud.core;

import cn.leancloud.*;
import cn.leancloud.ops.BaseOperation;
import cn.leancloud.ops.BaseOperationAdapter;
import cn.leancloud.types.AVDate;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import io.reactivex.Observable;

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

  public static void enablePrintAllHeaders4Debug(boolean flag) {
    printAllHeaders = flag;
    if (flag) {
      System.out.println("======= WARNING: you are going to dump all details for a REST request," +
              " it is very risky for security!!!" +
              " please ONLY USE IN absolutely trusted environment!!!");
      System.out.println("( •̀ᴗ•́ )و( •̀ᴗ•́ )و( •̀ᴗ•́ )و( •̀ᴗ•́ )و( •̀ᴗ•́ )و( •̀ᴗ•́ )و( •̀ᴗ•́ )و( •̀ᴗ•́ )و( •̀ᴗ•́ )و( •̀ᴗ•́ )و");
    }
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

  public static void setCircularReferenceDetectEnable(boolean enable) {
    enableCircularReferenceDetect = enable;
  }

  public static boolean isEnableCircularReferenceDetect() {
    return enableCircularReferenceDetect;
  }

  public static void initialize(String appId, String appKey) {
    ObjectTypeAdapter adapter = new ObjectTypeAdapter();
    ParserConfig.getGlobalInstance().putDeserializer(AVObject.class, adapter);
    ParserConfig.getGlobalInstance().putDeserializer(AVUser.class, adapter);
    ParserConfig.getGlobalInstance().putDeserializer(AVFile.class, adapter);
    ParserConfig.getGlobalInstance().putDeserializer(AVStatus.class, adapter);
    ParserConfig.getGlobalInstance().putDeserializer(AVInstallation.class, adapter);

    SerializeConfig.getGlobalInstance().put(AVObject.class, adapter);
    SerializeConfig.getGlobalInstance().put(AVUser.class, adapter);
    SerializeConfig.getGlobalInstance().put(AVFile.class, adapter);
    SerializeConfig.getGlobalInstance().put(AVStatus.class, adapter);
    SerializeConfig.getGlobalInstance().put(AVInstallation.class, adapter);

    BaseOperationAdapter opAdapter = new BaseOperationAdapter();
    ParserConfig.getGlobalInstance().putDeserializer(BaseOperation.class, opAdapter);
    SerializeConfig.getGlobalInstance().put(BaseOperation.class, opAdapter);

    AVObject.registerSubclass(AVStatus.class);
    AVObject.registerSubclass(AVUser.class);
    AVObject.registerSubclass(AVFile.class);
    AVObject.registerSubclass(AVInstallation.class);

    applicationId = appId;
    applicationKey = appKey;
    PaasClient.initializeGlobalClient();
  }

  public static void initialize(String appId, String appKey, String serverUrl) {
    setServerURLs(serverUrl);
    initialize(appId, appKey);
  }

  /**
   * get current datetime from server.
   *
   * @return observable instance.
   */
  public static Observable<AVDate> getServerDateInBackground() {
    return PaasClient.getStorageClient().getServerTime();
  }

  /**
   * set master key.
   *
   * @param masterKey master key.
   */
  public static void setMasterKey(String masterKey) {
    GeneralRequestSignature.setMasterKey(masterKey);
  }

  /**
   * set server info.
   * @param service service type.
   * @param host host string.
   */
  public static void setServer(AVOSService service, String host) {
    if (StringUtil.isEmpty(host)) {
      return;
    }
    if (!host.toLowerCase().startsWith("http")) {
      // default protocol is https
      host = "https://" + host;
    }
    AppRouter appRouter = AppRouter.getInstance();
    appRouter.freezeEndpoint(service, host);
  }

  protected static void setServerURLs(String host) {
    setServer(AVOSService.API, host);
    setServer(AVOSService.RTM, host);
    setServer(AVOSService.ENGINE, host);
    setServer(AVOSService.PUSH, host);
    setServer(AVOSService.STATS, host);
  }

  @Deprecated
  public static void setLastModifyEnabled(boolean val) {
    AppConfiguration.setLastModifyEnabled(val);
  }

  @Deprecated
  public static boolean isLastModifyEnabled() {
    return AppConfiguration.isLastModifyEnabled();
  }

  @Deprecated
  public static void setNetworkTimeout(int seconds) {
    AppConfiguration.setNetworkTimeout(seconds);
  }

  @Deprecated
  public static int getNetworkTimeout() {
    return AppConfiguration.getNetworkTimeout();
  }

  /**
   * Set default behavior for object save operation.
   *
   * @param flag flag to indicate whether enable auto merge operation data or not, default is false
   */
  public static void setAutoMergeOperationDataWhenSave(boolean flag) {
    AppConfiguration.setAutoMergeOperationDataWhenSave(flag);
  }

  /**
   * Set global AVObject save option: always use fetchWhenSave or not.
   * @param flag flag of global fetchWhenSave, default is false
   */
  static void setAlwaysFetchWhenSave(boolean flag) {
    ;
  }

  public static String getApplicationId() {
    return applicationId;
  }
  public static String getSimplifiedAppId() {
    if (StringUtil.isEmpty(applicationId)) {
      return "";
    }
    return applicationId.substring(0, 8);
  }

  public static String getApplicationKey() {
    return applicationKey;
  }

  private static REGION defaultRegion = REGION.NorthChina;
  private static String applicationId = "";
  private static String applicationKey = "";
  private static volatile AVLogger.Level logLevel = AVLogger.Level.INFO;
  static boolean printAllHeaders = false;
  private static boolean enableCircularReferenceDetect = false;
}
