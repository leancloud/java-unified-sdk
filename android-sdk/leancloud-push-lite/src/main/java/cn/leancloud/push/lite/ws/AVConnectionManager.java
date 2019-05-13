package cn.leancloud.push.lite.ws;

import android.util.Log;

import org.java_websocket.framing.CloseFrame;

import java.net.URI;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import cn.leancloud.push.lite.AVCallback;
import cn.leancloud.push.lite.AVException;
import cn.leancloud.push.lite.AVInstallation;
import cn.leancloud.push.lite.AVOSCloud;
import cn.leancloud.push.lite.proto.CommandPacket;
import cn.leancloud.push.lite.proto.LoginPacket;
import cn.leancloud.push.lite.proto.Messages;
import cn.leancloud.push.lite.utils.PacketAssembler;
import cn.leancloud.push.lite.utils.StringUtil;

public class AVConnectionManager implements WebSocketClientMonitor {
  private static final String TAG = AVConnectionManager.class.getSimpleName();
  private static AVConnectionManager instance = null;
  private AVStandardWebSocketClient webSocketClient = null;
  private Object webSocketClientWatcher = new Object();
  private String currentRTMConnectionServer = null;
  private int retryConnectionCount = 0;
  private boolean connectionEstablished = false;

  private volatile boolean connecting = false;
  private volatile AVCallback pendingCallback = null;

  private Map<String, AVConnectionListener> connectionListeners = new ConcurrentHashMap<>(1);

  public synchronized static AVConnectionManager getInstance() {
    if (instance == null) {
      instance = new AVConnectionManager(false);
    }
    return instance;
  }

  private AVConnectionManager(boolean autoConnection) {
    connectionListeners.put(AVPushMessageListener.DEFAULT_ID, AVPushMessageListener.getInstance());
    if (autoConnection) {
      startConnection();
    }
  }

  private void resetConnectingStatus(boolean succeed) {
    this.connecting = false;
    if (null != pendingCallback) {
      if (succeed) {
        pendingCallback.internalDone(null);
      } else {
        pendingCallback.internalDone(new AVException(AVException.TIMEOUT, "network timeout."));
      }
    }
    pendingCallback = null;
  }

  private void reConnectionRTMServer() {
    retryConnectionCount++;
    if (retryConnectionCount <= 3) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            long sleepMS = (long)Math.pow(2, retryConnectionCount) * 1000;
            Thread.sleep(sleepMS);
            Log.d(TAG, "reConnect rtm server. count=" + retryConnectionCount);
            startConnectionInternal();
          } catch (InterruptedException ex) {
            Log.w(TAG,"failed to start connection.", ex);
          }
        }
      }).start();
    } else {
      Log.e(TAG, "have tried " + (retryConnectionCount - 1) + " times, stop connecting...");
      resetConnectingStatus(false);
    }
  }

//  private String updateTargetServer(RTMConnectionServerResponse rtmConnectionServerResponse) {
//    String primaryServer = rtmConnectionServerResponse.getServer();
//    String secondary = rtmConnectionServerResponse.getSecondary();
//    if (StringUtil.isEmpty(this.currentRTMConnectionServer) || this.currentRTMConnectionServer.equalsIgnoreCase(secondary)) {
//      this.currentRTMConnectionServer = primaryServer;
//    } else {
//      this.currentRTMConnectionServer = secondary;
//    }
//    return this.currentRTMConnectionServer;
//  }

  private void initWebSocketClient(String targetServer) {
    Log.d(TAG, "try to connect server: " + targetServer);

    SSLSocketFactory sf = null;
    try {
      SSLContext sslContext = SSLContext.getDefault();
      sf = sslContext.getSocketFactory();
    } catch (NoSuchAlgorithmException exception) {
      Log.e(TAG, "failed to get SSLContext, cause: " + exception.getMessage());
    }
    URI targetURI;
    try {
      targetURI = URI.create(targetServer);
    } catch (Exception ex) {
      Log.e(TAG, "failed to parse targetServer:" + targetServer + ", cause:" + ex.getMessage());
      targetURI = null;
    }
    if (null == targetURI) {
      return;
    }

    synchronized (webSocketClientWatcher) {
      if (null != webSocketClient) {
        try {
          webSocketClient.close();
        } catch (Exception ex) {
          Log.e(TAG, "failed to close websocket client.", ex);
        } finally {
          webSocketClient = null;
        }
      }
      int connectTimeout = 30000;// milliseconds
      webSocketClient = new AVStandardWebSocketClient(targetURI, AVStandardWebSocketClient.SUB_PROTOCOL_2_3,
          true, true, sf, connectTimeout, AVConnectionManager.this);
      webSocketClient.connect();
    }
  }

  public void startConnection(AVCallback callback) {
    if (this.connectionEstablished) {
      Log.d(TAG, "connection is established, directly response callback...");
      if (null != callback) {
        callback.internalDone(null);
      }
    } else if (this.connecting) {
      Log.d(TAG, "on starting connection, save callback...");
      if (null != callback) {
        this.pendingCallback = callback;
      }
    } else {
      Log.d(TAG,"start connection with callback...");
      this.connecting = true;
      this.pendingCallback = callback;
      startConnectionInternal();
    }
  }

  public void startConnection() {
    if (this.connectionEstablished) {
      Log.d(TAG, "connection is established...");
    } else if (this.connecting) {
      Log.d(TAG, "on starting connection, ignore.");
      return;
    } else {
      Log.d(TAG, "start connection...");
      this.connecting = true;
      startConnectionInternal();
    }
  }

  private void startConnectionInternal() {
//    String specifiedServer = AVIMOptions.getGlobalOptions().getRtmServer();
//    if (!StringUtil.isEmpty(specifiedServer)) {
//      initWebSocketClient(specifiedServer);
//      return;
//    }
//    final AppRouter appRouter = AppRouter.getInstance();
//    final String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
//    appRouter.getEndpoint(AVOSCloud.getApplicationId(), AVOSService.RTM, retryConnectionCount > 0)
//        .map(new Function<String, RTMConnectionServerResponse>() {
//          @Override
//          public RTMConnectionServerResponse apply(@NonNull String var1) throws Exception {
//            String routerHost = var1.startsWith("http")? var1 : "https://" + var1;
//            return appRouter.fetchRTMConnectionServer(routerHost, AVOSCloud.getApplicationId(), installationId,
//                1, retryConnectionCount < 1).blockingFirst();
//          }
//        }).subscribe(new Observer<RTMConnectionServerResponse>() {
//      @Override
//      public void onSubscribe(Disposable disposable) {
//      }
//
//      @Override
//      public void onNext(RTMConnectionServerResponse rtmConnectionServerResponse) {
//        String targetServer = updateTargetServer(rtmConnectionServerResponse);
//        initWebSocketClient(targetServer);
//      }
//
//      @Override
//      public void onError(Throwable throwable) {
//        Log.e(TAG, "failed to query RTM Connection Server. cause: " + throwable.getMessage());
//        reConnectionRTMServer();
//      }
//
//      @Override
//      public void onComplete() {
//      }
//    });
  }

  public void cleanup() {
    resetConnection();

    this.connectionListeners.clear();
    this.pendingCallback = null;
  }

  void resetConnection() {
    synchronized (webSocketClientWatcher) {
      if (null != webSocketClient) {
        try {
          webSocketClient.closeConnection(CloseFrame.ABNORMAL_CLOSE, "Connectivity broken");
        } catch (Exception ex) {
          Log.e(TAG, "failed to close websocket client.", ex);
        } finally {
          webSocketClient = null;
        }
      }
    }

    connectionEstablished = false;
    retryConnectionCount = 0;
    connecting = false;
  }

  public void subscribeConnectionListener(String clientId, AVConnectionListener listener) {
    if (null != listener) {
      this.connectionListeners.put(clientId, listener);
    }
  }
  public void unsubscribeConnectionListener(String clientId) {
    this.connectionListeners.remove(clientId);
  }

  public void sendPacket(CommandPacket packet) {
    this.webSocketClient.send(packet);
  }

  public boolean isConnectionEstablished() {
    return this.connectionEstablished;
  }

  /**
   * WebSocketClientMonitor interfaces
   */
  public void onOpen() {
    Log.d(TAG, "webSocket established...");
    connectionEstablished = true;
    retryConnectionCount = 0;
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onWebSocketOpen();
    }

    resetConnectingStatus(true);

    // auto send login packet.
    LoginPacket lp = new LoginPacket();
    lp.setAppId(AVOSCloud.getApplicationId());
    lp.setInstallationId(AVInstallation.getCurrentInstallation().getInstallationId());
    this.sendPacket(lp);
  }

  public void onClose(int var1, String var2, boolean var3) {
    Log.d(TAG,"webSocket closed...");
    connectionEstablished = false;
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onWebSocketClose();
    }
  }

  public void onMessage(ByteBuffer bytes) {
    Messages.GenericCommand command = PacketAssembler.getInstance().disassemblePacket(bytes);
    if (null == command) {
      return;
    }

    Log.d(TAG, "downlink: " + command.toString());

    String peerId = command.getPeerId();
    Integer requestKey = command.hasI() ? command.getI() : null;
//    if (command.hasService() && command.getService() == LiveQueryLoginPacket.SERVICE_LIVE_QUERY) {
//      peerId = LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID;
//    } else if (command.getCmd().getNumber() == Messages.CommandType.data_VALUE) {
//      peerId = AVPushMessageListener.DEFAULT_ID;
//    }
    AVConnectionListener listener = this.connectionListeners.get(peerId);
    if (null != listener) {
      listener.onMessageArriving(peerId, requestKey, command);
    } else {
      Log.w(TAG, "no peer subscribed message, ignore it. peerId=" + peerId + ", requestKey=" + requestKey);
    }
  }

  public void onError(Exception exception) {
    connectionEstablished = false;
    reConnectionRTMServer();
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onError(null, null);
    }
  }
}
