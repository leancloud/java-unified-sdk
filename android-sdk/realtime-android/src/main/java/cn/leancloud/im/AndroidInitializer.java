package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.im.AVIMEventHandler;
import cn.leancloud.util.AndroidUtil;
import cn.leancloud.utils.LogUtil;

/**
 * Created by fengjunwen on 2018/8/11.
 */

public class AndroidInitializer {
  private static AVLogger LOGGER = LogUtil.getLogger(AndroidInitializer.class);

  public static void init() {
    LOGGER.i("[LeanCloud] initialize mainThreadChecker and threadShuttle within AVIMEventHandler.");
    AVIMEventHandler.setMainThreadChecker(new AVIMEventHandler.MainThreadChecker() {
      @Override
      public boolean isMainThread() {
        return AndroidUtil.isMainThread();
      }
    }, new AVIMEventHandler.ThreadShuttle() {
      @Override
      public void launch(Runnable runnable) {
        AVOSCloud.getHandler().post(runnable);
      }
    });
  }
}
