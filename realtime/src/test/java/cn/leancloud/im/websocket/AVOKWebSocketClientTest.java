package cn.leancloud.im.websocket;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.im.command.LoginPacket;
import cn.leancloud.im.command.SessionControlPacket;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import okhttp3.Response;
import okio.ByteString;

public class AVOKWebSocketClientTest extends TestCase {
  private AVOKWebSocketClient client = null;
  public AVOKWebSocketClientTest(String testname) {
    super(testname);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public static Test suite()
  {
    return new TestSuite( AVOKWebSocketClientTest.class );
  }

  public void testConnect() throws Exception {
    String wsUrl = "wss://rtm51.leancloud.cn";
    client = new AVOKWebSocketClient(new WsStatusListener(){
      public void onOpen(Response response) {
        System.out.println("websockdet opened!");
        final int requestId = 100;
        final String installation = "d45304813cf37c6c1a2177f84aee0bb8";

        LoginPacket lp = new LoginPacket();
        lp.setAppId(Configure.TEST_APP_ID);
        lp.setInstallationId(installation);
        lp.setRequestId(requestId - 1);
        System.out.println("uplink: " + lp.getGenericCommand().toString());
        client.sendMessage(lp.getGenericCommand().toString());

        new Thread() {
          @Override
          public void run() {
            SessionControlPacket scp = SessionControlPacket.genSessionCommand(
                    "fengjunwen", null,
                    SessionControlPacket.SessionControlOp.OPEN, null,
                    0, 0, requestId);
            scp.setTag("mobile");
            scp.setAppId(Configure.TEST_APP_ID);
            scp.setInstallationId(installation);
            scp.setReconnectionRequest(false);
            System.out.println("uplink: " + scp.getGenericCommand().toString());
            client.sendMessage(ByteString.of(scp.getGenericCommand().toByteString().asReadOnlyByteBuffer()));
            System.out.println("send open command.");
          }
        }.start();

      }
      public void onMessage(String text) {

      }
      public void onMessage(ByteString bytes) {

      }
      public void onReconnect() {

      }
      public void onClosing(int code, String reason) {

      }
      public void onClosed(int code, String reason) {

      }
      public void onFailure(Throwable t, Response response) {

      }
    }, true);
    client.connect(wsUrl);
    Thread.sleep(10000);
    client.close();
    Thread.sleep(3000);
  }
}
