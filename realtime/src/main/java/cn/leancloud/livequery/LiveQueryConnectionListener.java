package cn.leancloud.livequery;

import cn.leancloud.LCLogger;
import cn.leancloud.Messages;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.command.LiveQueryLoginPacket;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.session.LCConnectionListener;
import cn.leancloud.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

class LiveQueryConnectionListener implements LCConnectionListener {
  private static final LCLogger LOGGER = LogUtil.getLogger(LiveQueryConnectionListener.class);

  private volatile boolean connectionIsOpen = false;
  private LCLiveQueryConnectionHandler connectionHandler = null;

  public void onWebSocketOpen() {
    LOGGER.d("livequery connection opened, ready to send packet");
    if (null != connectionHandler) {
      connectionHandler.onConnectionOpen();
    }
  }

  public void onWebSocketClose() {
    LOGGER.d("livequery connection closed.");
    connectionIsOpen = false;
    if (null != connectionHandler) {
      connectionHandler.onConnectionClose();
    }
  }

  public boolean connectionIsOpen() {
    return connectionIsOpen;
  }

  public void setConnectionHandler(LCLiveQueryConnectionHandler handler) {
    this.connectionHandler = handler;
  }

  @Override
  public void onMessageArriving(String peerId, Integer requestKey, Messages.GenericCommand genericCommand) {
    if (null == genericCommand || !genericCommand.hasService()) {
      LOGGER.w("GenericCommand is null or hasn't service field.");
      return;
    }
    int service = genericCommand.getService();
    if (LiveQueryLoginPacket.SERVICE_LIVE_QUERY != service) {
      LOGGER.w("service field is invalid. expected=" + LiveQueryLoginPacket.SERVICE_LIVE_QUERY + ", result=" + service);
      return;
    }
    int commandCode = genericCommand.getCmd().getNumber();
    LOGGER.d("new message arriving. peerId=" + peerId + ", requestKey=" + requestKey + ", commandCode=" + commandCode);
    if (commandCode == Messages.CommandType.loggedin_VALUE) {
      processLoggedinCommand(requestKey);
    } else if (commandCode == Messages.CommandType.data_VALUE) {
      processLiveQueryData(genericCommand.getDataMessage());
    } else if (commandCode == Messages.CommandType.error_VALUE) {
      processErrorCommand(peerId, requestKey, genericCommand.getErrorMessage());
    } else {
      LOGGER.w("command isn't recognized.");
    }
  }

  @Override
  public void onError(Integer requestKey, Messages.ErrorCommand errorCommand) {
    LOGGER.e("encounter error.");
    connectionIsOpen = false;
    if (null != connectionHandler) {
      if (null == errorCommand) {
        connectionHandler.onConnectionError(-1, "");
      } else {
        int code = errorCommand.hasCode() ? errorCommand.getCode() : -1;
        String reason = errorCommand.hasReason() ? errorCommand.getReason() : "";
        connectionHandler.onConnectionError(code, reason);
      }
    }
  }

  private void processLoggedinCommand(Integer requestKey) {
    if (null == requestKey) {
      LOGGER.d("request key is null, ignore.");
    } else {
      connectionIsOpen = true;
      LiveQueryOperationDelegate.getInstance().ackOperationReplied(requestKey);
      InternalConfiguration.getOperationTube().onLiveQueryCompleted(requestKey, null);
    }
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
    LCLiveQuery.processData(dataList);
  }

  private void processErrorCommand(String peerId, Integer requestKey,
                                   Messages.ErrorCommand errorCommand) {
    if (null != requestKey && requestKey != CommandPacket.UNSUPPORTED_OPERATION) {
      int code = errorCommand.getCode();
      int appCode = (errorCommand.hasAppCode() ? errorCommand.getAppCode() : 0);
      String reason = errorCommand.getReason();
      Conversation.LCIMOperation operation = null;
      InternalConfiguration.getOperationTube().onOperationCompleted(peerId, null, requestKey,
              operation, new LCIMException(code, appCode, reason));
    }
  }
}
