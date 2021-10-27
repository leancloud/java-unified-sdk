package cn.leancloud.realtime_sample_app;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.StrictMode;

import java.io.OutputStream;
import java.io.PrintStream;

import cn.leancloud.LCInstallation;
import cn.leancloud.LCLogger;
import cn.leancloud.LeanCloud;
import cn.leancloud.LCObject;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMClientEventHandler;
import cn.leancloud.push.PushService;
import cn.leancloud.utils.LogUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by fengjunwen on 2018/8/9.
 */

public class MyApplication extends Application {
  private static final LCLogger LOGGER = LogUtil.getLogger(MyApplication.class);

  private static final String APPID = "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz";
  private static final String APPKEY = "ye24iIK6ys8IvaISMC4Bs5WK";
  private static final String APP_SERVER_HOST = "https://dyrq8yfh.lc-cn-n1-shared.com";

  static Context app;
  @Override
  public void onCreate() {
//    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//        .detectNetwork()   // or .detectAll() for all detectable problems
//        .penaltyLog()
//        .build());
//    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//        .detectLeakedSqlLiteObjects()
//        .detectLeakedClosableObjects()
//        .penaltyLog()
//        .penaltyDeath()
//        .build());

    System.setErr (new HProfDumpingStderrPrintStream (System.err));

    super.onCreate();
    app = this;

    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    LeanCloud.initialize(this, APPID, APPKEY, APP_SERVER_HOST);

    LCIMClient.setClientEventHandler(new LCIMClientEventHandler() {
      @Override
      public void onConnectionPaused(LCIMClient client) {
        System.out.println("============ CONNECTION PAUSED ============");
      }

      @Override
      public void onConnectionResume(LCIMClient client) {
        System.out.println("============ CONNECTION RESUMED ============");
      }

      @Override
      public void onClientOffline(LCIMClient client, int code) {
        System.out.println("============ CLIENT OFFLINE ============");
      }
    });

    LOGGER.d("onCreate in thread:" + this.getMainLooper().getThread().getId());

    LCInstallation currentInstallation = LCInstallation.getCurrentInstallation();
    currentInstallation.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(LCObject avObject) {
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
    String channelId = "cn.leancloud.simpleapp";
    PushService.createNotificationChannel(this, channelId, "realtime-demo", "realtime-demo",
        NotificationManager.IMPORTANCE_DEFAULT, false, 0, false, null);
    PushService.setDefaultChannelId(this, channelId);
    PushService.setNotificationIcon(R.drawable.ic_notifications_black_24dp);

//    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
//    Notification notification = notificationBuilder.setOngoing(true)
//        .setSmallIcon(R.mipmap.ic_launcher)
//        .setContentTitle("App is running in background")
//        .setPriority(NotificationManager.IMPORTANCE_MIN)
//        .setCategory(Notification.CATEGORY_SERVICE)
//        .build();
//    PushService.setForegroundMode(true, 101, notification);
    PushService.setDefaultPushCallback(this, MainActivity.class);
  }

  private static class HProfDumpingStderrPrintStream extends PrintStream
  {
    public HProfDumpingStderrPrintStream (OutputStream destination)
    {
      super (destination);
    }

    @Override
    public synchronized void println (String str)
    {
      super.println (str);
      if (str.equals ("StrictMode VmPolicy violation with POLICY_DEATH; shutting down."))
      {
        // StrictMode is about to terminate us... don't let it!
        super.println ("Trapped StrictMode shutdown notice: logging heap data");
        try {
          android.os.Debug.dumpHprofData(app.getDir ("hprof", MODE_WORLD_READABLE) +"/strictmode-death-penalty.hprof");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
