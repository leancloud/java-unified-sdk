package cn.leancloud.push.lite.proto;

import java.util.List;

public class PushAckPacket extends CommandPacket {
  List<String> ids;

  public PushAckPacket() {
    this.setCmd("ack");
  }

  public void setMessageIds(List<String> ids) {
    this.ids = ids;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setAckMessage(getAckCommand());
    return builder;
  }

  protected Messages.AckCommand getAckCommand() {
    Messages.AckCommand.Builder builder = Messages.AckCommand.newBuilder();
    builder.addAllIds(ids);
    return builder.build();
  }
}
