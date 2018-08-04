package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.utils.StringUtil;
import com.google.protobuf.ByteString;

import java.util.List;

public class MessagePatchModifyPacket extends PeerBasedCommandPacket {
  public MessagePatchModifyPacket() {
    setCmd("patch");
  }

  private String conversationId;
  private String messageId;
  private long timestamp;
  private String messageData;
  private boolean isRecall;
  private boolean mentionAll;
  private List<String> mentionList;
  private ByteString binaryData = null;

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setOp(Messages.OpType.modify);
    builder.setPatchMessage(getPatchCommand());
    return builder;
  }

  private Messages.PatchCommand getPatchCommand() {
    Messages.PatchCommand.Builder builder = Messages.PatchCommand.newBuilder();
    Messages.PatchItem.Builder patchItemBuilder = Messages.PatchItem.newBuilder();
    if (timestamp > 0) {
      patchItemBuilder.setTimestamp(timestamp);
    }
    if (!StringUtil.isEmpty(messageId)) {
      patchItemBuilder.setMid(messageId);
    }
    if (!StringUtil.isEmpty(conversationId)) {
      patchItemBuilder.setCid(conversationId);
    }
    if (!StringUtil.isEmpty(messageData)) {
      patchItemBuilder.setData(messageData);
    }
    patchItemBuilder.setMentionAll(mentionAll);
    if (null != mentionList) {
      patchItemBuilder.addAllMentionPids(mentionList);
    }
    patchItemBuilder.setRecall(isRecall);
    if (null != binaryData) {
      patchItemBuilder.setDataBytes(binaryData);
    }
    builder.addPatches(patchItemBuilder.build());

    return builder.build();
  }

  public static MessagePatchModifyPacket getMessagePatchPacketForUpdate(String peerId, String conversationId,
                                                                        String messageId, String data, byte[] binaryData,
                                                                        boolean mentionAll, List<String> mentionList,
                                                                        long timestamp, int requestId) {
    MessagePatchModifyPacket packet = new MessagePatchModifyPacket();
    packet.conversationId = conversationId;
    packet.messageId = messageId;
    packet.timestamp = timestamp;
    packet.messageData = data;
    if (null != binaryData) {
      packet.binaryData = ByteString.copyFrom(binaryData);
    }
    packet.isRecall = false;
    packet.mentionAll = mentionAll;
    packet.mentionList = mentionList;
    packet.setRequestId(requestId);
    if (AVIMClient.getClientsCount() > 1) {
      // peerId is necessary only when more than 1 client logined.
      packet.setPeerId(peerId);
    }
    return packet;
  }

  public static MessagePatchModifyPacket getMessagePatchPacketForRecall(String peerId, String conversationId, String messageId,
                                                                        long timestamp, int requestId) {
    MessagePatchModifyPacket packet = new MessagePatchModifyPacket();
    packet.conversationId = conversationId;
    packet.messageId = messageId;
    packet.timestamp = timestamp;
    packet.isRecall = true;
    packet.setRequestId(requestId);
    if (AVIMClient.getClientsCount() > 1) {
      // peerId is necessary only when more than 1 client logined.
      packet.setPeerId(peerId);
    }
    return packet;
  }
}
