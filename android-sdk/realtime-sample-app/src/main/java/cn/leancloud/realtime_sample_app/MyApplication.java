package cn.leancloud.realtime_sample_app;

import android.app.Application;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.push.PushService;
import cn.leancloud.utils.LogUtil;

/**
 * Created by fengjunwen on 2018/8/9.
 */

public class MyApplication extends Application {
  private static final AVLogger LOGGER = LogUtil.getLogger(MyApplication.class);

  private static final String APPID = "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz";
  private static final String APPKEY = "ye24iIK6ys8IvaISMC4Bs5WK";

  @Override
  public void onCreate() {
    LOGGER.d("onCreate");
    super.onCreate();
    AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
    AVOSCloud.initialize(this, APPID, APPKEY);
    PushService.setDefaultPushCallback(this, MainActivity.class);
  }
}
