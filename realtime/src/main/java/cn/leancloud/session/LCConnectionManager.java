package cn.leancloud.session;

import cn.leancloud.LCException;
import cn.leancloud.LCLogger;
import cn.leancloud.LCInstallation;
import cn.leancloud.Messages;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.command.LiveQueryLoginPacket;
import cn.leancloud.command.LoginPacket;
import cn.leancloud.command.SessionControlPacket;
import cn.leancloud.core.LeanCloud;
import cn.leancloud.core.LeanService;
import cn.leancloud.core.AppRouter;
import cn.leancloud.im.LCIMOptions;
import cn.leancloud.im.WindTalker;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.livequery.LiveQueryOperationDelegate;
import cn.leancloud.push.LCPushMessageListener;
import cn.leancloud.service.RTMConnectionServerResponse;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.websocket.StandardWebSocketClient;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class LCConnectionManager implements StandardWebSocketClient.WebSocketClientMonitor {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCConnectionManager.class);

  private static LCConnectionManager instance = null;

  enum ConnectionPolicy {
    Keep,
    LetItGone,
    ForceKeep
  }

  enum ConnectionStatus {
    Offline,
    Connecting,
    Connected
  }

  /*
   * socket related.
   */
  private StandardWebSocketClient webSocketClient = null;
  private final Object webSocketClientWatcher = new Object();
  private String currentRTMConnectionServer = null;
  private final LCInstallation currentInstallation;

  /*
   * connection status related.
   */
  private int retryConnectionCount = 0;
  private volatile ConnectionStatus currentStatus = ConnectionStatus.Offline;
  private volatile ConnectionPolicy connectionPolicy = ConnectionPolicy.Keep;

  private volatile LCCallback pendingCallback = null;

  /*
  * listeners.
  */
  private final Map<String, LCConnectionListener> connectionListeners = new ConcurrentHashMap<>(1);
  private final Map<String, LCConnectionListener> defaultConnectionListeners = new HashMap<>(2);

  public synchronized static LCConnectionManager getInstance() {
    if (instance == null) {
      instance = new LCConnectionManager(LCInstallation.getCurrentInstallation(), false);
    }
    return instance;
  }

  public static LCConnectionManager createInstance(LCInstallation installation) {
    return new LCConnectionManager(installation, false);
  }

  private LCConnectionManager(LCInstallation installation, boolean autoConnection) {
    this.currentInstallation = installation;
    subscribeDefaultConnectionListener(LCPushMessageListener.DEFAULT_ID, LCPushMessageListener.getInstance());
    if (autoConnection) {
      startConnection(new LCCallback() {
        @Override
        protected void internalDone0(Object o, LCException LCException) {
        }
      });
    }
  }

  private void resetConnectingStatus(boolean succeed) {
    this.currentStatus = succeed? ConnectionStatus.Connected: ConnectionStatus.Offline;
    if (null != pendingCallback) {
      if (succeed) {
        pendingCallback.internalDone(null);
      } else {
        pendingCallback.internalDone(new LCException(LCException.TIMEOUT, "network timeout."));
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
      int connectTimeout = LCIMOptions.getGlobalOptions().getTimeoutInSecs() * 1000;// milliseconds
      if (LCIMOptions.getGlobalOptions().isOnlyPushCount()) {
        webSocketClient = new StandardWebSocketClient(targetURI, StandardWebSocketClient.SUB_PROTOCOL_2_3,
                true, true, sf, connectTimeout, LCConnectionManager.this);
      } else {
        webSocketClient = new StandardWebSocketClient(targetURI, StandardWebSocketClient.SUB_PROTOCOL_2_1,
                true, true, sf, connectTimeout, LCConnectionManager.this);
      }
      webSocketClient.connect();
    }
  }

  public void startConnection(LCCallback callback) {
    startConnection(callback, false);
  }

  private void startConnection(LCCallback callback, boolean ignoreGone) {
    if (ConnectionStatus.Connected == this.currentStatus) {
      LOGGER.d("connection is established, directly response callback...");
      if (null != callback) {
        callback.internalDone(null);
      }
    } else if (ConnectionStatus.Connecting == this.currentStatus) {
      LOGGER.d("on starting connection, save callback...");
      if (null != callback) {
        this.pendingCallback = callback;
      }
    } else {
      if (ignoreGone && ConnectionPolicy.LetItGone == connectionPolicy) {
        LOGGER.d("ignore auto establish connection for policy:ConnectionPolicy.LetItGone...");
      } else {
        LOGGER.d("start connection with callback...");
        currentStatus = ConnectionStatus.Connecting;
        this.pendingCallback = callback;
        startConnectionInternal();
      }
    }
  }

  public void autoConnection() {
    startConnection(null, true);
  }

  private void startConnectionInternal() {
    String specifiedServer = LCIMOptions.getGlobalOptions().getRtmServer();
    if (!StringUtil.isEmpty(specifiedServer)) {
      initWebSocketClient(specifiedServer);
      return;
    }
    final AppRouter appRouter = AppRouter.getInstance();
    final String installationId = currentInstallation.getInstallationId();
    appRouter.getEndpoint(LeanCloud.getApplicationId(), LeanService.RTM).subscribe(new Observer<String>() {
              public void onSubscribe(@NonNull Disposable var1) { }

              public void onNext(@NonNull String var1) {
                if (StringUtil.isEmpty(var1)) {
                  LOGGER.e("failed to get RTM Endpoint. cause: push router url is emptry.");
                  reConnectionRTMServer();
                  return;
                }
                String routerHost = var1.startsWith("http")? var1 : "https://" + var1;
                appRouter.fetchRTMConnectionServer(routerHost, LeanCloud.getApplicationId(), installationId,
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
    currentStatus = ConnectionStatus.Offline;

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
  }

  public void subscribeConnectionListener(String clientId, LCConnectionListener listener) {
    if (null != listener) {
      this.connectionListeners.put(clientId, listener);
    }
  }

  public void subscribeDefaultConnectionListener(String clientId, LCConnectionListener listener) {
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
        boolean sessionCommandFound = SessionControlPacket.SESSION_COMMAND.equals(packet.getCmd());
        if (sessionCommandFound) {
          connectionPolicy = ConnectionPolicy.ForceKeep;
        }
        this.webSocketClient.send(packet);
      } else {
        LOGGER.w("StateException: web socket client is null, drop CommandPacket: " + packet);
      }
    }
  }

  public boolean isConnectionEstablished() {
    return ConnectionStatus.Connected == this.currentStatus;
  }

  /**
   * WebSocketClientMonitor interfaces
   */
  public void onOpen(WebSocketClient client) {
    LOGGER.d("webSocket(client=" + client + ") established...");
    currentStatus = ConnectionStatus.Connected;
    retryConnectionCount = 0;

    // auto send login packet.
    if (!LCIMOptions.getGlobalOptions().isDisableAutoLogin4Push()) {
      LCIMOptions globalOptions = LCIMOptions.getGlobalOptions();
      LoginPacket lp = new LoginPacket();
      lp.setAppId(LeanCloud.getApplicationId());
      lp.setInstallationId(currentInstallation.getInstallationId());
      if (null != globalOptions.getSystemReporter()) {
        lp.setSystemInfo(globalOptions.getSystemReporter().getInfo());
      }
      sendPacket(lp);
    }

    initSessionsIfExists();

    resetConnectingStatus(true);

    for (LCConnectionListener listener: connectionListeners.values()) {
      listener.onWebSocketOpen();
    }
    for (LCConnectionListener listener: defaultConnectionListeners.values()) {
      listener.onWebSocketOpen();
    }
  }

  private void initSessionsIfExists() {
    Map<String, String> cachedSessions = SessionCacheHelper.getTagCacheInstance().getAllSession();
    for (Map.Entry<String, String> entry : cachedSessions.entrySet()) {
      LCSession s = LCSessionManager.getInstance().getOrCreateSession(entry.getKey(), currentInstallation.getInstallationId(), this);
      s.setTag(entry.getValue());
      s.setSessionStatus(LCSession.Status.Closed);
      LCDefaultConnectionListener defaultSessionListener = new LCDefaultConnectionListener(s);
      subscribeConnectionListener(entry.getKey(), defaultSessionListener);
    }
  }

  public void onClose(WebSocketClient client, int var1, String var2, boolean var3) {
    LOGGER.d("client(" + client + ") closed...");
    currentStatus = ConnectionStatus.Offline;
    for (LCConnectionListener listener: connectionListeners.values()) {
      listener.onWebSocketClose();
    }
    for (LCConnectionListener listener: defaultConnectionListeners.values()) {
      listener.onWebSocketClose();
    }
  }

  public void onMessage(WebSocketClient client, ByteBuffer bytes) {
    WindTalker windTalker = WindTalker.getInstance();
    Messages.GenericCommand command = windTalker.disassemblePacket(bytes);
    if (null == command) {
      LOGGER.w("client(" + client + ") downlink: invalid command.");
      return;
    }

    LOGGER.d("client(" + client + ") downlink: " + command.toString());

    String peerId = command.getPeerId();
    Integer requestKey = command.hasI() ? command.getI() : null;
    if (command.hasService() && command.getService() == LiveQueryLoginPacket.SERVICE_LIVE_QUERY) {
      peerId = LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID;
    } else if (command.getCmd().getNumber() == Messages.CommandType.data_VALUE) {
      peerId = LCPushMessageListener.DEFAULT_ID;
    } else if (StringUtil.isEmpty(peerId)) {
      peerId = LCIMClient.getDefaultClient();
    }
    if (command.hasService() && command.getService() == LiveQueryLoginPacket.SERVICE_PUSH
            && command.getCmd().getNumber() == Messages.CommandType.loggedin_VALUE) {
      // push login response.
      Messages.LoggedinCommand loggedinCommand = command.getLoggedinMessage();
      if (null != loggedinCommand && loggedinCommand.hasPushDisabled()) {
        boolean pushDisabled = loggedinCommand.getPushDisabled();
        if (pushDisabled) {
          LOGGER.i("received close connection instruction from server.");
          if (ConnectionPolicy.ForceKeep != connectionPolicy) {
            connectionPolicy = ConnectionPolicy.LetItGone;
          }
        }
      }
      return;
    }
    LCConnectionListener listener = this.connectionListeners.get(peerId);
    if (null == listener) {
      listener = this.defaultConnectionListeners.get(peerId);
    }
    if (null != listener) {
      listener.onMessageArriving(peerId, requestKey, command);
    } else {
      LOGGER.w("no peer subscribed message, ignore it. peerId=" + peerId + ", requestKey=" + requestKey);
    }
  }

  public void onError(WebSocketClient client, Exception exception) {
    LOGGER.d("AVConnectionManager onError. client:" + client + ", exception:"
            + ((null != exception)? exception.getMessage(): "null"));
    currentStatus = ConnectionStatus.Offline;
    reConnectionRTMServer();
    for (LCConnectionListener listener: connectionListeners.values()) {
      listener.onError(null, null);
    }
    for (LCConnectionListener listener: defaultConnectionListeners.values()) {
      listener.onError(null, null);
    }
  }

}
