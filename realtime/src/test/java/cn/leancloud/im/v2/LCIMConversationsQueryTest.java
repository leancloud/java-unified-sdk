package cn.leancloud.im.v2;

import cn.leancloud.LCQuery;
import cn.leancloud.Configure;
import cn.leancloud.im.v2.callback.LCIMClientCallback;
import cn.leancloud.im.v2.callback.LCIMConversationCreatedCallback;
import cn.leancloud.im.v2.callback.LCIMConversationQueryCallback;
import cn.leancloud.session.LCConnectionManager;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LCIMConversationsQueryTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;
  LCIMClient client = LCIMClient.getInstance("testUser1");
  public LCIMConversationsQueryTest(String name) {
    super(name);
    Configure.initialize();
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    LCConnectionManager manager = LCConnectionManager.getInstance();
    manager.autoConnection();
    Thread.sleep(2000);
    opersationSucceed = false;
    final CountDownLatch latch = new CountDownLatch(1);
    System.out.println("try to openClient within setUp...");
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        System.out.println("openClient returned with result: " + (null == e));
        latch.countDown();
      }
    });
    latch.await();
  }

  @Override
  protected void tearDown() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    client.close(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        latch.countDown();
      }
    });
    latch.await();
    this.countDownLatch = null;
  }

  public void testNPECrashforIssue133() throws Exception {
    LCIMConversationsQuery query = client.getConversationsQuery();
    query.setQueryPolicy(LCQuery.CachePolicy.NETWORK_ONLY);
    query.orderByDescending("updatedAt");
    query.whereContains("m", client.getClientId());
    query.findInBackground(new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException e) {
        if (null != e) {
          System.out.println("failed to query convs");
          e.printStackTrace();
        } else {
          System.out.println("succeed to query convs。 result：" + conversations.size());
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testCommonQuery() throws Exception {
    LCIMConversationsQuery query = client.getConversationsQuery();
    query.whereContains("m", "Tom").addAscendingOrder("createdAt").findInBackground(new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException ex) {
        if (null != ex) {
          System.out.println("failed to query convs");
          ex.printStackTrace();
        } else {
          System.out.println("succeed to query convs. results=" + conversations.size());
          for (LCIMConversation conv : conversations) {
            System.out.println(conv.toJSONString());
          }
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testQueryEmptyConvWithLastMessage() throws Exception {
    LCIMConversationsQuery query = client.getConversationsQuery();
    query.whereEqualTo("objectId", "5d77057ec320f1ab6f8589c0").setWithLastMessagesRefreshed(true).limit(1)
            .findInBackground(new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException ex) {
        if (null != ex) {
          System.out.println("failed to query convs");
          ex.printStackTrace();
        } else {
          System.out.println("succeed to query convs. results=" + conversations.size());
          for (LCIMConversation conv : conversations) {
            if (conv.getLastMessage() == null) {
              opersationSucceed = true;
            } else {
              opersationSucceed = false;
            }
            System.out.println(conv.toJSONString());
          }
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testQueryNormalConvWithLastRawMessage() throws Exception {
    LCIMConversationsQuery query = client.getConversationsQuery();
    query.whereEqualTo("objectId", "5ce23b8ec320f1ab6f53fd50").setWithLastMessagesRefreshed(true).limit(1)
            .findInBackground(new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException ex) {
        if (null != ex) {
          System.out.println("failed to query convs");
          ex.printStackTrace();
        } else {
          System.out.println("succeed to query convs. results=" + conversations.size());
          for (LCIMConversation conv : conversations) {
            if (conv.getLastMessage() == null) {
              opersationSucceed = false;
            } else {
              opersationSucceed = true;
            }
            System.out.println(conv.toJSONString());
          }
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testQueryNormalConvWithLastTypedMessage() throws Exception {
    LCIMConversationsQuery query = client.getConversationsQuery();
    query.whereEqualTo("objectId", "5cfa1ff1c320f1ab6fa89f7e").setWithLastMessagesRefreshed(true).limit(1)
            .findInBackground(new LCIMConversationQueryCallback() {
              @Override
              public void done(List<LCIMConversation> conversations, LCIMException ex) {
                if (null != ex) {
                  System.out.println("failed to query convs");
                  ex.printStackTrace();
                } else {
                  System.out.println("succeed to query convs. results=" + conversations.size());
                  for (LCIMConversation conv : conversations) {
                    if (conv.getLastMessage() == null) {
                      opersationSucceed = false;
                    } else {
                      opersationSucceed = true;
                    }
                    System.out.println(conv.toJSONString());
                  }
                }
                countDownLatch.countDown();
              }
            });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testDirectQuery() throws Exception {
    LCIMConversationsQuery query = client.getConversationsQuery();
    String where = "{\"m\":{\"$regex\":\".*Tom.*\"}}";
    query.directFindInBackground(where, "createdAt", 0, 10, 2, new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException e) {
        if (null != e) {
          System.out.println("failed to query convs.");
          e.printStackTrace();
        } else {
          for(LCIMConversation conv : conversations) {
            System.out.println(conv.toJSONString());
          }
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testQueryTwice() throws Exception {
    final LCIMConversationsQuery query = client.getConversationsQuery();
    query.whereContains("m", "testUser1");
    query.findInBackground(new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          query.findInBackground(new LCIMConversationQueryCallback() {
            @Override
            public void done(List<LCIMConversation> conversations, LCIMException e) {
              opersationSucceed = null != conversations && conversations.size() > 0;
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testQueryNotExistedTempConversations() throws Exception {
    List<String> tempConvIds = new ArrayList<>();
    tempConvIds.add("fhaeihfafheh4247932472hfe");
    LCIMConversationsQuery query = client.getConversationsQuery();
    query.findTempConversationsInBackground(tempConvIds, new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException e) {
        if (null != e) {
          System.out.println("failed to query convs.");
          e.printStackTrace();
        } else {
          for(LCIMConversation conv : conversations) {
            System.out.println(conv.toJSONString());
          }
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testQueryRealTempConversations() throws Exception {
    final List<String> tempConvIds = new ArrayList<>();
    for (int i = 0;i < 3; i++) {
      List<String> members = new ArrayList<>();
      members.add(String.valueOf(i));
      final CountDownLatch latch = new CountDownLatch(1);
      client.createTemporaryConversation(members, 300, new LCIMConversationCreatedCallback() {
        @Override
        public void done(LCIMConversation conversation, LCIMException e) {
          if (null != conversation) {
            tempConvIds.add(conversation.getConversationId());
            System.out.println("succeed to create temporary conversation. " + conversation.toJSONString());
          } else {
            System.out.println("failed to create temporary conversation.");
            e.printStackTrace();
          }
          latch.countDown();
        }
      });
      latch.await();
    }

    LCIMConversationsQuery query = client.getConversationsQuery();
    query.setCompact(true);
    query.findTempConversationsInBackground(tempConvIds, new LCIMConversationQueryCallback() {
      @Override
      public void done(List<LCIMConversation> conversations, LCIMException e) {
        if (null != e) {
          System.out.println("failed to query convs.");
          e.printStackTrace();
        } else {
          for(LCIMConversation conv : conversations) {
            System.out.println(conv.toJSONString());
          }
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

}
