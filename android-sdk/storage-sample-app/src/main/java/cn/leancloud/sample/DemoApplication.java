package cn.leancloud.sample;

import android.app.Application;
import android.os.StrictMode;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;

/**
 * Created by fengjunwen on 2018/3/22.
 */

public class DemoApplication extends Application {
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
    AVObject.registerSubclass(Post.class);
    AVObject.registerSubclass(Student.class);
    AVObject.registerSubclass(Armor.class);
    AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
  }
}
