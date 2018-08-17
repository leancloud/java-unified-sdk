package cn.leancloud.internal;

public class ThreadModel {
  public interface MainThreadChecker {
    boolean isMainThread();
  }
  public interface ThreadShuttle {
    void launch(Runnable runnable);
  }
}
