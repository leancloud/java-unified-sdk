package cn.leancloud;

import android.content.Context;
import android.os.Handler;

import cn.leancloud.cache.AndroidSystemSetting;

import cn.leancloud.callback.AVCallback;
import cn.leancloud.internal.ThreadModel;
import cn.leancloud.logging.DefaultLoggerAdapter;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.network.AndroidNetworkingDetector;
import cn.leancloud.util.AndroidUtil;
import cn.leancloud.utils.LogUtil;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class AVOSCloud extends cn.leancloud.core.AVOSCloud {
  private static Context context = null;
  protected static Handler handler = null;

  public static Context getContext() {
    return context;
  }

  public static void setContext(Context context) {
    AVOSCloud.context = context;
  }

  public static Handler getHandler() {
    return handler;
  }

  public static void initialize(Context context, String appId, String appKey) {
    if (null == handler && !AndroidUtil.isMainThread()) {
      throw new IllegalStateException("Please call AVOSCloud.initialize in main thread.");
    }
    if (null == handler) {
      handler = new Handler();
    }
    AppConfiguration.setLogAdapter(new DefaultLoggerAdapter());
    AppConfiguration.setGlobalNetworkingDetector(new AndroidNetworkingDetector(context));

    ThreadModel.MainThreadChecker checker = new ThreadModel.MainThreadChecker() {
      @Override
      public boolean isMainThread() {
        return AndroidUtil.isMainThread();
      }
    };
    ThreadModel.ThreadShuttle shuttle = new ThreadModel.ThreadShuttle() {
      @Override
      public void launch(Runnable runnable) {
        AVOSCloud.getHandler().post(runnable);
      }
    };
    AVCallback.setMainThreadChecker(checker, shuttle);
    LogUtil.getLogger(AVOSCloud.class).i("[LeanCloud] initialize mainThreadChecker and threadShuttle within AVCallback.");

    String importantFileDir = context.getFilesDir().getAbsolutePath();
    String baseDir = context.getCacheDir().getAbsolutePath();
    String documentDir = context.getDir("PaaS", Context.MODE_PRIVATE).getAbsolutePath();
    String fileCacheDir = baseDir + "/avfile/";
    String commandCacheDir = baseDir + "/CommandCache";
    String analyticsDir = baseDir + "/Analysis";
    String queryResultCacheDir = baseDir + "/PaasKeyValueCache";
    AndroidSystemSetting defaultSetting = new AndroidSystemSetting(context);
    AppConfiguration.configCacheSettings(importantFileDir, documentDir, fileCacheDir, queryResultCacheDir,
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

    setContext(context);
  }
}
