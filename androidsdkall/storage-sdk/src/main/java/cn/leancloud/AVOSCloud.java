package cn.leancloud;

import android.content.Context;

import cn.leancloud.core.cache.PersistenceUtil;
import cn.leancloud.logging.DefaultLoggerAdapter;
import cn.leancloud.network.PaasClient;
import cn.leancloud.utils.LogUtil;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class AVOSCloud extends cn.leancloud.core.AVOSCloud {

  public static void initialize(Context context, String appId, String appKey) {
    cn.leancloud.core.AVOSCloud.initialize(appId, appKey);
    setLogAdapter(new DefaultLoggerAdapter());
    String baseDir = context.getCacheDir().getAbsolutePath();
    String documentDir = context.getDir("PaaS", Context.MODE_PRIVATE).getAbsolutePath();
    String fileCacheDir = baseDir + "/avfile/";
    String commandCacheDir = baseDir + "/CommandCache";
    String analyticsDir = baseDir + "/Analysis";
    PersistenceUtil.sharedInstance().config(documentDir, fileCacheDir, commandCacheDir, analyticsDir);
    LogUtil.getLogger(AVOSCloud.class).d("docDir=" + documentDir + ", fileDir=" + fileCacheDir
        + ", cmdDir=" + commandCacheDir + ", statDir=" + analyticsDir);
    PaasClient.config(true, new PaasClient.SchedulerCreator() {
      @Override
      public Scheduler create() {
        return AndroidSchedulers.mainThread();
      }
    });
  }
}
