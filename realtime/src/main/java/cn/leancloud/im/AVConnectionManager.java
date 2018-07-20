package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.command.LiveQueryLoginPacket;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.livequery.AVLiveQuery;
import cn.leancloud.push.AVInstallation;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.websocket.AVStandardWebSocketClient;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AVConnectionManager implements AVStandardWebSocketClient.WebSocketClientMonitor {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVConnectionManager.class);

  private static AVConnectionManager instance = null;
  private AVStandardWebSocketClient webSocketClient = null;
  private AVConnectionListener connectionListener = null;

  public synchronized static AVConnectionManager getInstance() {
    if (instance == null) {
      instance = new AVConnectionManager();
    }
    return instance;
  }

  private AVConnectionManager() {
    ;
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

  /**
   * WebSocketClientMonitor interfaces
   */
  public void onOpen() {
    if (null != this.connectionListener) {
      this.connectionListener.onWebSocketOpen();
    }
  }

  public void onClose(int var1, String var2, boolean var3) {
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

    String peerId = command.getPeerId();
    Integer requestKey = command.hasI() ? command.getI() : null;
    if (StringUtil.isEmpty(peerId)) {
      peerId = AVIMClient.getDefaultClient();
    }
    if (command.getCmd().getNumber() == Messages.CommandType.loggedin_VALUE) {
      ;
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
    if (null != this.connectionListener) {
      this.connectionListener.onError(null, null);
    }
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
    ;
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
