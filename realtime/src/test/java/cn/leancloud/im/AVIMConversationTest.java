package cn.leancloud.im;

import cn.leancloud.Configure;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCreatedCallback;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import cn.leancloud.session.AVConnectionManager;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVIMConversationTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;
  private AVIMClient client = AVIMClient.getInstance("testUser1");
  private List<String> memebers = Arrays.asList("User2", "User3");
  private String convName = "RealtimeUnitTest";

  public AVIMConversationTest(String suiteName) {
    super(suiteName);
    Configure.initialize();
    AVConnectionManager manager = AVConnectionManager.getInstance();
    try {
      Thread.sleep(3000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    opersationSucceed = false;
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
  }

  @Override
  protected void tearDown() throws Exception {
    this.countDownLatch = null;
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.close(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
  }

  public void testSendTextMessage() throws Exception {
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
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
