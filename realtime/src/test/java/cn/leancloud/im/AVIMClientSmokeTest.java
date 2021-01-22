package cn.leancloud.im;

import cn.leancloud.AVException;
import cn.leancloud.AVQuery;
import cn.leancloud.Configure;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationsQuery;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMConversationQueryCallback;
import cn.leancloud.session.AVConnectionManager;
import junit.framework.TestCase;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVIMClientSmokeTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;

  public AVIMClientSmokeTest(String name) {
    super(name);
    Configure.initializeWithApp("", "");
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    AVConnectionManager manager = AVConnectionManager.getInstance();
    final CountDownLatch tmpLatch = new CountDownLatch(1);
    manager.startConnection(new AVCallback() {
      @Override
      protected void internalDone0(Object o, AVException avException) {
        tmpLatch.countDown();
      }
    });
    tmpLatch.await();
    opersationSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    this.countDownLatch = null;
  }

  public void testSimpleWorkflow() throws Exception {
    String targetClient = "" + System.currentTimeMillis();
    final AVIMClient client = AVIMClient.getInstance(targetClient);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient tmp, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          AVIMConversationsQuery query = client.getConversationsQuery();
          query.setQueryPolicy(AVQuery.CachePolicy.NETWORK_ONLY);
          query.whereEqualTo("tr", true).addAscendingOrder("createdAt")
                  .findInBackground(new AVIMConversationQueryCallback() {
                    @Override
                    public void done(List<AVIMConversation> conversations, AVIMException ex) {
                      if (null != ex) {
                        System.out.println("failed to query convs");
                        ex.printStackTrace();
                        countDownLatch.countDown();
                      } else {
                        System.out.println("succeed to query convs. results=" + conversations.toString());
                        client.close(new AVIMClientCallback() {
                          @Override
                          public void done(AVIMClient client, AVIMException e) {
                            opersationSucceed = (null == e);
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
    assertTrue(opersationSucceed);
  }
}
