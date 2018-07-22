package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.Messages;
import cn.leancloud.command.LoginPacket;
import cn.leancloud.command.SessionControlPacket;
import cn.leancloud.core.AVOSCloud;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class AVConnectionManagerTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private AVConnectionListener listener = new AVConnectionListener() {
    @Override
    public void onWebSocketOpen() {
      System.out.println("ConnectionListener - WebSocket opened.");
      countDownLatch.countDown();
    }

    @Override
    public void onWebSocketClose() {
      System.out.println("ConnectionListener - WebSocket closed.");
    }

    @Override
    public void onDirectCommand(Messages.DirectCommand directCommand) {

    }

    @Override
    public void onSessionCommand(String op, Integer requestId, Messages.SessionCommand command) {
      System.out.println("ConnectionListener - onSessionCommand: op=" + op + ", requestId=" + requestId);
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
    manager.setConnectionListener(this.listener);
    this.countDownLatch.await();
    assertTrue(manager.isConnectionEstablished());
  }

  public void testSwitchConnection() throws Exception {
    ;
  }

  public void testAutoReconnection() throws Exception {
    ;
  }

  public void testLogin() throws Exception {
    AVConnectionManager manager = AVConnectionManager.getInstance();
    manager.setConnectionListener(this.listener);
    this.countDownLatch.await();
    assertTrue(manager.isConnectionEstablished());

    final int requestId = 100;
    final String installation = "d45304813cf37c6c1a2177f84aee0bb8";

    LoginPacket lp = new LoginPacket();
    lp.setAppId(Configure.TEST_APP_ID);
    lp.setInstallationId(installation);
    lp.setRequestId(requestId - 1);
    manager.sendPacket(lp);
    Thread.sleep(3000);

    SessionControlPacket scp = SessionControlPacket.genSessionCommand(
            "fengjunwen", null,
            SessionControlPacket.SessionControlOp.OPEN, null,
            0, 0, requestId);
    scp.setTag("mobile");
    scp.setAppId(Configure.TEST_APP_ID);
    scp.setInstallationId(installation);
    scp.setReconnectionRequest(false);
    manager.sendPacket(scp);

    Thread.sleep(3000);

  }
}
