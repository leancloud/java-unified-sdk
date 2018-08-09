package cn.leancloud.session;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.v2.Conversation;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class AVConnectionManagerExTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean operationSucceed = false;
  public AVConnectionManagerExTest(String name) {
    super(name);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    operationSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    this.countDownLatch = null;
  }

  public void testConnectionRetry() throws Exception {
    AVIMOptions.getGlobalOptions().setRtmServer("wss://cn-n1-cell987.leancloud.cn");
    AVConnectionManager manager = AVConnectionManager.getInstance();
    new Thread(new Runnable() {
      @Override
      public void run() {
        manager.startConnection();
      }
    }).start();
    try {
      Thread.sleep(1000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    manager.startConnection(new AVCallback() {
      @Override
      protected void internalDone0(Object o, AVException avException) {
        if (null != avException) {
          avException.printStackTrace();
          operationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(operationSucceed);
  }

  public void testMultipleConnection() throws Exception {
    AVIMOptions.getGlobalOptions().setRtmServer("wss://cn-n1-cell987.leancloud.cn");
    AVConnectionManager manager = AVConnectionManager.getInstance();
    new Thread(new Runnable() {
      @Override
      public void run() {
        manager.startConnection();
      }
    }).start();
    try {
      Thread.sleep(1000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    manager.startConnection();
    manager.startConnection();
    manager.startConnection(new AVCallback() {
      @Override
      protected void internalDone0(Object o, AVException avException) {
        if (null != avException) {
          avException.printStackTrace();
          operationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(operationSucceed);
  }

}
