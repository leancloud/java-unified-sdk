package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.internal.ThreadModel;

public abstract class AVCallback<T> {
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

  public void internalDone(final T t, final AVException avException) {
    if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
      threadShuttle.launch(new Runnable() {
        @Override
        public void run() {
          internalDone0(t, avException);
        }
      });
    } else {
      internalDone0(t, avException);
    }
  }

  public void internalDone(final AVException avException) {
    this.internalDone(null, avException);
  }

  protected abstract void internalDone0(T t, AVException avException);
}
