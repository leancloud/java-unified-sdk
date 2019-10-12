package cn.leancloud.demo.leancloud_search_sample;

import android.app.Application;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.utils.StringUtil;

public class MyApp extends Application {
  private static final String LeanCloud_APP_ID = "ohqhxu3mgoj2eyj6ed02yliytmbes3mwhha8ylnc215h0bgk";
  private static final String LeanCloud_APP_KEY = "6j8fuggqkbc5m86b8mp4pf2no170i5m7vmax5iypmi72wldc";
  private static final String LeanCloud_APP_MASTER_KEY = "";
  private static final String LeanCloud_APP_SERVER_URL = "https://ohqhxu3m.lc-cn-n1-shared.com";
  @Override
  public void onCreate() {
    super.onCreate();

    AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);

    AVOSCloud.initialize(this, LeanCloud_APP_ID, LeanCloud_APP_KEY, LeanCloud_APP_SERVER_URL);
    if (!StringUtil.isEmpty(LeanCloud_APP_MASTER_KEY)) {
      AVOSCloud.setMasterKey(LeanCloud_APP_MASTER_KEY);
    }
  }
}
