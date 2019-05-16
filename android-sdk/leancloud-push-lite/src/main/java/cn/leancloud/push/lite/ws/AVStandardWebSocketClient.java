package cn.leancloud.push.lite.ws;

import android.os.Build;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;

import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import cn.leancloud.push.lite.AVOSCloud;
import cn.leancloud.push.lite.proto.CommandPacket;
import cn.leancloud.push.lite.utils.StringUtil;

public class AVStandardWebSocketClient extends WebSocketClient {
  //public static final String SUB_PROTOCOL_2_1 = "lc.protobuf2.1";
  public static final String SUB_PROTOCOL_2_3 = "lc.protobuf2.3";

  private static final String HEADER_SUB_PROTOCOL = "Sec-WebSocket-Protocol";
  private static final int PING_TIMEOUT_CODE = 3000;
  private static final String TAG = AVStandardWebSocketClient.class.getSimpleName();

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
    PingFrame frame = new PingFrame();
    this.sendFrame(frame);
  }

  private void setSocket(boolean sniEnabled) {
    try {
      String url = getURI().toString();
      if (!StringUtil.isEmpty(url)) {
        if (url.startsWith("wss") && null != this.socketFactory) {
          Socket socket = socketFactory.createSocket();
          if (sniEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
              Log.w("WebSocketClient", ex.getMessage());
            }
          }
          setSocket(socket);
        } else {
          SocketFactory socketFactory = SocketFactory.getDefault();
          setSocket(socketFactory.createSocket());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onWebsocketPong(WebSocket conn, Framedata f) {
    super.onWebsocketPong(conn, f);
    heartBeatPolicy.onPong();
  }

  public void send(CommandPacket packet) {
    if (AVOSCloud.isDebugLogEnabled()) {
      Log.d(TAG, "uplink : " + packet.getGenericCommand().toString());
    }
    try {
      send(packet.getGenericCommand().toByteArray());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // WebSocketClient interfaces.
  public void onOpen(ServerHandshake var1) {
    this.heartBeatPolicy.start();
    if (null != this.socketClientMonitor) {
      this.socketClientMonitor.onOpen();
    }
  }

  public void onMessage(String var1) { }

  public void onMessage(ByteBuffer bytes) {
    if (null != this.socketClientMonitor) {
      this.socketClientMonitor.onMessage(bytes);
    }
  }

  public void onClose(int var1, String var2, boolean var3) {
    if (AVOSCloud.isDebugLogEnabled()) {
      Log.d(TAG, "onClose code=" + var1 + ", message=" + var2);
    }
    this.heartBeatPolicy.stop();
    if (null != this.socketClientMonitor) {
      this.socketClientMonitor.onClose(var1, var2, var3);
    }
  }

  public void onError(Exception var1) {
    Log.w(TAG, "onError ", var1);
    if (null != this.socketClientMonitor) {
      this.socketClientMonitor.onError(var1);
    }
  }
}
