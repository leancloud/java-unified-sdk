package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.Messages;
import cn.leancloud.core.AVOSCloud;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class AVConnectionManagerTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private AVConnectionListener listener = new AVConnectionListener() {
    @Override
    public void onWebSocketOpen() {
      countDownLatch.countDown();
    }

    @Override
    public void onWebSocketClose() {
      ;
    }

    @Override
    public void onDirectCommand(Messages.DirectCommand directCommand) {

    }

    @Override
    public void onSessionCommand(String op, Integer requestId, Messages.SessionCommand command) {

    }

    @Override
    public void onAckCommand(Integer requestKey, Messages.AckCommand ackCommand) {

    }

    @Override
    public void onMessageReceipt(Messages.RcpCommand rcpCommand) {

    }

    @Override
    public void onReadCmdReceipt(Messages.RcpCommand rcpCommand) {

    }

    @Override
    public void onListenerAdded(boolean open) {

    }

    @Override
    public void onListenerRemoved() {

    }

    @Override
    public void onBlacklistCommand(String operation, Integer requestKey, Messages.BlacklistCommand blacklistCommand) {

    }

    @Override
    public void onConversationCommand(String operation, Integer requestKey, Messages.ConvCommand convCommand) {

    }

    @Override
    public void onError(Integer requestKey, Messages.ErrorCommand errorCommand) {

    }

    @Override
    public void onHistoryMessageQuery(Integer requestKey, Messages.LogsCommand command) {

    }

    @Override
    public void onUnreadMessagesCommand(Messages.UnreadCommand unreadCommand) {

    }

    @Override
    public void onMessagePatchCommand(boolean isModify, Integer requestKey, Messages.PatchCommand command) {

    }
  };

  public AVConnectionManagerTest(String testName) {
    super(testName);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
  }

  @Override
  protected void tearDown() throws Exception {
    this.countDownLatch = null;
  }

  public void testInitConnection() throws Exception {
    AVConnectionManager manager = AVConnectionManager.getInstance();
    this.countDownLatch.await();
    assertTrue(manager.isConnectionEstablished());
  }
}
