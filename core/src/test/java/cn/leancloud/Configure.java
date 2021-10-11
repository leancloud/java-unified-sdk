package cn.leancloud;

import cn.leancloud.core.LeanCloud;
import cn.leancloud.core.LeanService;
import cn.leancloud.utils.StringUtil;

public class Configure {
  public static final String TEST_APP_ID;
  public static final String TEST_APP_KEY;
  private static final LeanCloud.REGION reGion;
  private static final String API_HOST;
  static {
    String app = System.getenv("APP_ID");
    TEST_APP_ID = StringUtil.isEmpty(app) ? "0RiAlMny7jiz086FaU" : app;
    String appKEY = System.getenv("APP_KEY");
    TEST_APP_KEY = StringUtil.isEmpty(appKEY) ? "8V8wemqkpkxmAN7qKhvlh6v0pXc8JJzEZe3JFUnU" : appKEY;
    String regionStr = System.getenv("APP_REGION");
    reGion = StringUtil.isEmpty(regionStr) ? LeanCloud.REGION.NorthChina : LeanCloud.REGION.valueOf(regionStr);
    //API_HOST = System.getenv("API_HOST");
    API_HOST = "https://0rialmny.cloud.tds1.tapapis.cn";

    System.out.println("Test APP_id: " + TEST_APP_ID);
    System.out.println("Test APP_key: " + TEST_APP_KEY);
    System.out.println("Test APP_region: " + reGion);
    System.out.println("Test API_HOST: " + API_HOST);
    System.out.println("");
  }

  public static void initializeWithApp(String appId, String appKey, LeanCloud.REGION region) {
    LeanCloud.setRegion(region);
    if (!StringUtil.isEmpty(API_HOST)) {
      LeanCloud.setServer(LeanService.API, API_HOST);
    }
    LeanCloud.setLogLevel(LCLogger.Level.VERBOSE);
//    AppConfiguration.setEnableLocalCache(false);
    LeanCloud.initialize(appId, appKey);
    LeanCloud.setMasterKey("");
  }

  public static void initializeWithApp(String appId, String appKey, String serverUrl) {
    LeanCloud.setLogLevel(LCLogger.Level.VERBOSE);
//    AppConfiguration.setEnableLocalCache(false);
    LeanCloud.initialize(appId, appKey, serverUrl);
    LeanCloud.setMasterKey("");
  }

  public static void initializeWithMasterKey(String appId, String masterKey, String serverUrl) {
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    LeanCloud.initialize(appId, "", serverUrl);
    LeanCloud.setMasterKey(masterKey);
  }

  public static void initializeRuntime() {
//    AppConfiguration.setLogAdapter(new DummyLoggerFactory());
    LeanCloud.setRegion(reGion);
    if (!StringUtil.isEmpty(API_HOST)) {
      LeanCloud.setServer(LeanService.API, API_HOST);
    }
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    LeanCloud.initialize(TEST_APP_ID, TEST_APP_KEY);
    LeanCloud.setMasterKey("");
  }
}
