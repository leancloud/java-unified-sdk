package cn.leancloud.im;

import java.util.concurrent.*;

public class BackgroundThreadpool {
  private static final BackgroundThreadpool INSTANCE = new BackgroundThreadpool();
  private ThreadPoolExecutor executor;
  private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
  public static BackgroundThreadpool getInstance() {
    return INSTANCE;
  }
  private BackgroundThreadpool() {
    this.executor = new ThreadPoolExecutor(1, 2, 10, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>());
    this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
  }

  public void execute(Runnable runnable) {
    this.executor.execute(runnable);
  }
  public void executeDelayed(Runnable runnable, long deleyInSecond) {
    this.scheduledThreadPoolExecutor.schedule(runnable, deleyInSecond, TimeUnit.SECONDS);
  }
  public void removeScheduledTask(Runnable runnable) {
    this.scheduledThreadPoolExecutor.remove(runnable);
  }
}
