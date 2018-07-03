package cn.leancloud.command;

import cn.leancloud.Messages;

public class PeerBasedCommandPacket extends CommandPacket {

  private String peerId;

  public String getPeerId() {
    return peerId;
  }

  public void setPeerId(String peerId) {
    this.peerId = peerId;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    if (null != getPeerId()) {
      builder.setPeerId(getPeerId());
    }
    return builder;
  }

}
