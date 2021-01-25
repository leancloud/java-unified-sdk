package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.im.Signature;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.utils.StringUtil;

import java.util.List;

public class BlacklistCommandPacket extends PeerBasedCommandPacket {
  public static final String BLACKLIST_CMD = "blacklist";
  public static class BlacklistCommandOp {
    /**
     * 客户端发出来的 op
     */
    public static final String BLOCK = "block";
    public static final String UNBLOCK = "unblock";
    public static final String QUERY = "query";
    /**
     * 服务端响应的 op
     */
    public static final String BLOCKED = "blocked";
    public static final String UNBLOCKED = "unblocked";
    public static final String MEMBER_BLOCKED = "members_blocked";
    public static final String MEMBER_UNBLOCKED = "members_unblocked";
    public static final String QUERY_RESULT = "query_result";
  }
  private String op;
  private String conversationId;
  private List<String> clientIds;
  private String signature;
  private long timestamp;
  private String nonce;
  private int offset = 0;
  private int limit = 0;

  public BlacklistCommandPacket() {
    setCmd(BLACKLIST_CMD);
  }

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public List<String> getClientIds() {
    return clientIds;
  }

  public void setClientIds(List<String> clientIds) {
    this.clientIds = clientIds;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setOp(Messages.OpType.valueOf(op));
    builder.setBlacklistMessage(getBlacklistCommand());
    return builder;
  }

  protected Messages.BlacklistCommand getBlacklistCommand() {
    Messages.BlacklistCommand.Builder builder = Messages.BlacklistCommand.newBuilder();
    builder.setSrcCid(getConversationId());
    if (null != clientIds && clientIds.size() > 0) {
      builder.addAllToPids(clientIds);
    }

    if (offset > 0) {
      builder.setNext(Integer.toString(offset));
    }
    if (limit > 0) {
      builder.setLimit(limit);
    }
    if (!StringUtil.isEmpty(this.signature)) {
      builder.setS(getSignature());
      builder.setT(getTimestamp());
      builder.setN(getNonce());
    }

    return builder.build();
  }

  public static BlacklistCommandPacket genBlacklistCommandPacket(String selfId, String conversationId,
                                                                 String op, List<String> members,
                                                                 Signature signature, int requestId) {
    BlacklistCommandPacket packet = new BlacklistCommandPacket();
    if (AVIMClient.getClientsCount() > 1) {
      packet.setPeerId(selfId);
    }
    packet.setConversationId(conversationId);
    packet.setOp(op);
    packet.setClientIds(members);
    packet.setRequestId(requestId);
    if (null != signature) {
      packet.setSignature(signature.getSignature());
      packet.setNonce(signature.getNonce());
      packet.setTimestamp(signature.getTimestamp());
    }
    return packet;
  }

  public static BlacklistCommandPacket genBlacklistCommandPacket(String selfId, String conversationId, String op,
                                                                 int offset, int limit, int requestId) {
    BlacklistCommandPacket packet = new BlacklistCommandPacket();
    if (AVIMClient.getClientsCount() > 1) {
      packet.setPeerId(selfId);
    }
    packet.setConversationId(conversationId);
    packet.setOp(op);
    packet.setOffset(offset);
    packet.setLimit(limit);
    packet.setRequestId(requestId);
    return packet;
  }
}
