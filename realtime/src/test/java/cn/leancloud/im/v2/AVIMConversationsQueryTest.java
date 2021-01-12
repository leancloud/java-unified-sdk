package cn.leancloud.im.v2;

import cn.leancloud.AVQuery;
import cn.leancloud.Configure;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCreatedCallback;
import cn.leancloud.im.v2.callback.AVIMConversationQueryCallback;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.utils.StringUtil;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVIMConversationsQueryTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;
  AVIMClient client = AVIMClient.getInstance("testUser1");
  public AVIMConversationsQueryTest(String name) {
    super(name);
    Configure.initialize();
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    AVConnectionManager manager = AVConnectionManager.getInstance();
    manager.autoConnection();
    Thread.sleep(2000);
    opersationSucceed = false;
    final CountDownLatch latch = new CountDownLatch(1);
    System.out.println("try to openClient within setUp...");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        System.out.println("openClient returned with result: " + (null == e));
        latch.countDown();
      }
    });
    latch.await();
  }

  @Override
  protected void tearDown() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    client.close(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        latch.countDown();
      }
    });
    latch.await();
    this.countDownLatch = null;
  }

  public void testNPECrashforIssue133() throws Exception {
    AVIMConversationsQuery query = client.getConversationsQuery();
    query.setQueryPolicy(AVQuery.CachePolicy.NETWORK_ONLY);
    query.orderByDescending("updatedAt");
    query.whereContains("m", client.getClientId());
    query.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
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
    AVIMConversationsQuery query = client.getConversationsQuery();
    query.whereContains("m", "Tom").addAscendingOrder("createdAt").findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException ex) {
        if (null != ex) {
          System.out.println("failed to query convs");
          ex.printStackTrace();
        } else {
          System.out.println("succeed to query convs. results=" + conversations.size());
          for (AVIMConversation conv : conversations) {
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

  public void testDirectQuery() throws Exception {
    AVIMConversationsQuery query = client.getConversationsQuery();
    String where = "{\"m\":{\"$regex\":\".*Tom.*\"}}";
    query.directFindInBackground(where, "createdAt", 0, 10, 2, new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
        if (null != e) {
          System.out.println("failed to query convs.");
          e.printStackTrace();
        } else {
          for(AVIMConversation conv : conversations) {
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
    final AVIMConversationsQuery query = client.getConversationsQuery();
    query.whereContains("m", "testUser1");
    query.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          query.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> conversations, AVIMException e) {
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
    AVIMConversationsQuery query = client.getConversationsQuery();
    query.findTempConversationsInBackground(tempConvIds, new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
        if (null != e) {
          System.out.println("failed to query convs.");
          e.printStackTrace();
        } else {
          for(AVIMConversation conv : conversations) {
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
      client.createTemporaryConversation(members, 300, new AVIMConversationCreatedCallback() {
        @Override
        public void done(AVIMConversation conversation, AVIMException e) {
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

    AVIMConversationsQuery query = client.getConversationsQuery();
    query.setCompact(true);
    query.findTempConversationsInBackground(tempConvIds, new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
        if (null != e) {
          System.out.println("failed to query convs.");
          e.printStackTrace();
        } else {
          for(AVIMConversation conv : conversations) {
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
