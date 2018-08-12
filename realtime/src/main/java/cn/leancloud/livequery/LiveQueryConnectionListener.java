package cn.leancloud.livequery;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.command.LiveQueryLoginPacket;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.session.AVConnectionListener;
import cn.leancloud.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

class LiveQueryConnectionListener implements AVConnectionListener {
  private static final AVLogger LOGGER = LogUtil.getLogger(LiveQueryConnectionListener.class);

  public void onWebSocketOpen() {
    LOGGER.d("connection opened, ready to send packet");
  }

  public void onWebSocketClose() {
    LOGGER.d("connection closed.");
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
  }

  private void processLoggedinCommand(Integer requestKey) {
    if (null != requestKey) {
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
    AVLiveQuery.processData(dataList);
  }

  private void processErrorCommand(String peerId, Integer requestKey,
                                   Messages.ErrorCommand errorCommand) {
    if (null != requestKey && requestKey != CommandPacket.UNSUPPORTED_OPERATION) {
      int code = errorCommand.getCode();
      int appCode = (errorCommand.hasAppCode() ? errorCommand.getAppCode() : 0);
      String reason = errorCommand.getReason();
      Conversation.AVIMOperation operation = null;
      InternalConfiguration.getOperationTube().onOperationCompleted(peerId, null, requestKey,
              operation, new AVIMException(code, appCode, reason));
    }
  }
}
