package cn.leancloud;

import android.content.Context;

import cn.leancloud.core.cache.PersistenceUtil;
import cn.leancloud.logging.DefaultLoggerAdapter;
import cn.leancloud.network.PaasClient;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class AVOSCloud extends cn.leancloud.core.AVOSCloud {
  public static void initialize(Context context, String appId, String appKey) {
    cn.leancloud.core.AVOSCloud.initialize(appId, appKey);
    setLogAdapter(new DefaultLoggerAdapter());
    String documentDir = "";
    String fileCacheDir = "";
    String commandCacheDir = "";
    String analyticsDir = "";
    PersistenceUtil.sharedInstance().config(documentDir, fileCacheDir, commandCacheDir, analyticsDir);
    PaasClient.config(true, new PaasClient.SchedulerCreator() {
      @Override
      public Scheduler create() {
        return AndroidSchedulers.mainThread();
      }
    });
  }
}
