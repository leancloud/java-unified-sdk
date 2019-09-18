package cn.leancloud.realtime_sample_app;

import android.app.Application;
import android.os.StrictMode;

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
  private static final String APP_SERVER_HOST = "https://dyrq8yfh.lc-cn-n1-shared.com";

  @Override
  public void onCreate() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .penaltyDeath()
        .build());

    super.onCreate();

    AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
    AVOSCloud.initialize(this, APPID, APPKEY, APP_SERVER_HOST);

    LOGGER.d("onCreate in thread:" + this.getMainLooper().getThread().getId());

    AVInstallation currentInstallation = AVInstallation.getCurrentInstallation();
    currentInstallation.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(AVObject avObject) {
        LOGGER.d("saveInstallation response in thread:" + Thread.currentThread().getId());
        System.out.println("succeed to save Installation. result: " + avObject);
      }

      @Override
      public void onError(Throwable e) {
        LOGGER.d("saveInstallation response in thread:" + Thread.currentThread().getId());
        System.out.println("failed to save Installation. cause:" + e.getMessage());
      }

      @Override
      public void onComplete() {

      }
    });
    PushService.setDefaultPushCallback(this, MainActivity.class);
  }
}
