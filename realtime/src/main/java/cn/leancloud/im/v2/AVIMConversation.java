package cn.leancloud.im.v2;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AVIMConversation {
  /**
   * 暂存消息
   * <p/>
   * 只有在消息发送时，对方也是在线的才能收到这条消息
   */
  public static final int TRANSIENT_MESSAGE_FLAG = 0x10;
  /**
   `   * 回执消息
   * <p/>
   * 当消息送到到对方以后，发送方会收到消息回执说明消息已经成功达到接收方
   */
  public static final int RECEIPT_MESSAGE_FLAG = 0x100;

  private static final String ATTR_PERFIX = Conversation.ATTRIBUTE + ".";

  String conversationId;
  Set<String> members;
  Map<String, Object> attributes;
  Map<String, Object> pendingAttributes;
  AVIMClient client;
  String creator;
  boolean isTransient;

  Date lastMessageAt;
  AVIMMessage lastMessage;

  String createdAt;
  String updatedAt;

  Map<String, Object> instanceData = new HashMap<>();
  Map<String, Object> pendingInstanceData = new HashMap<>();

  // 是否与数据库中同步了 lastMessage，避免多次走 sqlite 查询
  private boolean isSyncLastMessage = false;

  /**
   * 未读消息数量
   */
  int unreadMessagesCount = 0;
  boolean unreadMessagesMentioned = false;

  /**
   * 对方最后收到消息的时间，此处仅针对双人会话有效
   */
  long lastDeliveredAt;

  /**
   * 对方最后读消息的时间，此处仅针对双人会话有效
   */
  long lastReadAt;

  /**
   * 是否是服务号
   */
  boolean isSystem = false;

  /**
   * 是否是服务号
   */
  public boolean isSystem() {
    return isSystem;
  }

  /**
   * 是否是临时对话
   */
  boolean isTemporary = false;

  /**
   * 是否是临时对话
   */
  public boolean isTemporary() {
    return isTemporary;
  }

  void setTemporary(boolean temporary) {
    isTemporary = temporary;
  }

  /**
   * 临时对话过期时间
   */
  long temporaryExpiredat = 0l;

  /**
   * 获取临时对话过期时间（以秒为单位）
   */
  public long getTemporaryExpiredat() {
    return temporaryExpiredat;
  }

  /**
   * 设置临时对话过期时间（以秒为单位）
   * 仅对 临时对话 有效
   */
  public void setTemporaryExpiredat(long temporaryExpiredat) {
    if (this.isTemporary()) {
      this.temporaryExpiredat = temporaryExpiredat;
    }
  }
  public boolean isTransient() {
    return isTransient;
  }

  void setTransientForInit(boolean isTransient) {
    this.isTransient = isTransient;
  }

  protected int getType() {
    if (isSystem()) {
      return Conversation.CONV_TYPE_SYSTEM;
    } else if (isTransient()) {
      return Conversation.CONV_TYPE_TRANSIENT;
    } else if (isTemporary()) {
      return Conversation.CONV_TYPE_TEMPORARY;
    } else {
      return Conversation.CONV_TYPE_NORMAL;
    }
  }


}
