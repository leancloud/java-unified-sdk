package cn.leancloud;

import cn.leancloud.core.LeanCloud;
import cn.leancloud.utils.StringUtil;

public class Configure {
  public static final String TEST_APP_ID ;
  public static final String TEST_APP_KEY;
  public static final String API_HOST;
  public static final LeanCloud.REGION REGION;

  static {
    String app = System.getenv("APP_ID");
    TEST_APP_ID = StringUtil.isEmpty(app) ? "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz" : app;
    String appKEY = System.getenv("APP_KEY");
    TEST_APP_KEY = StringUtil.isEmpty(appKEY) ? "ye24iIK6ys8IvaISMC4Bs5WK" : appKEY;
    String regionStr = System.getenv("APP_REGION");
    REGION = StringUtil.isEmpty(regionStr) ? LeanCloud.REGION.NorthChina : LeanCloud.REGION.valueOf(regionStr);
    //API_HOST = System.getenv("API_HOST");
    API_HOST = "https://dyrq8yfh.lc-cn-n1-shared.com";

    System.out.println("Test APP_id: " + TEST_APP_ID);
    System.out.println("Test APP_key: " + TEST_APP_KEY);
    System.out.println("Test APP_region: " + REGION);
    System.out.println("Test API_HOST: " + API_HOST);
    System.out.println("");
  }

  public static void initialize() {
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    LeanCloud.setRegion(REGION);
    LeanCloud.initialize(TEST_APP_ID, TEST_APP_KEY, API_HOST);
    LeanCloud.setMasterKey(null);
  }

  public static void initializeWithApp(String appId, String appKey) {
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    LeanCloud.setRegion(REGION);
    LeanCloud.initialize(appId, appKey);
    LeanCloud.setMasterKey(null);
  }

  public static void initializeWithApp(String appId, String appKey, String serverUrl) {
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    LeanCloud.setRegion(REGION);
    LeanCloud.initialize(appId, appKey, serverUrl);
    LeanCloud.setMasterKey(null);
  }
}
