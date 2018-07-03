package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.utils.StringUtil;

public class ConversationMessageQueryPacket extends PeerBasedCommandPacket {
  public ConversationMessageQueryPacket() {
    setCmd("logs");
  }

  String conversationId;
  int requestId;

  String msgId;
  long timestamp;

  String toMsgId;
  long toTimestamp;

  int limit;

  boolean sclosed;
  boolean tclosed;
  int direct;

  int msgType;

  public boolean isSclosed() {
    return sclosed;
  }

  public void setSclosed(boolean sclosed) {
    this.sclosed = sclosed;
  }

  public boolean isTclosed() {
    return tclosed;
  }

  public void setTclosed(boolean tclosed) {
    this.tclosed = tclosed;
  }

  public int getDirect() {
    return direct;
  }

  public void setDirect(int direct) {
    this.direct = direct;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getToMsgId() {
    return toMsgId;
  }

  public void setToMsgId(String toMsgId) {
    this.toMsgId = toMsgId;
  }

  public long getToTimestamp() {
    return toTimestamp;
  }

  public void setToTimestamp(long toTimestamp) {
    this.toTimestamp = toTimestamp;
  }

  @Override
  public int getRequestId() {
    return requestId;
  }

  @Override
  public void setRequestId(int requestId) {
    this.requestId = requestId;
  }

  public int getMsgType() {
    return msgType;
  }

  public void setMsgType(int msgType) {
    this.msgType = msgType;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setLogsMessage(getLogsCommand());
    builder.setI(requestId);
    return builder;
  }

  protected Messages.LogsCommand getLogsCommand() {
    Messages.LogsCommand.Builder builder = Messages.LogsCommand.newBuilder();
    builder.setCid(conversationId);
    builder.setLimit(limit);
    if (!StringUtil.isEmpty(msgId)) {
      builder.setMid(msgId);
    }

    if (timestamp > 0) {
      builder.setT(timestamp);
    }
    builder.setTIncluded(sclosed);

    if (!StringUtil.isEmpty(toMsgId)) {
      builder.setTmid(toMsgId);
    }

    if (toTimestamp > 0) {
      builder.setTt(toTimestamp);
    }
    builder.setTtIncluded(tclosed);

    if (direct == 0) {
      builder.setDirection(Messages.LogsCommand.QueryDirection.OLD);
    } else {
      builder.setDirection(Messages.LogsCommand.QueryDirection.NEW);
    }
    if (msgType != 0) {
      builder.setLctype(msgType);
    }
    return builder.build();
  }

  public static ConversationMessageQueryPacket getConversationMessageQueryPacket(String peerId, String conversationId,
                                                                                 String msgId, long timestamp, boolean sclosed,
                                                                                 String toMsgId, long toTimestamp, boolean tclosed,
                                                                                 int direct, int limit, int msgType, int requestId) {
    ConversationMessageQueryPacket cqp = new ConversationMessageQueryPacket();
    if (AVIMClient.getClientsCount() > 1) {
      // peerId is necessary only when more than 1 client logined.
      cqp.setPeerId(peerId);
    }

    cqp.setConversationId(conversationId);
    cqp.setMsgId(msgId);
    cqp.setLimit(limit);
    cqp.setDirect(direct);
    cqp.setTimestamp(timestamp);
    cqp.setSclosed(sclosed);
    cqp.setRequestId(requestId);

    cqp.setToMsgId(toMsgId);
    cqp.setToTimestamp(toTimestamp);
    cqp.setTclosed(tclosed);
    cqp.setMsgType(msgType);

    return cqp;
  }
}
