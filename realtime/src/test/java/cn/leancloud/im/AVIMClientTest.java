package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.session.AVConnectionManager;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class AVIMClientTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;

  public AVIMClientTest(String name) {
    super(name);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    AVConnectionManager manager = AVConnectionManager.getInstance();
    opersationSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    this.countDownLatch = null;
  }

  public void testOpenClient() throws Exception {
    AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(2000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
        } else {
          System.out.println("succeed open client.");
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    client.close(null);
  }

  public void testCloseClient() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(2000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          client.close(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
              if (null != e) {
                System.out.println("failed close client");
                e.printStackTrace();
              } else {
                System.out.println("succeed close client.");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }
}
