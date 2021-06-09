package cn.leancloud.im.v2;

import java.util.Arrays;

public class LCIMBinaryMessage extends LCIMMessage {
  /**
   * default constructor
   */
  public LCIMBinaryMessage() {
    super();
  }

  /**
   * constructor
   * @param conversationId - conversation id
   * @param from           - message sender
   */
  public LCIMBinaryMessage(String conversationId, String from) {
    super(conversationId, from);
  }

  /**
   * constructor
   * @param conversationId - conversation id
   * @param from           - message sender
   * @param timestamp      - message send timestamp
   * @param deliveredAt    - message deliver timestamp
   */
  public LCIMBinaryMessage(String conversationId, String from, long timestamp, long deliveredAt) {
    super(conversationId, from, timestamp, deliveredAt);
  }

  /**
   * constructor
   * @param conversationId - conversation id
   * @param from           - message sender
   * @param timestamp      - message send timestamp
   * @param deliveredAt    - message deliver timestamp
   * @param readAt         - message read timestamp
   */
  public LCIMBinaryMessage(String conversationId, String from, long timestamp, long deliveredAt, long readAt) {
    super(conversationId, from, timestamp, deliveredAt, readAt);
  }

  /**
   * create binary instance by copying AVIMMessage instance(except content field).
   * @param other other instance
   * @return new binary message instance.
   */
  public static LCIMBinaryMessage createInstanceFromMessage(LCIMMessage other) {
    if (null == other) {
      return null;
    }
    LCIMBinaryMessage msg = new LCIMBinaryMessage();
    msg.conversationId = other.conversationId;
    msg.currentClient = other.currentClient;
    msg.from = other.from;
    msg.ioType = other.ioType;
    msg.mentionList = other.mentionList;
    msg.mentionAll = other.mentionAll;
    msg.messageId = other.messageId;
    msg.uniqueToken = other.uniqueToken;
    msg.status = other.status;

    msg.deliveredAt = other.deliveredAt;
    msg.readAt = other.readAt;
    msg.timestamp = other.timestamp;
    msg.updateAt = other.updateAt;
    return msg;
  }

  /**
   * set binary content.
   * @param val data bytes.
   */
  public void setBytes(byte[] val) {
    bytes = val;
  }

  /**
   * get binary content.
   * @return data bytes.
   */
  public byte[] getBytes() {
    return bytes;
  }

  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    if (null != this.bytes) {
      result = prime * result + this.bytes.hashCode();
    }
    return result;
  }

  public boolean equals(Object other) {
    if (!super.equals(other)) {
      return false;
    }
    return Arrays.equals(this.bytes, ((LCIMBinaryMessage)other).bytes);
  }
}
