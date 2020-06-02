package cn.leancloud.websocket;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.command.LoginPacket;
import cn.leancloud.command.SessionControlPacket;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.nio.ByteBuffer;

public class AVStandardWebSocketClientTest extends TestCase {
  private AVStandardWebSocketClient.WebSocketClientMonitor monitor;

  public AVStandardWebSocketClientTest(String testname) {
    super(testname);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
    this.monitor = new AVStandardWebSocketClient.WebSocketClientMonitor() {
      @Override
      public void onOpen(WebSocketClient client) {

      }

      @Override
      public void onClose(WebSocketClient client, int var1, String var2, boolean var3) {

      }

      @Override
      public void onMessage(WebSocketClient client, ByteBuffer bytes) {

      }

      @Override
      public void onError(WebSocketClient client, Exception exception) {

      }
    };
  }

  public static Test suite()
  {
    return new TestSuite( AVStandardWebSocketClientTest.class );
  }

  public void testConnect() throws Exception {
    String wsUrl = "wss://cn-n1-core-k8s-cell-12.leancloud.cn";
    SSLContext sslContext = SSLContext.getDefault();
    SSLSocketFactory sf = sslContext.getSocketFactory();
    AVStandardWebSocketClient client = new AVStandardWebSocketClient(URI.create(wsUrl),
            AVStandardWebSocketClient.SUB_PROTOCOL_2_3,
            true, true, sf, 0, this.monitor);
    boolean rst = client.connectBlocking();
    assertTrue(rst);
    final int requestId = 100;
    final String installation = "d45304813cf37c6c1a2177f84aee0bb8";

    LoginPacket lp = new LoginPacket();
    lp.setAppId(Configure.TEST_APP_ID);
    lp.setInstallationId(installation);
    lp.setRequestId(requestId - 1);
    client.send(lp);
    Thread.sleep(3000);

    SessionControlPacket scp = SessionControlPacket.genSessionCommand(
            "fengjunwen", null,
            SessionControlPacket.SessionControlOp.OPEN, null,
            0, 0, requestId);
    scp.setTag("mobile");
    scp.setAppId(Configure.TEST_APP_ID);
    scp.setInstallationId(installation);
    scp.setReconnectionRequest(false);
    client.send(scp);

    Thread.sleep(3000);
    client.close();
    Thread.sleep(3000);
  }
}
