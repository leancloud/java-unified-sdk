package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.utils.StringUtil;

import java.util.Collection;

public class DirectMessagePacket extends PeerBasedCommandPacket {
  private String msg;

  private Collection<String> toPeerIds;

  private String roomId;

  private boolean transi;

  private boolean receipt;

  public DirectMessagePacket() {
    this.setCmd("direct");
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public Collection<String> getToPeerIds() {
    return toPeerIds;
  }

  public void setToPeerIds(Collection<String> toPeerIds) {
    this.toPeerIds = toPeerIds;
  }

  public boolean isTransient() {
    return transi;
  }

  public void setTransient(boolean transi) {
    this.transi = transi;
  }

  public String getGroupId() {
    return roomId;
  }

  public void setGroupId(String roomId) {
    this.roomId = roomId;
  }

  public boolean isReceipt() {
    return receipt;
  }

  public void setReceipt(boolean receipt) {
    this.receipt = receipt;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setDirectMessage(getDirectCommand());
    return builder;
  }

  protected Messages.DirectCommand getDirectCommand() {
    Messages.DirectCommand.Builder builder = Messages.DirectCommand.newBuilder();
    builder.setMsg(getMsg());

    if (getToPeerIds() != null && !getToPeerIds().isEmpty()) {
      builder.addAllToPeerIds(getToPeerIds());
    }

    if (receipt) {
      builder.setR(true);
    }

    if (!StringUtil.isEmpty(this.getGroupId())) {
      builder.setRoomId(getGroupId());
    }

    if (isTransient()) {
      builder.setTransient(isTransient());
    }
    return builder.build();
  }
}
