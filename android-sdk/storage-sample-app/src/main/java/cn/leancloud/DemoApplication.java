package cn.leancloud;

import android.app.Application;

/**
 * Created by fengjunwen on 2018/3/22.
 */

public class DemoApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AVObject.registerSubclass(Post.class);
    AVObject.registerSubclass(Student.class);
    AVObject.registerSubclass(Armor.class);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
  }
}
