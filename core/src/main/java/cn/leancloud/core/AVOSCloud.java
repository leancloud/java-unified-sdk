package cn.leancloud.core;

public class AVOSCloud {
  public static enum REGION {
    EastChina, NorthChina, NorthAmerica
  }

  public static void setRegion(REGION region) {
    defaultRegion = region;
  }

  public static void initialize(String appId, String appKey) {
    applicationId = appId;
    applicationKey = appKey;
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
}
