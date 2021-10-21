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
    TEST_APP_ID = StringUtil.isEmpty(app) ? "ohqhxu3mgoj2eyj6ed02yliytmbes3mwhha8ylnc215h0bgk" : app;
    String appKEY = System.getenv("APP_KEY");
    TEST_APP_KEY = StringUtil.isEmpty(appKEY) ? "6j8fuggqkbc5m86b8mp4pf2no170i5m7vmax5iypmi72wldc" : appKEY;
    String regionStr = System.getenv("APP_REGION");
    reGion = StringUtil.isEmpty(regionStr) ? LeanCloud.REGION.NorthChina : LeanCloud.REGION.valueOf(regionStr);
    //API_HOST = System.getenv("API_HOST");
    API_HOST = "https://ohqhxu3m.lc-cn-n1-shared.com";

    System.out.println("Test APP_id: " + TEST_APP_ID);
    System.out.println("Test APP_key: " + TEST_APP_KEY);
    System.out.println("Test APP_region: " + reGion);
    System.out.println("Test API_HOST: " + API_HOST);
    System.out.println("");
  }

  public static void initializeWithApp(String appId, String appKey, LeanCloud.REGION region) {
    LeanCloud.setRegion(region);
    LeanCloud.clearServerURLs();
    if (!StringUtil.isEmpty(API_HOST)) {
      LeanCloud.setServer(LeanService.API, API_HOST);
    }
    LeanCloud.setLogLevel(LCLogger.Level.INFO);
//    AppConfiguration.setEnableLocalCache(false);
    LeanCloud.initialize(appId, appKey);
    LeanCloud.setMasterKey("");
  }

  public static void initializeWithApp(String appId, String appKey, String serverUrl) {
    LeanCloud.setLogLevel(LCLogger.Level.INFO);
//    AppConfiguration.setEnableLocalCache(false);
    LeanCloud.initialize(appId, appKey, serverUrl);
    LeanCloud.setMasterKey("");
  }

  public static void initializeWithMasterKey(String appId, String masterKey, String serverUrl) {
    LeanCloud.setLogLevel(LCLogger.Level.INFO);
    LeanCloud.initialize(appId, "", serverUrl);
    LeanCloud.setMasterKey(masterKey);
  }

  public static void initializeRuntime() {
//    AppConfiguration.setLogAdapter(new DummyLoggerFactory());
    LeanCloud.setRegion(reGion);
    LeanCloud.clearServerURLs();
    if (!StringUtil.isEmpty(API_HOST)) {
      LeanCloud.setServer(LeanService.API, API_HOST);
    }
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    LeanCloud.initialize(TEST_APP_ID, TEST_APP_KEY);
    LeanCloud.setMasterKey("");
  }
}
