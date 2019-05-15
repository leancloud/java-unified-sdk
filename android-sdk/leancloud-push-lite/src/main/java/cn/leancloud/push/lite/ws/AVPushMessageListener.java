package cn.leancloud.push.lite.ws;

import java.util.List;

import cn.leancloud.push.lite.AVInstallation;
import cn.leancloud.push.lite.AVNotificationManager;
import cn.leancloud.push.lite.proto.CommandPacket;
import cn.leancloud.push.lite.proto.Messages;
import cn.leancloud.push.lite.proto.PushAckPacket;
import cn.leancloud.push.lite.utils.PacketAssembler;

public class AVPushMessageListener implements AVConnectionListener {
  public static final String DEFAULT_ID = "leancloud_push_default_id";

  private static final AVPushMessageListener instance = new AVPushMessageListener();
  public static AVPushMessageListener getInstance() {
    return instance;
  }

  private AVPushMessageListener() {
    ;
  }

  public void onWebSocketOpen() {}

  public void onWebSocketClose() {}

  public void onMessageArriving(String peerId, Integer requestKey, Messages.GenericCommand genericCommand) {
    if (null == genericCommand || null == genericCommand.getDataMessage()) {
      return;
    }
    switch (genericCommand.getCmd().getNumber()) {
      case Messages.CommandType.data_VALUE:
        Messages.DataCommand dataCommand = genericCommand.getDataMessage();
        List<String> messageIds = dataCommand.getIdsList();
        List<Messages.JsonObjectMessage> messages = dataCommand.getMsgList();
        for (int i = 0; i < messages.size() && i < messageIds.size(); i++) {
          if (null != messages.get(i)) {
            AVNotificationManager.getInstance().processPushMessage(messages.get(i).getData(), messageIds.get(i));
          }
        }

        CommandPacket packet = PacketAssembler.getInstance().assemblePushAckPacket(AVInstallation.getCurrentInstallation().getInstallationId(), messageIds);
        AVConnectionManager.getInstance().sendPacket(packet);
        break;
      case Messages.CommandType.goaway_VALUE:
        processGoawayCommand();
        break;
    }
  }

  public void onError(Integer requestKey, Messages.ErrorCommand errorCommand) {

  }


  private void processDataCommand(Messages.DataCommand dataCommand) {
    List<String> messageIds = dataCommand.getIdsList();
    List<Messages.JsonObjectMessage> messages = dataCommand.getMsgList();
    for (int i = 0; i < messages.size() && i < messageIds.size(); i++) {
      if (null != messages.get(i)) {
        AVNotificationManager.getInstance().processPushMessage(messages.get(i).getData(), messageIds.get(i));
      }
    }
    PushAckPacket pap = new PushAckPacket();
    pap.setInstallationId(AVInstallation.getCurrentInstallation().getInstallationId());
    pap.setMessageIds(messageIds);
    AVConnectionManager.getInstance().sendPacket(pap);
  }

  private void processGoawayCommand() {
    AVConnectionManager.getInstance().cleanup();
    AVConnectionManager.getInstance().startConnection();
  }
}
