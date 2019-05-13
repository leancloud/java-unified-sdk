package cn.leancloud.push.lite.ws;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

abstract class HeartBeatPolicy {
  /**
   * 定时任务
   */
  private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

  private Future healthFuture;

  /**
   * 最近收到 ping 的时间
   */
  private long lastPongTS;

  /**
   * 心跳设置为 180s
   */
  private final static long HEART_BEAT_INTERNAL = 180 * 1000;

  /**
   * 超时时长设置为两个心跳
   */
  private final static long HEALTHY_THRESHOLD = HEART_BEAT_INTERNAL * 2;

  private Runnable healthMonitor = new Runnable() {
    public void run() {
      if (System.currentTimeMillis() - lastPongTS > HEALTHY_THRESHOLD) {
        onTimeOut();
      } else {
        sendPing();
      }
    }
  };

  synchronized void onPong() {
    lastPongTS = System.currentTimeMillis();
  }

  synchronized void start() {
    stop();
    lastPongTS = System.currentTimeMillis();
    healthFuture = executor.scheduleAtFixedRate(healthMonitor, HEART_BEAT_INTERNAL, HEART_BEAT_INTERNAL,
        TimeUnit.MILLISECONDS);
  }

  synchronized void stop() {
    if (null != healthFuture) {
      healthFuture.cancel(true);
    }
  }

  public abstract void onTimeOut();

  public abstract void sendPing();
}
