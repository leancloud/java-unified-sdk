package cn.leancloud;

import android.content.Context;

import cn.leancloud.cache.AndroidSystemSetting;

import cn.leancloud.logging.DefaultLoggerAdapter;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.utils.LogUtil;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class AVOSCloud extends cn.leancloud.core.AVOSCloud {

  public static void initialize(Context context, String appId, String appKey) {
    AppConfiguration.setLogAdapter(new DefaultLoggerAdapter());

    String importantFileDir = context.getFilesDir();
    String baseDir = context.getCacheDir().getAbsolutePath();
    String documentDir = context.getDir("PaaS", Context.MODE_PRIVATE).getAbsolutePath();
    String fileCacheDir = baseDir + "/avfile/";
    String commandCacheDir = baseDir + "/CommandCache";
    String analyticsDir = baseDir + "/Analysis";
    String queryResultCacheDir = baseDir + "/PaasKeyValueCache";
    AndroidSystemSetting defaultSetting = new AndroidSystemSetting(context);
    AppConfiguration.configCacheSettings(documentDir, fileCacheDir, queryResultCacheDir,
        commandCacheDir, analyticsDir, defaultSetting);
    AppConfiguration.setApplicationPackagename(context.getPackageName());

    LogUtil.getLogger(AVOSCloud.class).d("docDir=" + documentDir + ", fileDir=" + fileCacheDir
        + ", cmdDir=" + commandCacheDir + ", statDir=" + analyticsDir);

    AppConfiguration.config(true, new AppConfiguration.SchedulerCreator() {
      public Scheduler create() {
        return AndroidSchedulers.mainThread();
      }
    });

    cn.leancloud.core.AVOSCloud.initialize(appId, appKey);
  }
}
