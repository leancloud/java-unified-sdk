package cn.leancloud;

import cn.leancloud.core.AVOSCloud;

public class Configure {
  public static final String TEST_APP_ID = "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz";
  public static final String TEST_APP_KEY = "ye24iIK6ys8IvaISMC4Bs5WK";
  public static final AVOSCloud.REGION REGION = AVOSCloud.REGION.NorthChina;

  public static void initialize() {
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }
}
