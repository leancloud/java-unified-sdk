package cn.leancloud.im.command;

import cn.leancloud.im.Messages;

import java.util.ArrayList;
import java.util.List;

public class SessionAckPacket extends PeerBasedCommandPacket {

  List<String> ids;

  public SessionAckPacket() {
    this.setCmd("ack");
  }

  public void setMessageId(String id) {
    ids = new ArrayList<String>(1);
    ids.add(id);
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setAckMessage(getAckCommand());
    return builder;
  }

  protected Messages.AckCommand getAckCommand() {
    Messages.AckCommand.Builder builder = Messages.AckCommand.newBuilder();
    if (null != ids && ids.size() > 0) {
      builder.addAllIds(ids);
    }
    return builder.build();
  }
}
