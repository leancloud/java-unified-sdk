package cn.leancloud.im.websocket;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.im.command.LoginPacket;
import cn.leancloud.im.command.SessionControlPacket;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

public class AVStandardWebSocketClientTest extends TestCase {

  public AVStandardWebSocketClientTest(String testname) {
    super(testname);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public static Test suite()
  {
    return new TestSuite( AVStandardWebSocketClientTest.class );
  }

  public void testConnect() throws Exception {
    String wsUrl = "wss://rtm51.leancloud.cn";
    SSLContext sslContext = SSLContext.getDefault();
    SSLSocketFactory sf = sslContext.getSocketFactory();
    AVStandardWebSocketClient client = new AVStandardWebSocketClient(URI.create(wsUrl),
            AVStandardWebSocketClient.SUB_PROTOCOL_2_3,
            true, true, sf);
    boolean rst = client.connectBlocking();
    assertTrue(rst);
    final int requestId = 100;
    final String installation = "d45304813cf37c6c1a2177f84aee0bb8";

    LoginPacket lp = new LoginPacket();
    lp.setAppId(Configure.TEST_APP_ID);
    lp.setInstallationId(installation);
    lp.setRequestId(requestId - 1);
    System.out.println("uplink: " + lp.getGenericCommand().toString());
    client.send(lp);

    SessionControlPacket scp = SessionControlPacket.genSessionCommand(
            "fengjunwen", null,
            SessionControlPacket.SessionControlOp.OPEN, null,
            0, 0, requestId);
    scp.setTag("mobile");
    scp.setAppId(Configure.TEST_APP_ID);
    scp.setInstallationId(installation);
    scp.setReconnectionRequest(false);
    System.out.println("uplink: " + scp.getGenericCommand().toString());
    client.send(scp);

    Thread.sleep(10000);
    client.close();
    Thread.sleep(3000);
  }
}
