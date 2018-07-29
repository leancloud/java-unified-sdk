package cn.leancloud.session;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AVOSService;
import cn.leancloud.core.AppRouter;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.WindTalker;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.push.AVInstallation;
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
  private Boolean connectionEstablished = false;

  private Map<String, AVConnectionListener> connectionListeners = new ConcurrentHashMap<>(1);

  public synchronized static AVConnectionManager getInstance() {
    if (instance == null) {
      instance = new AVConnectionManager();
    }
    return instance;
  }

  private AVConnectionManager() {
    initConnection();
  }

  private void reConnectionRTMServer() {
    retryConnectionCount++;
    if (retryConnectionCount <= 3) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          long sleepMS = (long)Math.pow(2, retryConnectionCount) * 1000;
          try {
            Thread.sleep(sleepMS);
            initConnection();
          } catch (InterruptedException ex) {
            ;
          }
        }
      }).start();
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
    int connectTimeout = AVIMOptions.getGlobalOptions().getTimeoutInSecs();
    if (AVIMOptions.getGlobalOptions().isOnlyPushCount()) {
      webSocketClient = new AVStandardWebSocketClient(URI.create(targetServer), AVStandardWebSocketClient.SUB_PROTOCOL_2_3,
              true, true, sf, connectTimeout, AVConnectionManager.this);
    } else {
      webSocketClient = new AVStandardWebSocketClient(URI.create(targetServer), AVStandardWebSocketClient.SUB_PROTOCOL_2_1,
              true, true, sf, connectTimeout, AVConnectionManager.this);
    }
    webSocketClient.connect();
  }

  private void initConnection() {
    String specifiedServer = AVIMOptions.getGlobalOptions().getRtmServer();
    if (!StringUtil.isEmpty(specifiedServer)) {
      initWebSocketClient(specifiedServer);
      return;
    }
    final AppRouter appRouter = AppRouter.getInstance();
    final String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
    appRouter.getEndpoint(AVOSCloud.getApplicationId(), AVOSService.RTM, retryConnectionCount < 1)
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
    if (!this.connectionEstablished) {
      ;
    }
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
    if (StringUtil.isEmpty(peerId)) {
      peerId = AVIMClient.getDefaultClient();
    }
    AVConnectionListener listener = this.connectionListeners.get(peerId);
    if (null != listener) {
      listener.onMessageArriving(peerId, requestKey, command);
    } else {
      LOGGER.w("");
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
