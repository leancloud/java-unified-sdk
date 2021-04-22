package cn.leancloud.push;

import cn.leancloud.LCInstallation;
import cn.leancloud.Messages;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.im.WindTalker;
import cn.leancloud.session.LCConnectionListener;
import cn.leancloud.session.LCConnectionManager;

import java.util.List;

public class LCPushMessageListener implements LCConnectionListener {
  public static final String DEFAULT_ID = "leancloud_push_default_id";

  private static final LCPushMessageListener instance = new LCPushMessageListener();
  public static LCPushMessageListener getInstance() {
    return instance;
  }

  private LCNotificationManager notificationManager = new DummyNotificationManager();

  private LCPushMessageListener() {
    ;
  }

  public void setNotificationManager(LCNotificationManager manager) {
    this.notificationManager = manager;
  }

  public LCNotificationManager getNotificationManager() {
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
    CommandPacket packet = windTalker.assemblePushAckPacket(LCInstallation.getCurrentInstallation().getInstallationId(), messageIds);
    LCConnectionManager.getInstance().sendPacket(packet);
  }

  public void onError(Integer requestKey, Messages.ErrorCommand errorCommand) {}
}
