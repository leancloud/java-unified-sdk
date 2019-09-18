package cn.leancloud;

import android.content.Context;
import android.os.Handler;

import com.alibaba.fastjson.parser.ParserConfig;

import java.lang.reflect.Method;

import cn.leancloud.cache.AndroidSystemSetting;

import cn.leancloud.callback.AVCallback;
import cn.leancloud.core.AppRouter;
import cn.leancloud.internal.ThreadModel;
import cn.leancloud.logging.DefaultLoggerAdapter;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.network.AndroidNetworkingDetector;
import cn.leancloud.util.AndroidMimeTypeDetector;
import cn.leancloud.util.AndroidUtil;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
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
    if (!hasCustomizedServerURL(appId)) {
      throw new IllegalStateException("Please call AVOSCloud#initialize(context, appid, appkey, serverURL) instead of" +
          " AVOSCloud#initialize(context, appid, appkey), or call AVOSCloud#setServer(service, host) at first.");
    }
    if (null == handler && !AndroidUtil.isMainThread()) {
      throw new IllegalStateException("Please call AVOSCloud#initialize() in main thread.");
    }
    if (null == handler) {
      handler = new Handler();
    }

    ParserConfig.getGlobalInstance().autoTypeSupport = true;

    AppConfiguration.setLogAdapter(new DefaultLoggerAdapter());
    AppConfiguration.setGlobalNetworkingDetector(new AndroidNetworkingDetector(context));
    AppConfiguration.setMimeTypeDetector(new AndroidMimeTypeDetector());

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
    final AVLogger logger = LogUtil.getLogger(AVOSCloud.class);
    logger.i("[LeanCloud] initialize mainThreadChecker and threadShuttle within AVCallback.");

    String appIdPrefix = StringUtil.isEmpty(appId) ? "" : appId.substring(0, 8);
    String importantFileDir = context.getFilesDir().getAbsolutePath();
    String baseDir = context.getCacheDir().getAbsolutePath();
    String documentDir = context.getDir(appIdPrefix + "Paas", Context.MODE_PRIVATE).getAbsolutePath();
    String fileCacheDir = baseDir + "/" + appIdPrefix + "avfile";
    String commandCacheDir = baseDir + "/" + appIdPrefix + "CommandCache";
    String analyticsDir = baseDir + "/" + appIdPrefix + "Analysis";
    String queryResultCacheDir = baseDir + "/" + appIdPrefix + "PaasKeyValueCache";

    AndroidSystemSetting defaultSetting = new AndroidSystemSetting(context);

    AppConfiguration.configCacheSettings(importantFileDir, documentDir, fileCacheDir, queryResultCacheDir,
        commandCacheDir, analyticsDir, defaultSetting);
    AppConfiguration.setApplicationPackagename(context.getPackageName());

    logger.d("docDir=" + documentDir + ", fileDir=" + fileCacheDir + ", cmdDir="
        + commandCacheDir + ", statDir=" + analyticsDir);

    AppConfiguration.config(true, new AppConfiguration.SchedulerCreator() {
      public Scheduler create() {
        return AndroidSchedulers.mainThread();
      }
    });

    cn.leancloud.core.AVOSCloud.initialize(appId, appKey);

    try {
      Class androidInit = context.getClassLoader().loadClass("cn.leancloud.im.AndroidInitializer");
      //Class androidInit = Class.forName("cn.leancloud.im.v2.AndroidInitializer");
      Method initMethod = androidInit.getDeclaredMethod("init", Context.class);
      initMethod.invoke(null, context);
      logger.d("succeed to call cn.leancloud.im.AndroidInitializer#init(Context)");
    } catch (ClassNotFoundException ex) {
      logger.d("not found class: cn.leancloud.im.AndroidInitializer.");
    } catch (NoSuchMethodException ex) {
      logger.d("invalid AndroidInitializer, init(Context) method not found.");
    } catch (Exception ex) {
      logger.d("failed to call AndroidInitializer#init(Context), cause:" + ex.getMessage());
    }

    setContext(context);
  }

  public static void initialize(Context context, String appId, String appKey, String serverURL) {
    setServerURLs(serverURL);
    initialize(context, appId, appKey);
  }

  protected static boolean hasCustomizedServerURL(String applicationId) {
    REGION region = AppRouter.getAppRegion(applicationId);
    if (REGION.NorthAmerica == region || REGION.NorthAmerica == getRegion()) {
      // we use region from both application id and specified manually.
      return true;
    }
    return AppRouter.getInstance().hasFrozenEndpoint();
  }
}
