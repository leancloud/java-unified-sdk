package cn.leancloud.im;

import cn.leancloud.LCException;
import cn.leancloud.LCQuery;
import cn.leancloud.Configure;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMConversationsQuery;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.callback.LCIMClientCallback;
import cn.leancloud.im.v2.callback.LCIMConversationQueryCallback;
import cn.leancloud.session.LCConnectionManager;
import junit.framework.TestCase;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LCIMClientSmokeTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;

  public LCIMClientSmokeTest(String name) {
    super(name);
    Configure.initializeWithApp("", "");
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    LCConnectionManager manager = LCConnectionManager.getInstance();
    final CountDownLatch tmpLatch = new CountDownLatch(1);
    manager.startConnection(new LCCallback() {
      @Override
      protected void internalDone0(Object o, LCException LCException) {
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
    final LCIMClient client = LCIMClient.getInstance(targetClient);
    client.open(new LCIMClientCallback() {
      @Override
      public void done(LCIMClient tmp, LCIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          LCIMConversationsQuery query = client.getConversationsQuery();
          query.setQueryPolicy(LCQuery.CachePolicy.NETWORK_ONLY);
          query.whereEqualTo("tr", true).addAscendingOrder("createdAt")
                  .findInBackground(new LCIMConversationQueryCallback() {
                    @Override
                    public void done(List<LCIMConversation> conversations, LCIMException ex) {
                      if (null != ex) {
                        System.out.println("failed to query convs");
                        ex.printStackTrace();
                        countDownLatch.countDown();
                      } else {
                        System.out.println("succeed to query convs. results=" + conversations.toString());
                        client.close(new LCIMClientCallback() {
                          @Override
                          public void done(LCIMClient client, LCIMException e) {
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
