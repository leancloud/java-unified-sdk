package cn.leancloud.websocket;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AVStandardWebSocketClient extends WebSocketClient {
  public static final String SUB_PROTOCOL_2_1 = "lc.protobuf2.1";
  public static final String SUB_PROTOCOL_2_3 = "lc.protobuf2.3";

  private static final AVLogger gLogger = LogUtil.getLogger(AVStandardWebSocketClient.class);
  private static final String HEADER_SUB_PROTOCOL = "Sec-WebSocket-Protocol";
  private static final int PING_TIMEOUT_CODE = 3000;

  private SSLSocketFactory socketFactory;
  private HeartBeatPolicy heartBeatPolicy;
  private WebSocketClientMonitor socketClientMonitor;
  private static ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
  static {
    protocols.add(new Protocol(SUB_PROTOCOL_2_3));
  }

  public AVStandardWebSocketClient(URI serverUrl, final String subProtocol, boolean secEnabled, boolean sniEnabled,
                                   SSLSocketFactory socketFactory, int connectTimeout, WebSocketClientMonitor monitor) {
    super(serverUrl, new Draft_6455(Collections.<IExtension>emptyList(), protocols), new HashMap<String, String>() {
      {
        put(HEADER_SUB_PROTOCOL, subProtocol);
      }
    }, connectTimeout);
    this.socketFactory = socketFactory;
    this.heartBeatPolicy = new HeartBeatPolicy() {
      @Override
      public void onTimeOut() {
        closeConnection(PING_TIMEOUT_CODE, "No response for ping");
      }

      @Override
      public void sendPing() {
        ping();
      }
    };
    this.socketClientMonitor = monitor;
    if (secEnabled) {
      setSocket(sniEnabled);
    }
  }

  protected void ping() {
    gLogger.d("send ping packet");
    PingFrame frame = new PingFrame();
    this.sendFrame(frame);
  }

  @Override
  public void onWebsocketPong(WebSocket conn, Framedata f) {
    super.onWebsocketPong(conn, f);
    heartBeatPolicy.onPong();
    gLogger.d("onWebsocketPong()");
  }

  private void setSocket(boolean sniEnabled) {
    try {
      String url = getURI().toString();
      if (!StringUtil.isEmpty(url)) {
        if (url.startsWith("wss") && null != this.socketFactory) {
          Socket socket = socketFactory.createSocket();
          if (sniEnabled) {
            try {
              Class sniHostnameClazz = Class.forName("javax.net.ssl.SNIHostName");
              Class sslSocketClazz = Class.forName("javax.net.ssl.SSLSocket");
              if (null != sniHostnameClazz && null != sslSocketClazz && (socket instanceof javax.net.ssl.SSLSocket)) {
                javax.net.ssl.SNIHostName serverName = new javax.net.ssl.SNIHostName(getURI().getHost());
                List<SNIServerName> serverNames = new ArrayList<SNIServerName>(1);
                serverNames.add(serverName);

                SSLParameters params = ((javax.net.ssl.SSLSocket)socket).getSSLParameters();
                params.setServerNames(serverNames);
                ((javax.net.ssl.SSLSocket)socket).setSSLParameters(params);
              }
            } catch (Exception ex) {
              // for Android OS before Build.VERSION_CODES.N, we can't find out javax.net.ssl.SNIHostName.
              gLogger.w("failed to init SSLSocket. cause: " + ex.getMessage());
            }
          }
          setSocket(socket);
        } else {
          SocketFactory socketFactory = SocketFactory.getDefault();
          setSocket(socketFactory.createSocket());
        }
      }
    } catch (Exception e) {
      gLogger.e("Socket Initializer Error", e);
    }
  }

  public void send(CommandPacket packet) {
    gLogger.d("uplink : " + packet.getGenericCommand().toString());
    try {
      send(packet.getGenericCommand().toByteArray());
    } catch (Exception e) {
      gLogger.e(e.getMessage());
    }
  }

  // WebSocketClient interfaces.
  public void onOpen(ServerHandshake var1) {
    gLogger.d("onOpen status=" + var1.getHttpStatus() + ", statusMsg=" + var1.getHttpStatusMessage());
    this.heartBeatPolicy.start();
    if (null != this.socketClientMonitor) {
      this.socketClientMonitor.onOpen();
    }
  }

  public void onMessage(String var1) {
    gLogger.d("onMessage " + var1);
  }

  public void onMessage(ByteBuffer bytes) {
    if (null != this.socketClientMonitor) {
      this.socketClientMonitor.onMessage(bytes);
    }
  }

  public void onClose(int var1, String var2, boolean var3) {
    gLogger.d("onClose code=" + var1 + ", message=" + var2);
    this.heartBeatPolicy.stop();
    if (null != this.socketClientMonitor) {
      this.socketClientMonitor.onClose(var1, var2, var3);
    }
  }

  public void onError(Exception var1) {
    gLogger.w("onError ", var1);
    if (null != this.socketClientMonitor) {
      this.socketClientMonitor.onError(var1);
    }
  }
  // end of WebSocketClient interfaces.

  public interface WebSocketClientMonitor {
    void onOpen();
    void onClose(int var1, String var2, boolean var3);
    void onMessage(ByteBuffer bytes);
    void onError(Exception exception);
  }
}
