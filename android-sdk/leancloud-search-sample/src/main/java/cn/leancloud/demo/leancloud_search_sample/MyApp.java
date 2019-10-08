package cn.leancloud.demo.leancloud_search_sample;

import android.app.Application;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.utils.StringUtil;

public class MyApp extends Application {
  private static final String LeanCloud_APP_ID = "qJnLgVRA9mnzVSw4Ho3HtIaI-gzGzoHsz";
  private static final String LeanCloud_APP_KEY = "";
  private static final String LeanCloud_APP_MASTER_KEY = "lWmvnikOKNbJ9A6oj66Fe5aP";
  private static final String LeanCloud_APP_SERVER_URL = "https://api.leanticket.cn";
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
