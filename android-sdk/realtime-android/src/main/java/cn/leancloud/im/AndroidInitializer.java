package cn.leancloud.im;

import android.content.Context;
import android.os.Build;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.Messages;
import cn.leancloud.internal.ThreadModel.MainThreadChecker;
import cn.leancloud.internal.ThreadModel.ThreadShuttle;
import cn.leancloud.livequery.AVLiveQueryEventHandler;
import cn.leancloud.push.AVPushMessageListener;
import cn.leancloud.push.AndroidNotificationManager;
import cn.leancloud.util.AndroidUtil;
import cn.leancloud.utils.LogUtil;

/**
 * Created by fengjunwen on 2018/8/11.
 */

public class AndroidInitializer {
  private static AVLogger LOGGER = LogUtil.getLogger(AndroidInitializer.class);

  private static class AndroidSystemReporter implements SystemReporter {
    public AndroidSystemReporter() {
    }
    public SystemInfo getInfo() {
      boolean isProbablyAnEmulator = Build.FINGERPRINT.startsWith("generic")
          || Build.FINGERPRINT.startsWith("unknown")
          || Build.MODEL.contains("google_sdk")
          || Build.MODEL.contains("Emulator")
          || Build.MODEL.contains("Android SDK built for x86")
          || Build.BOARD == "QC_Reference_Phone" //bluestacks
          || Build.MANUFACTURER.contains("Genymotion")
          || Build.HOST.startsWith("Build") //MSI App Player
          || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
          || "google_sdk" == Build.PRODUCT;
      SystemInfo result = new SystemInfo();
      result.setBrand(Build.BRAND);
      result.setManufacturer(Build.MANUFACTURER);
      result.setModel(Build.MODEL);
      result.setOsAPILevel(Build.VERSION.SDK_INT);
      result.setOsCodeName(Build.VERSION.CODENAME);
      result.setRunOnEmulator(isProbablyAnEmulator);
      return result;
    }
  }

  private static void init() {
    MainThreadChecker checker = new MainThreadChecker() {
      @Override
      public boolean isMainThread() {
        return AndroidUtil.isMainThread();
      }
    };
    ThreadShuttle shuttle = new ThreadShuttle() {
      @Override
      public void launch(Runnable runnable) {
        AVOSCloud.getHandler().post(runnable);
      }
    };
    LOGGER.i("[LeanCloud] initialize mainThreadChecker and threadShuttle within AVIMEventHandler.");
    AVIMEventHandler.setMainThreadChecker(checker, shuttle);
    LOGGER.i("[LeanCloud] initialize mainThreadChecker and threadShuttle within AVLiveQueryEventHandler.");
    AVLiveQueryEventHandler.setMainThreadChecker(checker, shuttle);
    AVPushMessageListener.getInstance().setNotificationManager(AndroidNotificationManager.getInstance());
    LOGGER.i("[LeanCloud] initialize Android System Reporter.");
    AVIMOptions.getGlobalOptions().setSystemReporter(new AndroidSystemReporter());
  }

  public static void init(Context context) {
    if (InternalConfiguration.getDatabaseDelegateFactory() != null) {
      LOGGER.i("[LeanCloud] re-initialize InternalConfiguration.");
      return;
    }

    init();

    LOGGER.i("[LeanCloud] initialize InternalConfiguration within AVIMEventHandler.");
    AVIMOptions.getGlobalOptions().setMessageQueryCacheEnabled(true);
    InternalConfiguration.setFileMetaAccessor(new AndroidFileMetaAccessor());
    InternalConfiguration.setOperationTube(new AndroidOperationTube());
    InternalConfiguration.setDatabaseDelegateFactory(new AndroidDatabaseDelegateFactory(context));
  }
}
