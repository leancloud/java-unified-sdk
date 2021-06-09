package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.internal.ThreadModel;

public abstract class LCCallback<T> {
  private static volatile boolean needCheckMainThread = false;
  private static volatile ThreadModel.MainThreadChecker mainThreadChecker = null;
  private static volatile ThreadModel.ThreadShuttle threadShuttle = null;

  public static void setMainThreadChecker(ThreadModel.MainThreadChecker checker, ThreadModel.ThreadShuttle shuttle) {
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

  public void internalDone(final T t, final LCException LCException) {
    if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
      threadShuttle.launch(new Runnable() {
        @Override
        public void run() {
          internalDone0(t, LCException);
        }
      });
    } else {
      internalDone0(t, LCException);
    }
  }

  public void internalDone(final LCException LCException) {
    this.internalDone(null, LCException);
  }

  protected abstract void internalDone0(T t, LCException LCException);
}
