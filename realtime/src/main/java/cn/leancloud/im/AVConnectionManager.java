package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.command.LiveQueryLoginPacket;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AVOSServices;
import cn.leancloud.core.AppRouter;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.livequery.AVLiveQuery;
import cn.leancloud.push.AVInstallation;
import cn.leancloud.service.RTMConnectionServerResponse;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.websocket.AVStandardWebSocketClient;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AVConnectionManager implements AVStandardWebSocketClient.WebSocketClientMonitor {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVConnectionManager.class);

  private static AVConnectionManager instance = null;
  private AVStandardWebSocketClient webSocketClient = null;
  private AVConnectionListener connectionListener = null;
  private String currentRTMConnectionServer = null;
  private int retryConnectionCount = 0;
  private boolean connectionEstablished = false;

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
    appRouter.getEndpoint(AVOSCloud.getApplicationId(), AVOSServices.RTM, retryConnectionCount < 1)
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
  public AVConnectionListener getConnectionListener() {
    return connectionListener;
  }

  public void setConnectionListener(AVConnectionListener connectionListener) {
    this.connectionListener = connectionListener;
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
    if (null != this.connectionListener) {
      this.connectionListener.onWebSocketOpen();
    }
  }

  public void onClose(int var1, String var2, boolean var3) {
    LOGGER.d("webSocket closed...");
    connectionEstablished = false;
    if (null != this.connectionListener) {
      this.connectionListener.onWebSocketClose();
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
    if (command.getCmd().getNumber() == Messages.CommandType.loggedin_VALUE) {
      if (LiveQueryLoginPacket.SERVICE_LIVE_QUERY == command.getService()) {
        processLoggedinCommand(requestKey);
      }
    } else if (command.getCmd().getNumber() == Messages.CommandType.data_VALUE) {
      switch (command.getCmd().getNumber()) {
        case Messages.CommandType.data_VALUE:
          if (!command.hasService()) {
            processDataCommand(command.getDataMessage());
          } else {
            final int service = command.getService();
            if (LiveQueryLoginPacket.SERVICE_PUSH == service) {
              processDataCommand(command.getDataMessage());
            } else if (LiveQueryLoginPacket.SERVICE_LIVE_QUERY == service) {
              processLiveQueryData(command.getDataMessage());
            }
          }
          break;
        case Messages.CommandType.direct_VALUE:
          processDirectCommand(peerId, command.getDirectMessage());
          break;
        case Messages.CommandType.session_VALUE:
          processSessionCommand(peerId, command.getOp().name(), requestKey,
                  command.getSessionMessage());
          break;
        case Messages.CommandType.ack_VALUE:
          processAckCommand(peerId, requestKey, command.getAckMessage());
          break;
        case Messages.CommandType.rcp_VALUE:
          processRpcCommand(peerId, command.getRcpMessage());
          break;
        case Messages.CommandType.conv_VALUE:
          processConvCommand(peerId, command.getOp().name(), requestKey,
                  command.getConvMessage());
          break;
        case Messages.CommandType.error_VALUE:
          processErrorCommand(peerId, requestKey, command.getErrorMessage());
          break;
        case Messages.CommandType.logs_VALUE:
          processLogsCommand(peerId, requestKey, command.getLogsMessage());
          break;
        case Messages.CommandType.unread_VALUE:
          processUnreadCommand(peerId, command.getUnreadMessage());
          break;
        case Messages.CommandType.blacklist_VALUE:
          processBlacklistCommand(peerId, command.getOp().name(), requestKey, command.getBlacklistMessage());
          break;
        case Messages.CommandType.patch_VALUE:
          if(command.getOp().equals(Messages.OpType.modify)) {
            // modify 为服务器端主动推送的 patch 消息
            processPatchCommand(peerId, true, requestKey, command.getPatchMessage());
          } else if (command.getOp().equals(Messages.OpType.modified)) {
            // modified 代表的是服务器端对于客户端请求的相应
            processPatchCommand(peerId, false, requestKey, command.getPatchMessage());
          }
          break;
        default:
          break;
      }
    }
  }

  public void onError(Exception exception) {
    connectionEstablished = false;
    reConnectionRTMServer();
    if (null != this.connectionListener) {
      this.connectionListener.onError(null, null);
    }
  }

  private void processLoggedinCommand(Integer requestKey) {
    ;
  }
  private void processDataCommand(Messages.DataCommand dataCommand) {
    List<String> messageIds = dataCommand.getIdsList();
    List<Messages.JsonObjectMessage> messages = dataCommand.getMsgList();
    for (int i = 0; i < messages.size() && i < messageIds.size(); i++) {
      if (null != messages.get(i)) {
        // TODO
        // AVNotificationManager.getInstance().processPushMessage(messages.get(i).getData(), messageIds.get(i));
      }
    }
    WindTalker windTalker = WindTalker.getInstance();
    CommandPacket packet = windTalker.assemblePushAckPacket(AVInstallation.getCurrentInstallation().getInstallationId(), messageIds);
    sendPacket(packet);
  }
  private void processLiveQueryData(Messages.DataCommand dataCommand) {
    List<String> messageIds = dataCommand.getIdsList();
    List<Messages.JsonObjectMessage> messages = dataCommand.getMsgList();

    ArrayList<String> dataList = new ArrayList<>();
    for (int i = 0; i < messages.size() && i < messageIds.size(); i++) {
      Messages.JsonObjectMessage message = messages.get(i);
      if (null != message) {
        dataList.add(message.getData());
      }
    };
    AVLiveQuery.processData(dataList);
  }

  private void processDirectCommand(String peerId, Messages.DirectCommand directCommand) {
    ;
  }
  private void processSessionCommand(String peerId, String op, Integer requestId,
                                     Messages.SessionCommand command) {
    if (null != this.connectionListener) {
      this.connectionListener.onSessionCommand(op, requestId, command);
    }
  }
  private void processAckCommand(String peerId, Integer requestKey, Messages.AckCommand command) {
    ;
  }
  private void processRpcCommand(String peerId, Messages.RcpCommand command) {
    ;
  }
  private void processConvCommand(String peerId, String operation, Integer requestKey,
                                  Messages.ConvCommand convCommand) {
    ;
  }
  private void processErrorCommand(String peerId, Integer requestKey,
                                   Messages.ErrorCommand errorCommand) {
    ;
  }
  private void processLogsCommand(String peerId, Integer requestKey,
                                  Messages.LogsCommand logsCommand) {
    ;
  }
  private void processUnreadCommand(String peerId, Messages.UnreadCommand unreadCommand) {
    ;
  }
  private void processBlacklistCommand(String peerId, String operation, Integer requestKey,
                                       Messages.BlacklistCommand blacklistCommand) {
    ;
  }
  private void processPatchCommand(String peerId, boolean isModify, Integer requestKey, Messages.PatchCommand patchCommand) {
    ;
  }
}
