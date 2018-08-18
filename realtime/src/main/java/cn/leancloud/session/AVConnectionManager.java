package cn.leancloud.session;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.command.LiveQueryLoginPacket;
import cn.leancloud.command.LoginPacket;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AVOSService;
import cn.leancloud.core.AppRouter;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.WindTalker;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.livequery.LiveQueryOperationDelegate;
import cn.leancloud.AVInstallation;
import cn.leancloud.push.AVPushMessageListener;
import cn.leancloud.service.RTMConnectionServerResponse;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.websocket.AVStandardWebSocketClient;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AVConnectionManager implements AVStandardWebSocketClient.WebSocketClientMonitor {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVConnectionManager.class);

  private static AVConnectionManager instance = null;
  private AVStandardWebSocketClient webSocketClient = null;
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
            LOGGER.d("reConnect rtm server. count=" + retryConnectionCount);
            startConnectionInternal();
          } catch (InterruptedException ex) {
            LOGGER.w("failed to start connection.", ex);
          }
        }
      }).start();
    } else {
      LOGGER.e("have tried " + (retryConnectionCount - 1) + " times, stop connecting...");
      resetConnectingStatus(false);
    }
  }

  private String updateTargetServer(RTMConnectionServerResponse rtmConnectionServerResponse) {
    String primaryServer = rtmConnectionServerResponse.getServer();
    String secondary = rtmConnectionServerResponse.getSecondary();
    if (null == this.currentRTMConnectionServer || this.currentRTMConnectionServer.equalsIgnoreCase(secondary)) {
      this.currentRTMConnectionServer = primaryServer;
    } else {
      this.currentRTMConnectionServer = secondary;
    }
    return this.currentRTMConnectionServer;
  }

  private void initWebSocketClient(String targetServer) {
    LOGGER.d("try to connect server: " + targetServer);

    SSLSocketFactory sf = null;
    try {
      SSLContext sslContext = SSLContext.getDefault();
      sf = sslContext.getSocketFactory();
    } catch (NoSuchAlgorithmException exception) {
      LOGGER.e("failed to get SSLContext, cause: " + exception.getMessage());
    }
    if (null != webSocketClient) {
      try {
        webSocketClient.close();
      } catch (Exception ex) {
        LOGGER.e("failed to close websocket client.", ex);
      } finally {
        webSocketClient = null;
      }
    }
    int connectTimeout = AVIMOptions.getGlobalOptions().getTimeoutInSecs() * 1000;// milliseconds
    if (AVIMOptions.getGlobalOptions().isOnlyPushCount()) {
      webSocketClient = new AVStandardWebSocketClient(URI.create(targetServer), AVStandardWebSocketClient.SUB_PROTOCOL_2_3,
              true, true, sf, connectTimeout, AVConnectionManager.this);
    } else {
      webSocketClient = new AVStandardWebSocketClient(URI.create(targetServer), AVStandardWebSocketClient.SUB_PROTOCOL_2_1,
              true, true, sf, connectTimeout, AVConnectionManager.this);
    }
    webSocketClient.connect();
  }

  public void startConnection(AVCallback callback) {
    if (this.connectionEstablished) {
      LOGGER.d("connection is established, directly response callback...");
      if (null != callback) {
        callback.internalDone(null);
      }
    } else if (this.connecting) {
      LOGGER.d("on starting connection, save callback...");
      if (null != callback) {
        this.pendingCallback = callback;
      }
    } else {
      LOGGER.d("start connection with callback...");
      this.connecting = true;
      this.pendingCallback = callback;
      startConnectionInternal();
    }
  }

  public void startConnection() {
    if (this.connectionEstablished) {
      LOGGER.d("connection is established...");
    } else if (this.connecting) {
      LOGGER.d("on starting connection, ignore.");
      return;
    } else {
      LOGGER.d("start connection...");
      this.connecting = true;
      startConnectionInternal();
    }
  }

  private void startConnectionInternal() {
    String specifiedServer = AVIMOptions.getGlobalOptions().getRtmServer();
    if (!StringUtil.isEmpty(specifiedServer)) {
      initWebSocketClient(specifiedServer);
      return;
    }
    final AppRouter appRouter = AppRouter.getInstance();
    final String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
    appRouter.getEndpoint(AVOSCloud.getApplicationId(), AVOSService.RTM, retryConnectionCount > 0)
            .map(new Function<String, RTMConnectionServerResponse>() {
              @Override
              public RTMConnectionServerResponse apply(@NonNull String var1) throws Exception {
                String routerHost = var1.startsWith("http")? var1 : "https://" + var1;
                return appRouter.fetchRTMConnectionServer(routerHost, AVOSCloud.getApplicationId(), installationId,
                        1, retryConnectionCount < 1).blockingFirst();
              }
            }).subscribe(new Observer<RTMConnectionServerResponse>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(RTMConnectionServerResponse rtmConnectionServerResponse) {
        String targetServer = updateTargetServer(rtmConnectionServerResponse);
        initWebSocketClient(targetServer);
      }

      @Override
      public void onError(Throwable throwable) {
        LOGGER.e("failed to query RTM Connection Server. cause: " + throwable.getMessage());
        reConnectionRTMServer();
      }

      @Override
      public void onComplete() {
      }
    });
  }

  public void cleanup() {
    if (null != webSocketClient) {
      try {
        webSocketClient.close();
      } catch (Exception ex) {
        LOGGER.e("failed to close websocket client.", ex);
      } finally {
        webSocketClient = null;
      }
    }
    this.connectionListeners.clear();
    connectionEstablished = false;
    retryConnectionCount = 0;
    this.connecting = false;
    this.pendingCallback = null;
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
    LOGGER.d("webSocket established...");
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
    LOGGER.d("webSocket closed...");
    connectionEstablished = false;
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onWebSocketClose();
    }
  }

  public void onMessage(ByteBuffer bytes) {
    WindTalker windTalker = WindTalker.getInstance();
    Messages.GenericCommand command = windTalker.disassemblePacket(bytes);
    if (null == command) {
      return;
    }

    LOGGER.d("downlink: " + command.toString());

    String peerId = command.getPeerId();
    Integer requestKey = command.hasI() ? command.getI() : null;
    if (command.hasService() && command.getService() == LiveQueryLoginPacket.SERVICE_LIVE_QUERY) {
      peerId = LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID;
    } else if (command.getCmd().getNumber() == Messages.CommandType.data_VALUE) {
      peerId = AVPushMessageListener.DEFAULT_ID;
    } else if (StringUtil.isEmpty(peerId)) {
      peerId = AVIMClient.getDefaultClient();
    }
    AVConnectionListener listener = this.connectionListeners.get(peerId);
    if (null != listener) {
      listener.onMessageArriving(peerId, requestKey, command);
    } else {
      LOGGER.w("no peer subscribed message, ignore it. peerId=" + peerId + ", requestKey=" + requestKey);
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
