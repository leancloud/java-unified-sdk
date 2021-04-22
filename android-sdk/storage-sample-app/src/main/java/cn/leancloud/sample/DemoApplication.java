package cn.leancloud.sample;

import android.app.Application;
import android.os.StrictMode;

import cn.leancloud.LCLogger;
import cn.leancloud.LeanCloud;
import cn.leancloud.LCObject;

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
    LCObject.registerSubclass(Post.class);
    LCObject.registerSubclass(Student.class);
    LCObject.registerSubclass(Armor.class);
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
  }
}
