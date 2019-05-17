package cn.leancloud.realtime_sample_app;

import android.app.Application;

import cn.leancloud.AVInstallation;
import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.push.PushService;
import cn.leancloud.utils.LogUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

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
    AVInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("succeed to save Installation. result: " + avObject);
      }

      @Override
      public void onError(Throwable e) {
        System.out.println("failed to save Installation. cause:" + e.getMessage());
      }

      @Override
      public void onComplete() {

      }
    });
    PushService.setDefaultPushCallback(this, MainActivity.class);
  }
}
