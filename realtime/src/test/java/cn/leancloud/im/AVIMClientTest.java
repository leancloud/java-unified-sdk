package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.im.v2.conversation.AVIMConversationMemberInfo;
import cn.leancloud.session.AVConnectionManager;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
          client.close(null);
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);

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

  public void testOnlineQuery() throws Exception {
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
          List<String> clients = Arrays.asList("Tom", "Jerry", "William");
          client.getOnlineClients(clients, new AVIMOnlineClientsCallback() {
            @Override
            public void done(List<String> object, AVIMException e) {
              if (null != e) {
                System.out.println("failed getOnlineClients");
                e.printStackTrace();
              } else {
                System.out.println("succeed getOnlineClients.");
                opersationSucceed = true;
              }
              client.close(null);
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testGetClientStatus() throws Exception {
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
          client.getClientStatus(new AVIMClientStatusCallback() {
            @Override
            public void done(AVIMClient.AVIMClientStatus status) {
              if (null != e) {
                System.out.println("failed getOnlineClients");
                e.printStackTrace();
              } else {
                System.out.println("succeed getOnlineClients.");
                opersationSucceed = true;
              }
              client.close(null);
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testCreateConversation() throws Exception {
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
          client.createConversation(Arrays.asList("testUser2"), "user1&user2", null, new AVIMConversationCreatedCallback() {
            @Override
            public void done(AVIMConversation conversation, AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to create Conv");
                ex.printStackTrace();
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to create Conv");
                conversation.getAllMemberInfo(0, 10, new AVIMConversationMemberQueryCallback() {
                  @Override
                  public void done(List<AVIMConversationMemberInfo> memberInfoList, AVIMException e3) {
                    if (null != e3) {
                      System.out.println("failed to query member info");
                      e3.printStackTrace();
                    } else {
                      System.out.println("succeed to query member info, result=" + memberInfoList);
                      opersationSucceed = true;
                    }
                    countDownLatch.countDown();
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    client.close(null);
    assertTrue(opersationSucceed);
  }
}
