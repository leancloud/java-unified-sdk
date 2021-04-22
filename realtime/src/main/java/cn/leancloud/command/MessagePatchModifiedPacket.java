package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.im.v2.LCIMClient;

public class MessagePatchModifiedPacket extends PeerBasedCommandPacket {

  private long lastPatchTime;

  public MessagePatchModifiedPacket() {
    this.setCmd("patch");
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setOp(Messages.OpType.modified);
    builder.setPatchMessage(getPatchCommand());
    return builder;
  }

  protected Messages.PatchCommand getPatchCommand() {
    Messages.PatchCommand.Builder builder = Messages.PatchCommand.newBuilder();
    builder.setLastPatchTime(lastPatchTime);
    return builder.build();
  }

  public static MessagePatchModifiedPacket getPatchMessagePacket(String peerId, long lastPatchTime) {
    MessagePatchModifiedPacket messagePatchModifiedPacket = new MessagePatchModifiedPacket();
    if (LCIMClient.getClientsCount() > 1) {
      // peerId is necessary only when more than 1 client logined.
      messagePatchModifiedPacket.setPeerId(peerId);
    }
    messagePatchModifiedPacket.lastPatchTime = lastPatchTime;
    return messagePatchModifiedPacket;
  }

}
