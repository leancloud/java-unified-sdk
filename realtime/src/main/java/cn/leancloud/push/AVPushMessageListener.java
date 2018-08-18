package cn.leancloud.push;

import cn.leancloud.AVInstallation;
import cn.leancloud.Messages;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.im.WindTalker;
import cn.leancloud.session.AVConnectionListener;
import cn.leancloud.session.AVConnectionManager;

import java.util.List;

public class AVPushMessageListener implements AVConnectionListener {
  public static final String DEFAULT_ID = "leancloud_push_default_id";

  private static final AVPushMessageListener instance = new AVPushMessageListener();
  public static AVPushMessageListener getInstance() {
    return instance;
  }

  private AVNotificationManager notificationManager = new DummyNotificationManager();

  private AVPushMessageListener() {
    ;
  }

  public void setNotificationManager(AVNotificationManager manager) {
    this.notificationManager = manager;
  }

  public AVNotificationManager getNotificationManager() {
    return notificationManager;
  }

  public void onWebSocketOpen() {}

  public void onWebSocketClose() {}

  public void onMessageArriving(String peerId, Integer requestKey, Messages.GenericCommand genericCommand) {
    if (null == genericCommand || null == genericCommand.getDataMessage()) {
      return;
    }
    Messages.DataCommand dataCommand = genericCommand.getDataMessage();
    List<String> messageIds = dataCommand.getIdsList();
    List<Messages.JsonObjectMessage> messages = dataCommand.getMsgList();
    for (int i = 0; i < messages.size() && i < messageIds.size(); i++) {
      if (null != messages.get(i)) {
        this.notificationManager.processPushMessage(messages.get(i).getData(), messageIds.get(i));
      }
    }

    WindTalker windTalker = WindTalker.getInstance();
    CommandPacket packet = windTalker.assemblePushAckPacket(AVInstallation.getCurrentInstallation().getInstallationId(), messageIds);
    AVConnectionManager.getInstance().sendPacket(packet);
  }

  public void onError(Integer requestKey, Messages.ErrorCommand errorCommand) {}
}
