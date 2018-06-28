package cn.leancloud.sample;

import android.app.Application;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;

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
