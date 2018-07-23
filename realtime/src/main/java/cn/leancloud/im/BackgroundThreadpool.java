package cn.leancloud.im;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackgroundThreadpool {
  private static final BackgroundThreadpool INSTANCE = new BackgroundThreadpool();
  private ThreadPoolExecutor executor;
  public static BackgroundThreadpool getInstance() {
    return INSTANCE;
  }
  private BackgroundThreadpool() {
    this.executor = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>());
  }

  public void execute(Runnable runnable) {
    this.executor.execute(runnable);
  }
}
