package cn.leancloud.im;

import android.content.Context;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
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
  }

  public static void init(Context context) {
    if (InternalConfiguration.getDatabaseDelegateFactory() != null) {
      LOGGER.i("[LeanCloud] re-initialize InternalConfiguration.");
      return;
    }

    init();

    LOGGER.i("[LeanCloud] initialize InternalConfiguration within AVIMEventHandler.");
    InternalConfiguration.setFileMetaAccessor(new AndroidFileMetaAccessor());
    InternalConfiguration.setOperationTube(new AndroidOperationTube());
    InternalConfiguration.setDatabaseDelegateFactory(new AndroidDatabaseDelegateFactory(context));
  }
}
