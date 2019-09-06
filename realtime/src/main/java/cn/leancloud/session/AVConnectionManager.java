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
import org.java_websocket.framing.CloseFrame;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class AVConnectionManager implements AVStandardWebSocketClient.WebSocketClientMonitor {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVConnectionManager.class);

  private static AVConnectionManager instance = null;
  private AVStandardWebSocketClient webSocketClient = null;
  private Object webSocketClientWatcher = new Object();
  private String currentRTMConnectionServer = null;
  private int retryConnectionCount = 0;

  private volatile boolean connectionEstablished = false;
  private volatile boolean connecting = false;
  private volatile AVCallback pendingCallback = null;

  private Map<String, AVConnectionListener> connectionListeners = new ConcurrentHashMap<>(1);
  private Map<String, AVConnectionListener> defaultConnectionListeners = new HashMap<>(2);

  public synchronized static AVConnectionManager getInstance() {
    if (instance == null) {
      instance = new AVConnectionManager(false);
    }
    return instance;
  }

  private AVConnectionManager(boolean autoConnection) {
    subscribeDefaultConnectionListener(AVPushMessageListener.DEFAULT_ID, AVPushMessageListener.getInstance());
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
    if (StringUtil.isEmpty(this.currentRTMConnectionServer) || this.currentRTMConnectionServer.equalsIgnoreCase(secondary)) {
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
    URI targetURI;
    try {
      targetURI = URI.create(targetServer);
    } catch (Exception ex) {
      LOGGER.e("failed to parse targetServer:" + targetServer + ", cause:" + ex.getMessage());
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
          LOGGER.e("failed to close websocket client.", ex);
        } finally {
          webSocketClient = null;
        }
      }
      int connectTimeout = AVIMOptions.getGlobalOptions().getTimeoutInSecs() * 1000;// milliseconds
      if (AVIMOptions.getGlobalOptions().isOnlyPushCount()) {
        webSocketClient = new AVStandardWebSocketClient(targetURI, AVStandardWebSocketClient.SUB_PROTOCOL_2_3,
                true, true, sf, connectTimeout, AVConnectionManager.this);
      } else {
        webSocketClient = new AVStandardWebSocketClient(targetURI, AVStandardWebSocketClient.SUB_PROTOCOL_2_1,
                true, true, sf, connectTimeout, AVConnectionManager.this);
      }
      webSocketClient.connect();
    }
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
    appRouter.getEndpoint(AVOSCloud.getApplicationId(), AVOSService.RTM).subscribe(new Observer<String>() {
              public void onSubscribe(@NonNull Disposable var1) { }

              public void onNext(@NonNull String var1) {
                String routerHost = var1.startsWith("http")? var1 : "https://" + var1;
                appRouter.fetchRTMConnectionServer(routerHost, AVOSCloud.getApplicationId(), installationId,
                        1, retryConnectionCount < 1)
                        .subscribe(new Observer<RTMConnectionServerResponse>() {
                  @Override
                  public void onSubscribe(Disposable disposable) { }

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
                  public void onComplete() { }
                });
              }

              public void onError(@NonNull Throwable var1) {
                LOGGER.e("failed to get RTM Endpoint. cause: " + var1.getMessage());
                reConnectionRTMServer();
              }

              public void onComplete() { }
            });
  }

  public void cleanup() {
    resetConnection();

    this.connectionListeners.clear();
    this.pendingCallback = null;
  }

  public void resetConnection() {
    connectionEstablished = false;

    synchronized (webSocketClientWatcher) {
      if (null != webSocketClient) {
        try {
          webSocketClient.closeConnection(CloseFrame.ABNORMAL_CLOSE, "Connectivity broken");
        } catch (Exception ex) {
          LOGGER.e("failed to close websocket client.", ex);
        } finally {
          webSocketClient = null;
        }
      }
    }

    retryConnectionCount = 0;
    connecting = false;
  }

  public void subscribeConnectionListener(String clientId, AVConnectionListener listener) {
    if (null != listener) {
      this.connectionListeners.put(clientId, listener);
    }
  }

  public void subscribeDefaultConnectionListener(String clientId, AVConnectionListener listener) {
    if (null != listener) {
      this.defaultConnectionListeners.put(clientId, listener);
    }
  }

  public void unsubscribeConnectionListener(String clientId) {
    this.connectionListeners.remove(clientId);
  }

  public void sendPacket(CommandPacket packet) {
    synchronized (webSocketClientWatcher) {
      if (null != this.webSocketClient) {
        this.webSocketClient.send(packet);
      } else {
        LOGGER.w("StateException: web socket client is null, drop CommandPacket: " + packet);
      }
    }
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
    resetConnectingStatus(true);

    // auto send login packet.
    LoginPacket lp = new LoginPacket();
    lp.setAppId(AVOSCloud.getApplicationId());
    lp.setInstallationId(AVInstallation.getCurrentInstallation().getInstallationId());
    sendPacket(lp);

    initSessionsIfExists();
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onWebSocketOpen();
    }
    for (AVConnectionListener listener: defaultConnectionListeners.values()) {
      listener.onWebSocketOpen();
    }
  }

  private void initSessionsIfExists() {
    Map<String, String> cachedSessions = AVSessionCacheHelper.getTagCacheInstance().getAllSession();
    for (Map.Entry<String, String> entry : cachedSessions.entrySet()) {
      AVSession s = AVSessionManager.getInstance().getOrCreateSession(entry.getKey());
      s.setTag(entry.getValue());
      s.setSessionStatus(AVSession.Status.Closed);
      AVDefaultConnectionListener defaultSessionListener = new AVDefaultConnectionListener(s);
      subscribeConnectionListener(entry.getKey(), defaultSessionListener);
    }
  }

  public void onClose(int var1, String var2, boolean var3) {
    LOGGER.d("webSocket closed...");
    connectionEstablished = false;
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onWebSocketClose();
    }
    for (AVConnectionListener listener: defaultConnectionListeners.values()) {
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
    if (command.hasService() && command.getService() == LiveQueryLoginPacket.SERVICE_PUSH
            && command.getCmd().getNumber() == Messages.CommandType.loggedin_VALUE) {
      // push login response.
      return;
    }
    AVConnectionListener listener = this.connectionListeners.get(peerId);
    if (null == listener) {
      listener = this.defaultConnectionListeners.get(peerId);
    }
    if (null != listener) {
      listener.onMessageArriving(peerId, requestKey, command);
    } else {
      LOGGER.w("no peer subscribed message, ignore it. peerId=" + peerId + ", requestKey=" + requestKey);
    }
  }

  public void onError(Exception exception) {
    LOGGER.d("webSocket onError. exception:" + ((null != exception)? exception.getMessage(): "null"));
    connectionEstablished = false;
    reConnectionRTMServer();
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onError(null, null);
    }
    for (AVConnectionListener listener: defaultConnectionListeners.values()) {
      listener.onError(null, null);
    }
  }

}
