package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.utils.StringUtil;

public class UnreadMessagesClearPacket extends PeerBasedCommandPacket {
  String conversationId;
  String messageId;
  long messageTS;

  public UnreadMessagesClearPacket() {
    this.setCmd("read");
  }

  String getConversationId() {
    return conversationId;
  }

  void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  void setMessageTS(long timestamp) {
    this.messageTS = timestamp;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setReadMessage(getReadCommand());
    return builder;
  }

  protected Messages.ReadCommand getReadCommand() {
    Messages.ReadCommand.Builder builder = Messages.ReadCommand.newBuilder();
    Messages.ReadTuple.Builder readTupleBuilder = builder.addConvsBuilder();
    if (!StringUtil.isEmpty(messageId)) {
      readTupleBuilder.setMid(messageId);
    }
    if (messageTS > 0) {
      readTupleBuilder.setTimestamp(messageTS);
    }
    readTupleBuilder.setCid(conversationId);
    return builder.build();
  }

  public static UnreadMessagesClearPacket getUnreadClearPacket(String peerId,
                                                               String conversationId, String messageId, long timeStamp, int requestId) {
    UnreadMessagesClearPacket packet = new UnreadMessagesClearPacket();
    if (LCIMClient.getClientsCount() > 1) {
      // peerId is necessary only for more than 1 clients loggined.
      packet.setPeerId(peerId);
    }
    packet.setConversationId(conversationId);
    packet.setRequestId(requestId);
    packet.setMessageId(messageId);
    packet.setMessageTS(timeStamp);
    return packet;
  }
}
