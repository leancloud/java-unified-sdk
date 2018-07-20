package cn.leancloud.im.v2;

import cn.leancloud.AVException;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.MessageBus;
import cn.leancloud.im.v2.callback.AVIMConversationCallback;
import cn.leancloud.im.v2.callback.AVIMMessageRecalledCallback;
import cn.leancloud.im.v2.callback.AVIMMessageUpdatedCallback;
import cn.leancloud.im.v2.messages.AVIMFileMessage;
import cn.leancloud.im.v2.messages.AVIMFileMessageAccessor;
import cn.leancloud.utils.StringUtil;

import java.util.*;

public class AVIMConversation {
  /**
   * 暂存消息
   * <p/>
   * 只有在消息发送时，对方也是在线的才能收到这条消息
   */
  public static final int TRANSIENT_MESSAGE_FLAG = 0x10;
  /**
   * 回执消息
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

  AVIMMessageStorage storage;

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

  protected AVIMConversation(AVIMClient client, List<String> members,
                             Map<String, Object> attributes, boolean isTransient) {
    this.members = new HashSet<String>();
    if (members != null) {
      this.members.addAll(members);
    }
    this.attributes = new HashMap<String, Object>();
    if (attributes != null) {
      this.attributes.putAll(attributes);
    }
    this.client = client;
    pendingAttributes = new HashMap<String, Object>();
    this.isTransient = isTransient;

    this.storage = client.getStorage();
  }
  protected AVIMConversation(AVIMClient client, String conversationId) {
    this(client, null, null, false);
    this.conversationId = conversationId;
  }
  public String getConversationId() {
    return this.conversationId;
  }

  protected void setConversationId(String id) {
    this.conversationId = id;
  }

  protected void setCreator(String creator) {
    this.creator = creator;
  }

  /**
   * 获取聊天对话的创建者
   *
   * @return
   * @since 3.0
   */
  public String getCreator() {
    return this.creator;
  }

  /**
   * 获取conversation当前的参与者
   *
   * @return
   * @since 3.0
   */
  public List<String> getMembers() {
    List<String> allList = new ArrayList<String>();
    allList.addAll(members);

    return Collections.unmodifiableList(allList);
  }

  protected void setMembers(List<String> m) {
    members.clear();
    if (m != null) {
      members.addAll(m);
    }
  }

  /**
   * get the latest readAt timestamp
   * @return
   */
  public long getLastReadAt() {
    return lastReadAt;
  }

  /**
   * get the latest deliveredAt timestamp
   * @return
   */
  public long getLastDeliveredAt() {
    if (lastReadAt > lastDeliveredAt) {
      // 既然已读，肯定已经送到了
      return lastReadAt;
    }
    return lastDeliveredAt;
  }

  void setLastReadAt(long timeStamp, boolean saveToLocal) {
    if (timeStamp > lastReadAt) {
      lastReadAt = timeStamp;
      if (saveToLocal) {
        storage.updateConversationTimes(this);
      }
    }
  }

  void setLastDeliveredAt(long timeStamp, boolean saveToLocal) {
    if (timeStamp > lastDeliveredAt) {
      lastDeliveredAt = timeStamp;
      if (saveToLocal) {
        storage.updateConversationTimes(this);
      }
    }
  }

  /**
   * Add a key-value pair to this conversation
   * @param key   Keys must be alphanumerical plus underscore, and start with a letter.
   * @param value Values may be numerical, String, JSONObject, JSONArray, JSONObject.NULL, or other
   *              AVObjects. value may not be null.
   */
  public void set(String key, Object value) {
    if (!StringUtil.isEmpty(key) && null != value) {
      pendingInstanceData.put(key, value);
    }
  }

  /**
   * Access a value
   * @param key
   * @return
   */
  public Object get(String key) {
    if (!StringUtil.isEmpty(key)) {
      if (pendingInstanceData.containsKey(key)) {
        return pendingInstanceData.get(key);
      }
      if (instanceData.containsKey(key)) {
        return instanceData.get(key);
      }
    }
    return null;
  }

  /**
   * 获取当前聊天对话的属性
   *
   * @return
   * @since 3.0
   */
  @Deprecated
  public Object getAttribute(String key) {
    Object value;
    if (pendingAttributes.containsKey(key)) {
      value = pendingAttributes.get(key);
    } else {
      value = attributes.get(key);
    }
    return value;
  }

  @Deprecated
  public void setAttribute(String key, Object value) {
    if (!StringUtil.isEmpty(key)) {
      // 以往的 sdk 支持 setAttribute("attr.key", "attrValue") 这种格式，这里兼容一下
      if (key.startsWith(ATTR_PERFIX)) {
        this.pendingAttributes.put(key.substring(ATTR_PERFIX.length()), value);
      } else {
        this.pendingAttributes.put(key, value);
      }
    }
  }

  /**
   * 设置当前聊天对话的属性
   *
   * @param attr
   * @since 3.0
   */
  @Deprecated
  public void setAttributes(Map<String, Object> attr) {
    pendingAttributes.clear();
    pendingAttributes.putAll(attr);
  }

  /**
   * 设置当前聊天对话的属性，仅用于初始化时
   * 因为 attr 涉及到本地缓存，所以初始化时与主动调用 setAttributes 行为不同
   * @param attr
   */
  void setAttributesForInit(Map<String, Object> attr) {
    this.attributes.clear();
    if (attr != null) {
      this.attributes.putAll(attr);
    }
  }

  /**
   * 获取conversation的名字
   *
   * @return
   */
  public String getName() {
    return (String) getAttribute(Conversation.NAME);
  }

  public void setName(String name) {
    pendingAttributes.put(Conversation.NAME, name);
  }

  /**
   * 获取最新一条消息的时间
   *
   * @return
   */
  public Date getLastMessageAt() {
    AVIMMessage lastMessage = getLastMessage();
    if (null != lastMessage) {
      setLastMessageAt(new Date(lastMessage.getReceiptTimestamp()));
    }
    return lastMessageAt;
  }

  void setLastMessageAt(Date messageTime) {
    if (null != messageTime && (null == lastMessageAt || messageTime.after(this.lastMessageAt))) {
      this.lastMessageAt = messageTime;
    }
  }

  /**
   * 获取最新一条消息的时间
   *
   * @return
   */
  public AVIMMessage getLastMessage() {
    if (AVIMOptions.getGlobalOptions().isMessageQueryCacheEnabled() && !isSyncLastMessage) {
      setLastMessage(getLastMessageFromLocal());
    }
    return lastMessage;
  }

  private AVIMMessage getLastMessageFromLocal() {
    if (AVIMOptions.getGlobalOptions().isMessageQueryCacheEnabled()) {
      AVIMMessage lastMessageInLocal = storage.getLatestMessage(getConversationId());
      isSyncLastMessage = true;
      return lastMessageInLocal;
    }
    return null;
  }

  /**
   * lastMessage 的来源：
   * 1. sqlite conversation 表中的 lastMessage，conversation query 后存储下来的
   * 2. sqlite message 表中存储的最新的消息
   * 3. 实时通讯推送过来的消息也要及时更新 lastMessage
   * @param lastMessage
   */
  void setLastMessage(AVIMMessage lastMessage) {
    if (null != lastMessage) {
      if (null == this.lastMessage) {
        this.lastMessage = lastMessage;
      } else {
        if(this.lastMessage.getTimestamp() <= lastMessage.getTimestamp()) {
          this.lastMessage = lastMessage;
        }
      }
    }
  }

  void increaseUnreadCount(int num, boolean mentioned) {
    unreadMessagesCount = getUnreadMessagesCount() + num;
    if (mentioned) {
      unreadMessagesMentioned = mentioned;
    }

  }

  void updateUnreadCountAndMessage(AVIMMessage lastMessage, int unreadCount, boolean mentioned) {
    if (null != lastMessage) {
      setLastMessage(lastMessage);
      storage.insertMessage(lastMessage, true);
    }

    if (unreadMessagesCount != unreadCount) {
      unreadMessagesCount = unreadCount;
      unreadMessagesMentioned = mentioned;
      storage.updateConversationUreadCount(conversationId, unreadMessagesCount, mentioned);
    }
  }

  /**
   * 获取当前未读消息数量
   * @return
   */
  public int getUnreadMessagesCount() {
    return unreadMessagesCount;
  }

  /**
   * 判断当前未读消息中是否有提及当前用户的消息存在。
   * @return
   */
  public boolean unreadMessagesMentioned() {
    return unreadMessagesMentioned;
  }

  /**
   * 发送一条非暂存消息
   *
   * @param message
   * @param callback
   * @since 3.0
   */
  public void sendMessage(AVIMMessage message, final AVIMConversationCallback callback) {
    sendMessage(message, null, callback);
  }

  /**
   * 发送一条消息。
   *
   * @param message
   * @param messageFlag 消息发送选项。
   * @param callback
   * @since 3.0
   * @deprecated Please use {@link #sendMessage(AVIMMessage, AVIMMessageOption, AVIMConversationCallback)}
   */
  public void sendMessage(AVIMMessage message, int messageFlag, AVIMConversationCallback callback) {
    AVIMMessageOption option = new AVIMMessageOption();
    option.setReceipt((messageFlag & AVIMConversation.RECEIPT_MESSAGE_FLAG) == AVIMConversation.RECEIPT_MESSAGE_FLAG);
    option.setTransient((messageFlag & AVIMConversation.TRANSIENT_MESSAGE_FLAG) == AVIMConversation.TRANSIENT_MESSAGE_FLAG);
    sendMessage(message, option, callback);
  }

  public void sendMessage(final AVIMMessage message, final AVIMMessageOption messageOption, final AVIMConversationCallback callback) {
    message.setConversationId(conversationId);
    message.setFrom(client.getClientId());
    message.generateUniqueToken();
    message.setTimestamp(System.currentTimeMillis());
    if (false) {
      // judge network status.
      message.setMessageStatus(AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed);
      if (callback != null) {
        callback.internalDone(new AVException(AVException.CONNECTION_FAILED, "Connection lost"));
      }
      return;
    }

    message.setMessageStatus(AVIMMessage.AVIMMessageStatus.AVIMMessageStatusSending);
    if (AVIMFileMessage.class.isAssignableFrom(message.getClass())) {
      AVIMFileMessageAccessor.upload((AVIMFileMessage) message, new SaveCallback() {
        public void done(AVException e) {
          if (e != null) {
            message.setMessageStatus(AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed);
            if (callback != null) {
              callback.internalDone(e);
            }
          } else {
            MessageBus.getInstance().sendMessage(message, messageOption, callback);
          }
        }
      });
    } else {
      MessageBus.getInstance().sendMessage(message, messageOption, callback);
    }
  }

  /**
   * update message content
   * @param oldMessage the message need to be modified
   * @param newMessage the content of the old message will be covered by the new message's
   * @param callback
   */
  public void updateMessage(AVIMMessage oldMessage, AVIMMessage newMessage, AVIMMessageUpdatedCallback callback) {
    MessageBus.getInstance().updateMessage(oldMessage, newMessage, callback);
  }

  /**
   * racall message
   * @param message the message need to be recalled
   * @param callback
   */
  public void recallMessage(AVIMMessage message, AVIMMessageRecalledCallback callback) {
    MessageBus.getInstance().recallMessage(message, callback);
  }

  /**
   * save local message which failed to send to LeanCloud server.
   * Notice: this operation perhaps to block the main thread because that database operation is executing.
   *
   * @param message the message need to be saved to local.
   */
  public void addToLocalCache(AVIMMessage message) {
    this.storage.insertLocalMessage(message);
  }

  /**
   * remove local message from cache.
   * Notice: this operation perhaps to block the main thread because that database operation is executing.
   *
   * @param message
   */
  public void removeFromLocalCache(AVIMMessage message) {
    this.storage.removeLocalMessage(message);
  }


}
