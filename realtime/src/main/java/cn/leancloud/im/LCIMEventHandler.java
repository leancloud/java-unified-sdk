package cn.leancloud.im;

import cn.leancloud.internal.ThreadModel.ThreadShuttle;
import cn.leancloud.internal.ThreadModel.MainThreadChecker;

public abstract class LCIMEventHandler {

  private static volatile boolean needCheckMainThread = false;
  private static volatile MainThreadChecker mainThreadChecker = null;
  private static volatile ThreadShuttle threadShuttle = null;

  static void setMainThreadChecker(MainThreadChecker checker, ThreadShuttle shuttle) {
    if (null == checker) {
      needCheckMainThread = false;
      mainThreadChecker = null;
      threadShuttle = null;
    } else {
      needCheckMainThread = true;
      mainThreadChecker = checker;
      threadShuttle = shuttle;
    }
  }

  public void processEvent(final int operation, final Object operator, final Object operand,
                           final Object eventScene) {
    if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
      threadShuttle.launch(new Runnable() {
        @Override
        public void run() {
          processEvent0(operation, operator, operand, eventScene);
        }
      });
    } else {
      processEvent0(operation, operator, operand, eventScene);
    }
  };

  protected abstract void processEvent0(int operation, Object operator, Object operand,
                                        Object eventScene);
}