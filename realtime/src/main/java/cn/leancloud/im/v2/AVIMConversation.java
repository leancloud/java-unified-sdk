package cn.leancloud.im.v2;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.AVObject;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.im.v2.conversation.AVIMConversationMemberInfo;
import cn.leancloud.im.v2.conversation.ConversationMemberRole;
import cn.leancloud.im.v2.messages.AVIMFileMessage;
import cn.leancloud.im.v2.messages.AVIMFileMessageAccessor;
import cn.leancloud.im.v2.messages.AVIMRecalledMessage;
import cn.leancloud.ops.Utils;
import cn.leancloud.query.QueryConditions;
import cn.leancloud.query.QueryOperation;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.*;

public class AVIMConversation {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVIMConversation.class);
  /**
   * 暂存消息
   *
   * 只有在消息发送时，对方也是在线的才能收到这条消息
   */
  public static final int TRANSIENT_MESSAGE_FLAG = 0x10;
  /**
   * 回执消息
   *
   * 当消息送到到对方以后，发送方会收到消息回执说明消息已经成功达到接收方
   */
  public static final int RECEIPT_MESSAGE_FLAG = 0x100;

  private static final String ATTR_PERFIX = Conversation.ATTRIBUTE + ".";

  String conversationId;
  Set<String> members; // 成员
  Map<String, Object> attributes; // 用户自定义属性
  Map<String, Object> pendingAttributes; // 修改中的属性
  AVIMClient client;  // AVIMClient 引用
  String creator;     // 创建者
  boolean isTransient; // 是否为临时对话

  AVIMMessageStorage storage;

  // 注意，sqlite conversation 表中的 lastMessageAt、lastMessage 的来源是 AVIMConversationQuery
  // 所以并不一定是最新的，返回时要与 message 表中的数据比较，然后返回最新的，
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
   * @return flag of service conversation.
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
   * @return flag of temporary conversation.
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
   * @return expired interval.
   */
  public long getTemporaryExpiredat() {
    return temporaryExpiredat;
  }

  /**
   * 设置临时对话过期时间（以秒为单位）
   * 仅对 临时对话 有效
   * @param temporaryExpiredat expiration value.
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

  /**
   * 获取Conversation的创建时间
   *
   * @return conversation created date.
   */
  public Date getCreatedAt() {
    return StringUtil.dateFromString(createdAt);
  }

  void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * 获取Conversation的更新时间
   *
   * @return conversation updated date.
   */
  public Date getUpdatedAt() {
    return StringUtil.dateFromString(updatedAt);
  }

  void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   * 超时的时间间隔设置为一个小时，即 fetch 操作并且返回了错误，则一个小时内 sdk 不再进行调用 fetch
   */
  int FETCH_TIME_INTERVEL = 3600 * 1000;

  /**
   * 最近的 sdk 调用的 fetch 操作的时间
   */
  long latestConversationFetch = 0;

  /**
   * 判断当前 Conversation 是否有效，因为 AVIMConversation 为客户端创建，有可能因为没有同步造成数据丢失
   * 可以根据此函数来判断，如果无效，则需要调用 fetchInfoInBackground 同步数据
   * 如果 fetchInfoInBackground 出错（比如因为 acl 问题造成 Forbidden to find by class permissions ），
   * 客户端就会在收到消息后一直做 fetch 操作，所以这里加了一个判断，如果在 FETCH_TIME_INTERVEL 内有业务类型的
   * error code 返回，则不在请求
   * @return flag indicating should need fetch or not.
   */
  public boolean isShouldFetch() {
    return null == getCreatedAt() || (System.currentTimeMillis() - latestConversationFetch > FETCH_TIME_INTERVEL);
  }

  public void setMustFetch() {
    latestConversationFetch = 0;
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

  /**
   * get conversation id
   *
   * @return conversation id.
   */
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
   * @return owner client id.
   * @since 3.0
   */
  public String getCreator() {
    return this.creator;
  }

  /**
   * 获取conversation当前的参与者
   *
   * @return member list.
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
   * @return latest readat timestamp
   */
  public long getLastReadAt() {
    return lastReadAt;
  }

  /**
   * get the latest deliveredAt timestamp
   * @return latest deliveredAt timestamp
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
   * @param key attribute key
   * @return attribute value.
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
   * @param key attribute key
   * @return attribute value.
   * @since 3.0
   */
  public Object getAttribute(String key) {
    Object value;
    if (pendingAttributes.containsKey(key)) {
      value = pendingAttributes.get(key);
    } else {
      value = attributes.get(key);
    }
    return value;
  }

  /**
   * Return all attributes.
   * Notice: you should only read it, it doesnt work for any modify operation.
   *
   * @return attributes map.
   */
  public Map<String, Object> getAttributes() {
    return this.attributes;
  }

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
   * @param attr attribute map
   * @since 3.0
   */
  public void setAttributes(Map<String, Object> attr) {
    pendingAttributes.clear();
    pendingAttributes.putAll(attr);
  }

  /**
   * 设置当前聊天对话的属性，仅用于初始化时
   * 因为 attr 涉及到本地缓存，所以初始化时与主动调用 setAttributes 行为不同
   * @param attr attribute map
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
   * @return conversation name.
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
   * @return lastest message receipted timestamp
   */
  public Date getLastMessageAt() {
    AVIMMessage lastMessage = getLastMessage();
    if (null != lastMessage) {
      setLastMessageAt(new Date(lastMessage.getDeliveredAt()));
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
   * @return latest message.
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
   * @param lastMessage lastest message.
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
   * @return unread message count.
   */
  public int getUnreadMessagesCount() {
    return unreadMessagesCount;
  }

  /**
   * 判断当前未读消息中是否有提及当前用户的消息存在。
   * @return flag indicating unread messages mention current user or not.
   */
  public boolean unreadMessagesMentioned() {
    return unreadMessagesMentioned;
  }

  /**
   * 发送一条非暂存消息
   *
   * @param message message instance.
   * @param callback callback function.
   * @since 3.0
   */
  public void sendMessage(AVIMMessage message, final AVIMConversationCallback callback) {
    sendMessage(message, null, callback);
  }

  /**
   * 发送一条消息。
   *
   * @param message message instance.
   * @param messageOption 消息发送选项。
   * @param callback callback function.
   *
   */
  public void sendMessage(final AVIMMessage message, final AVIMMessageOption messageOption, final AVIMConversationCallback callback) {
    message.setConversationId(conversationId);
    message.setFrom(client.getClientId());
    message.generateUniqueToken();
    message.setTimestamp(System.currentTimeMillis());
    if (!AppConfiguration.getGlobalNetworkingDetector().isConnected()) {
      // judge network status.
      message.setMessageStatus(AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed);
      if (callback != null) {
        callback.internalDone(new AVException(AVException.CONNECTION_FAILED, "Connection lost"));
      }
      return;
    }
    final AVIMCommonJsonCallback wrapperCallback = new AVIMCommonJsonCallback() {
      @Override
      public void done(Map<String, Object> result, AVIMException e) {
        if (null == e && null != result) {
          String msgId = (String) result.get(Conversation.callbackMessageId);
          Long msgTimestamp = (Long) result.get(Conversation.callbackMessageTimeStamp);
          message.setMessageId(msgId);
          if (null != msgTimestamp) {
            message.setTimestamp(msgTimestamp);
          }
          message.setMessageStatus(AVIMMessage.AVIMMessageStatus.AVIMMessageStatusSent);
          if ((null == messageOption || !messageOption.isTransient()) && AVIMOptions.getGlobalOptions().isMessageQueryCacheEnabled()) {
            setLastMessage(message);
            storage.insertMessage(message, false);
          } else {
            LOGGER.d("skip inserting into local storage.");
          }
          AVIMConversation.this.lastMessageAt = (null != msgTimestamp)? new Date(msgTimestamp) : new Date();
          storage.updateConversationLastMessageAt(AVIMConversation.this);
        } else {
          message.setMessageStatus(AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed);
        }
        if (null != callback) {
          callback.internalDone(AVIMException.wrapperAVException(e));
        }
      }
    };

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
            InternalConfiguration.getOperationTube().sendMessage(client.getClientId(), getConversationId(), getType(),
                    message, messageOption, wrapperCallback);
          }
        }
      });
    } else {
      InternalConfiguration.getOperationTube().sendMessage(client.getClientId(), getConversationId(), getType(),
              message, messageOption, wrapperCallback);
    }
  }

  private void copyMessageWithoutContent(AVIMMessage oldMessage, AVIMMessage newMessage) {
    newMessage.setMessageId(oldMessage.getMessageId());
    newMessage.setConversationId(oldMessage.getConversationId());
    newMessage.setFrom(oldMessage.getFrom());
    newMessage.setDeliveredAt(oldMessage.getDeliveredAt());
    newMessage.setReadAt(oldMessage.getReadAt());
    newMessage.setTimestamp(oldMessage.getTimestamp());
    newMessage.setMessageStatus(oldMessage.getMessageStatus());
    newMessage.setMessageIOType(oldMessage.getMessageIOType());
  }

  /**
   * update message content
   * @param oldMessage the message need to be modified
   * @param newMessage the content of the old message will be covered by the new message's
   * @param callback callback function.
   */
  public void updateMessage(final AVIMMessage oldMessage, final AVIMMessage newMessage, final AVIMMessageUpdatedCallback callback) {
    if (null == oldMessage || null == newMessage) {
      if (null != callback) {
        callback.internalDone(new AVException(new IllegalArgumentException("oldMessage/newMessage shouldn't be null")));
      }
      return;
    }
    final AVIMCommonJsonCallback tmpCallback = new AVIMCommonJsonCallback() {
      @Override
      public void done(Map<String, Object> result, AVIMException e) {
        if (null != e || null == result) {
          if (null != callback) {
            callback.internalDone(null, e);
          }
        } else {
          long patchTime = 0;
          if (result.containsKey(Conversation.PARAM_MESSAGE_PATCH_TIME)) {
            patchTime = (Long) result.get(Conversation.PARAM_MESSAGE_PATCH_TIME);
          }
          copyMessageWithoutContent(oldMessage, newMessage);
          newMessage.setUpdateAt(patchTime);
          updateLocalMessage(newMessage);
          if (null != callback) {
            callback.internalDone(newMessage, null);
          }
        }
      }
    };
    InternalConfiguration.getOperationTube().updateMessage(client.getClientId(), getType(), oldMessage, newMessage, tmpCallback);
  }

  /**
   * racall message
   * @param message the message need to be recalled
   * @param callback callback function.
   */
  public void recallMessage(final AVIMMessage message, final AVIMMessageRecalledCallback callback) {
    if (null == message) {
      if (null != callback) {
        callback.internalDone(new AVException(new IllegalArgumentException("message shouldn't be null")));
      }
      return;
    }
    final AVIMCommonJsonCallback tmpCallback = new AVIMCommonJsonCallback() {
      @Override
      public void done(Map<String, Object> result, AVIMException e) {
        if (null != e || null == result) {
          if (null != callback) {
            callback.internalDone(null, e);
          }
        } else {
          long patchTime = 0;
          if (result.containsKey(Conversation.PARAM_MESSAGE_PATCH_TIME)) {
            patchTime = (Long) result.get(Conversation.PARAM_MESSAGE_PATCH_TIME);
          }
          AVIMRecalledMessage recalledMessage = new AVIMRecalledMessage();
          copyMessageWithoutContent(message, recalledMessage);
          recalledMessage.setUpdateAt(patchTime);
          recalledMessage.setMessageStatus(AVIMMessage.AVIMMessageStatus.AVIMMessageStatusRecalled);
          updateLocalMessage(recalledMessage);

          if (null != callback) {
            callback.internalDone(recalledMessage, null);
          }
        }
      }
    };
    InternalConfiguration.getOperationTube().recallMessage(client.getClientId(), getType(), message, tmpCallback);
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
   * @param message message instance.
   */
  public void removeFromLocalCache(AVIMMessage message) {
    this.storage.removeLocalMessage(message);
  }

  /**
   * fetchReceiptTimestamps
   *
   * @param callback callback function.
   */
  public void fetchReceiptTimestamps(final AVIMConversationCallback callback) {
    final AVIMCommonJsonCallback tmpCallback = new AVIMCommonJsonCallback() {
      @Override
      public void done(Map<String, Object> result, AVIMException e) {
        if (null == callback) {
          return;
        }
        if (null != e || null == result) {
          callback.internalDone(e);
        } else {
          long readAt = 0;
          if (result.containsKey(Conversation.callbackReadAt)) {
            readAt = (Long) result.get(Conversation.callbackReadAt);
          }

          long deliveredAt = 0;
          if (result.containsKey(Conversation.callbackDeliveredAt)) {
            readAt = (Long) result.get(Conversation.callbackDeliveredAt);
          }
          AVIMConversation.this.setLastReadAt(readAt, false);
          AVIMConversation.this.setLastDeliveredAt(deliveredAt, false);
          storage.updateConversationTimes(AVIMConversation.this);
          callback.internalDone(null, null);
        }
      }
    };
    boolean ret = InternalConfiguration.getOperationTube().fetchReceiptTimestamps(client.getClientId(), getConversationId(),
            getType(),
            Conversation.AVIMOperation.CONVERSATION_FETCH_RECEIPT_TIME, tmpCallback);
    if (!ret && null != callback) {
      callback.internalDone(new AVException(AVException.OPERATION_FORBIDDEN, "couldn't send request in background."));
    }
  }

  /**
   * 查询最近的20条消息记录
   *
   * @param callback callback function.
   */
  public void queryMessages(final AVIMMessagesQueryCallback callback) {
    this.queryMessages(20, callback);
  }

  /**
   * 从服务器端拉取最新消息
   * @param limit result size.
   * @param callback callback function.
   */
  public void queryMessagesFromServer(int limit, final AVIMMessagesQueryCallback callback) {
    queryMessagesFromServer(null, 0, limit, null, 0, new AVIMMessagesQueryCallback() {
      @Override
      public void done(List<AVIMMessage> messages, AVIMException e) {
        if (null == e) {
          if (AVIMOptions.getGlobalOptions().isMessageQueryCacheEnabled()) {
            processContinuousMessages(messages);
          }
          callback.internalDone(messages, null);
        } else {
          callback.internalDone(null, e);
        }
      }
    });
  }

  /**
   * 从本地缓存中拉取消息
   * @param limit result size.
   * @param callback callback function.
   */
  public void queryMessagesFromCache(int limit, final AVIMMessagesQueryCallback callback) {
    queryMessagesFromCache(null, 0, limit, callback);
  }

  private void processContinuousMessages(List<AVIMMessage> messages) {
    if (null != messages && !messages.isEmpty()) {
      Collections.sort(messages, messageComparator);
      setLastMessage(messages.get(messages.size() - 1));
      storage.insertContinuousMessages(messages, conversationId);
    }
  }
  private void queryMessagesFromServer(String msgId, long timestamp, int limit,
                                       String toMsgId, long toTimestamp, final AVIMMessagesQueryCallback callback) {
    queryMessagesFromServer(msgId, timestamp, false, toMsgId, toTimestamp, false,
            AVIMMessageQueryDirection.AVIMMessageQueryDirectionFromNewToOld, limit, callback);
  }

  /**
   * 获取特停类型的历史消息。
   * 注意：这个操作总是会从云端获取记录。
   * 另，该函数和 queryMessagesByType(type, msgId, timestamp, limit, callback) 配合使用可以实现翻页效果。
   *
   * @param msgType     消息类型，可以参看  `AVIMMessageType` 里的定义。
   * @param limit       本批次希望获取的消息数量。
   * @param callback    结果回调函数
   */
  public void queryMessagesByType(int msgType, int limit, final AVIMMessagesQueryCallback callback) {
    queryMessagesByType(msgType, null, 0, limit, callback);
  }

  /**
   * 获取特定类型的历史消息。
   * 注意：这个操作总是会从云端获取记录。
   * 另，如果不指定 msgId 和 timestamp，则该函数效果等同于 queryMessageByType(type, limit, callback)
   *
   * @param msgType     消息类型，可以参看  `AVIMMessageType` 里的定义。
   * @param msgId       消息id，从特定消息 id 开始向前查询（结果不会包含该记录）
   * @param timestamp   查询起始的时间戳，返回小于这个时间的记录，必须配合 msgId 一起使用。
   *                    要从最新消息开始获取时，请用 0 代替客户端的本地当前时间（System.currentTimeMillis()）
   * @param limit       返回条数限制
   * @param callback    结果回调函数
   */
  public void queryMessagesByType(int msgType, final String msgId, final long timestamp, final int limit,
                                  final AVIMMessagesQueryCallback callback) {
    if (null == callback) {
      return;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_MESSAGE_QUERY_MSGID, msgId);
    params.put(Conversation.PARAM_MESSAGE_QUERY_TIMESTAMP, timestamp);
    params.put(Conversation.PARAM_MESSAGE_QUERY_STARTCLOSED, false);
    params.put(Conversation.PARAM_MESSAGE_QUERY_TO_MSGID, "");
    params.put(Conversation.PARAM_MESSAGE_QUERY_TO_TIMESTAMP, 0);
    params.put(Conversation.PARAM_MESSAGE_QUERY_TOCLOSED, false);
    params.put(Conversation.PARAM_MESSAGE_QUERY_DIRECT, AVIMMessageQueryDirection.AVIMMessageQueryDirectionFromNewToOld.getCode());
    params.put(Conversation.PARAM_MESSAGE_QUERY_LIMIT, limit);
    params.put(Conversation.PARAM_MESSAGE_QUERY_TYPE, msgType);
    boolean ret = InternalConfiguration.getOperationTube().queryMessages(this.client.getClientId(), getConversationId(),
            getType(), JSON.toJSONString(params),
            Conversation.AVIMOperation.CONVERSATION_MESSAGE_QUERY, callback);
    if (!ret) {
      callback.internalDone(new AVException(AVException.OPERATION_FORBIDDEN, "couldn't send request in background."));
    }
  }

  private void queryMessagesFromServer(String msgId, long timestamp, boolean startClosed,
                                       String toMsgId, long toTimestamp, boolean toClosed,
                                       AVIMMessageQueryDirection direction, int limit,
                                       AVIMMessagesQueryCallback cb) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_MESSAGE_QUERY_MSGID, msgId);
    params.put(Conversation.PARAM_MESSAGE_QUERY_TIMESTAMP, timestamp);
    params.put(Conversation.PARAM_MESSAGE_QUERY_STARTCLOSED, startClosed);
    params.put(Conversation.PARAM_MESSAGE_QUERY_TO_MSGID, toMsgId);
    params.put(Conversation.PARAM_MESSAGE_QUERY_TO_TIMESTAMP, toTimestamp);
    params.put(Conversation.PARAM_MESSAGE_QUERY_TOCLOSED, toClosed);
    params.put(Conversation.PARAM_MESSAGE_QUERY_DIRECT, direction.getCode());
    params.put(Conversation.PARAM_MESSAGE_QUERY_LIMIT, limit);
    params.put(Conversation.PARAM_MESSAGE_QUERY_TYPE, 0);
    boolean ret = InternalConfiguration.getOperationTube().queryMessages(this.client.getClientId(), getConversationId(),
            getType(), JSON.toJSONString(params),
            Conversation.AVIMOperation.CONVERSATION_MESSAGE_QUERY, cb);
    if (!ret && null != cb) {
      cb.internalDone(null,
              new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  private void queryMessagesFromCache(final String msgId, final long timestamp, final int limit,
                                      final AVIMMessagesQueryCallback callback) {
    if (null != callback) {
      storage.getMessages(msgId, timestamp, limit, conversationId,
              new AVIMMessageStorage.StorageQueryCallback() {
                @Override
                public void done(List<AVIMMessage> messages, List<Boolean> breakpoints) {
                  if (null != messages) {
                    Collections.reverse(messages);
                  }
                  callback.internalDone(messages, null);
                }
              });
    }
  }

  /**
   * 获取最新的消息记录
   *
   * @param limit result size.
   * @param callback callback function.
   */
  public void queryMessages(final int limit, final AVIMMessagesQueryCallback callback) {
    if (limit <= 0 || limit > 1000) {
      if (callback != null) {
        callback.internalDone(null, new AVException(new IllegalArgumentException(
                "limit should be in [1, 1000]")));
      }
    }
    // 如果屏蔽了本地缓存则全部走网络
    if (!AVIMOptions.getGlobalOptions().isMessageQueryCacheEnabled()) {
      queryMessagesFromServer(null, 0, limit, null, 0, new AVIMMessagesQueryCallback() {

        @Override
        public void done(List<AVIMMessage> messages, AVIMException e) {
          if (callback != null) {
            if (e != null) {
              callback.internalDone(e);
            } else {
              callback.internalDone(messages, null);
            }
          }
        }
      });
      return;
    }
    if (!AppConfiguration.getGlobalNetworkingDetector().isConnected()) {
      queryMessagesFromCache(null, 0, limit, callback);
    } else {
      // 选择最后一条有 breakpoint 为 false 的消息做截断，因为是 true 的话，会造成两次查询。
      // 在 queryMessages 还是遇到 breakpoint，再次查询了
      long cacheMessageCount = storage.getMessageCount(conversationId);
      long toTimestamp = 0;
      String toMsgId = null;
      // 如果本地的缓存的量都不够的情况下，应该要去服务器查询，以免第一次查询的时候出现limit跟返回值不一致让用户认为聊天记录已经到头的问题
      if (cacheMessageCount >= limit) {
        final AVIMMessage latestMessage =
                storage.getLatestMessageWithBreakpoint(conversationId, false);

        if (latestMessage != null) {
          toMsgId = latestMessage.getMessageId();
          toTimestamp = latestMessage.getTimestamp();
        }
      }

      // 去服务器查询最新消息，看是否在其它终端产生过消息。为省流量，服务器会截断至 toMsgId 、toTimestamp
      queryMessagesFromServer(null, 0, limit, toMsgId, toTimestamp,
              new AVIMMessagesQueryCallback() {
                @Override
                public void done(List<AVIMMessage> messages, AVIMException e) {
                  if (e != null) {
                    // 如果遇到本地错误或者网络错误，直接返回缓存数据
                    if (e.getCode() == AVIMException.TIMEOUT || e.getCode() == 0 || e.getCode() == 3000) {
                      queryMessagesFromCache(null, 0, limit, callback);
                    } else {
                      if (callback != null) {
                        callback.internalDone(e);
                      }
                    }
                  } else {
                    if (null == messages || messages.size() < 1) {
                      // 这种情况就说明我们的本地消息缓存是最新的
                    } else {
                      /*
                       * 1.messages.size()<=limit && messages.contains(latestMessage)
                       * 这种情况就说明在本地客户端退出后，该用户在其他客户端也产生了聊天记录而没有缓存到本地来,且产生了小于一页的聊天记录
                       * 2.messages==limit && !messages.contains(latestMessage)
                       * 这种情况就说明在本地客户端退出后，该用户在其他客户端也产生了聊天记录而没有缓存到本地来,且产生了大于一页的聊天记录
                       */

                      processContinuousMessages(messages);
                    }
                    queryMessagesFromCache(null, 0, limit, callback);
                  }
                }
              });
    }
  }

  /**
   * 查询消息记录，上拉时使用。
   *
   * @param msgId 消息id，从消息id开始向前查询
   * @param timestamp 查询起始的时间戳，返回小于这个时间的记录。
   *          客户端时间不可靠，请用 0 代替 System.currentTimeMillis()
   * @param limit 返回条数限制
   * @param callback callback function.
   */
  public void queryMessages(final String msgId, final long timestamp, final int limit,
                            final AVIMMessagesQueryCallback callback) {
    if (StringUtil.isEmpty(msgId) && timestamp == 0) {
      this.queryMessages(limit, callback);
      return;
    }
    // 如果屏蔽了本地缓存则全部走网络
    if (!AVIMOptions.getGlobalOptions().isMessageQueryCacheEnabled()) {
      queryMessagesFromServer(msgId, timestamp, limit, null, 0, new AVIMMessagesQueryCallback() {

        @Override
        public void done(List<AVIMMessage> messages, AVIMException e) {
          if (callback != null) {
            if (e != null) {
              callback.internalDone(e);
            } else {
              callback.internalDone(messages, null);
            }
          }
        }
      });
      return;
    }

    // 先去本地缓存查询消息
    storage.getMessage(msgId, timestamp, conversationId,
            new AVIMMessageStorage.StorageMessageCallback() {

              @Override
              public void done(final AVIMMessage indicatorMessage,
                               final boolean isIndicateMessageBreakPoint) {
                if (indicatorMessage == null || isIndicateMessageBreakPoint) {
                  String startMsgId = msgId;
                  long startTS = timestamp;
                  int requestLimit = limit;
                  queryMessagesFromServer(startMsgId, startTS, requestLimit, null, 0,
                          new AVIMMessagesQueryCallback() {
                            @Override
                            public void done(List<AVIMMessage> messages, AVIMException e) {
                              if (e != null) {
                                callback.internalDone(e);
                              } else {
                                List<AVIMMessage> cachedMsgs = new LinkedList<AVIMMessage>();
                                if (indicatorMessage != null) {
                                  // add indicatorMessage to remove breakpoint.
                                  cachedMsgs.add(indicatorMessage);
                                }
                                if (messages != null) {
                                  cachedMsgs.addAll(messages);
                                }
                                processContinuousMessages(cachedMsgs);
                                queryMessagesFromCache(msgId, timestamp, limit, callback);
                              }
                            }
                          });
                } else {
                  // 本地缓存过而且不是breakPoint
                  storage.getMessages(msgId, timestamp, limit, conversationId,
                          new AVIMMessageStorage.StorageQueryCallback() {
                            @Override
                            public void done(List<AVIMMessage> messages, List<Boolean> breakpoints) {
                              processStorageQueryResult(messages, breakpoints, msgId, timestamp, limit,
                                      callback);
                            }
                          });
                }
              }
            });
  }

  /**
   * 若发现有足够的连续消息，则直接返回。否则去服务器查询消息，同时消除断点。
   * */
  private void processStorageQueryResult(List<AVIMMessage> cachedMessages,
                                         List<Boolean> breakpoints, String originMsgId, long originMsgTS, int limit,
                                         final AVIMMessagesQueryCallback callback) {

    final List<AVIMMessage> continuousMessages = new ArrayList<AVIMMessage>();
    int firstBreakPointIndex = -1;
    for (int index = 0; index < cachedMessages.size(); index++) {
      if (breakpoints.get(index)) {
        firstBreakPointIndex = index;
        break;
      } else {
        continuousMessages.add(cachedMessages.get(index));
      }
    }
    final boolean connected = AppConfiguration.getGlobalNetworkingDetector().isConnected();
    // 如果只是最后一个消息是breakPoint，那还走啥网络
    if (!connected || continuousMessages.size() >= limit/* - 1*/) {
      // in case of wifi is invalid, and thre query list contain breakpoint, the result is error.
      Collections.sort(continuousMessages, messageComparator);
      callback.internalDone(continuousMessages, null);
    } else {
      final int restCount;
      final AVIMMessage startMessage;
      if (!continuousMessages.isEmpty()) {
        // 这里是缓存里面没有breakPoint，但是limit不够的情况下
        restCount = limit - continuousMessages.size();
        startMessage = continuousMessages.get(continuousMessages.size() - 1);
      } else {
        startMessage = null;
        restCount = limit;
      }
      queryMessagesFromServer(startMessage == null ? originMsgId : startMessage.messageId,
              startMessage == null ? originMsgTS : startMessage.timestamp, restCount, null, 0,
              new AVIMMessagesQueryCallback() {
                @Override
                public void done(List<AVIMMessage> serverMessages, AVIMException e) {
                  if (e != null) {
                    // 不管如何，先返回缓存里面已有的有效数据
                    if (continuousMessages.size() > 0) {
                      callback.internalDone(continuousMessages, null);
                    } else {
                      callback.internalDone(e);
                    }
                  } else {
                    if (serverMessages == null) {
                      serverMessages = new ArrayList<AVIMMessage>();
                    }
                    continuousMessages.addAll(serverMessages);
                    processContinuousMessages(continuousMessages);
                    callback.internalDone(continuousMessages, null);
                  }
                }
              });
    }
  }

  /**
   * 根据指定的区间来查询历史消息，可以指定区间开闭、查询方向以及最大条目限制
   * @param interval  - 区间，由起止 AVIMMessageIntervalBound 组成
   * @param direction - 查询方向，支持向前（AVIMMessageQueryDirection.AVIMMessageQueryDirectionFromNewToOld）
   *                    或向后（AVIMMessageQueryDirection.AVIMMessageQueryDirectionFromOldToNew）查询
   * @param limit     - 结果最大条目限制
   * @param callback  - 结果回调函数
   */
  public void queryMessages(final AVIMMessageInterval interval, AVIMMessageQueryDirection direction, final int limit,
                            final AVIMMessagesQueryCallback callback) {
    if (null == interval || limit < 0) {
      if (null != callback) {
        callback.internalDone(null,
                new AVException(new IllegalArgumentException("interval must not null, or limit must great than 0.")));
      }
      return;
    }
    String mid = null;
    long ts = 0;
    boolean startClosed = false;
    String tmid = null;
    long tts = 0;
    boolean endClosed = false;
    if (null != interval.startIntervalBound) {
      mid = interval.startIntervalBound.messageId;
      ts = interval.startIntervalBound.timestamp;
      startClosed = interval.startIntervalBound.closed;
    }
    if (null != interval.endIntervalBound) {
      tmid = interval.endIntervalBound.messageId;
      tts = interval.endIntervalBound.timestamp;
      endClosed = interval.endIntervalBound.closed;
    }
    queryMessagesFromServer(mid, ts, startClosed, tmid, tts, endClosed, direction, limit, callback);
  }


  /**
   * 获取当前对话的所有角色信息
   * @param offset    查询结果的起始点
   * @param limit     查询结果集上限
   * @param callback  结果回调函数
   */
  public void getAllMemberInfo(int offset, int limit, final AVIMConversationMemberQueryCallback callback) {
    QueryConditions conditions = new QueryConditions();
    conditions.addWhereItem("cid", QueryOperation.EQUAL_OP, this.conversationId);
    conditions.setSkip(offset);
    conditions.setLimit(limit);
    queryMemberInfo(conditions, callback);
  }

  /**
   * 获取对话内指定成员的角色信息
   * @param memberId  成员的 clientid
   * @param callback  结果回调函数
   */
  public void getMemberInfo(final String memberId, final AVIMConversationMemberQueryCallback callback) {
    QueryConditions conditions = new QueryConditions();
    conditions.addWhereItem("cid", QueryOperation.EQUAL_OP, this.conversationId);
    conditions.addWhereItem("peerId", QueryOperation.EQUAL_OP, memberId);
    queryMemberInfo(conditions, callback);
  }

  private void queryMemberInfo(final QueryConditions queryConditions, final AVIMConversationMemberQueryCallback callback) {
    if (null == queryConditions || null == callback) {
      return;
    }
    client.queryConversationMemberInfo(queryConditions, callback);
  }

  /**
   * 在聊天对话中间增加新的参与者
   *
   * @param memberIds member id list.
   * @param callback callback function.
   * @since 3.0
   */
  public void addMembers(final List<String> memberIds, final AVIMConversationCallback callback) {
    if (null == memberIds || memberIds.size() < 1) {
      if (null != callback) {
        callback.done(new AVIMException(new IllegalArgumentException("memberIds is null")));
      }
      return;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CONVERSATION_MEMBER, memberIds);
    boolean ret = InternalConfiguration.getOperationTube().processMembers(this.client.getClientId(), this.conversationId,
            getType(), JSON.toJSONString(params), Conversation.AVIMOperation.CONVERSATION_ADD_MEMBER, callback);
    if (!ret && null != callback) {
      callback.internalDone(null,
              new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  /**
   * 在聊天对话中间增加新的参与者
   *
   * @param memberIds member id list.
   * @param callback callback function.
   * @since 3.0
   */
  public void kickMembers(final List<String> memberIds, final AVIMConversationCallback callback) {
    if (null == memberIds || memberIds.size() < 1) {
      if (null != callback) {
        callback.done(new AVIMException(new IllegalArgumentException("memberIds is null")));
      }
      return;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CONVERSATION_MEMBER, memberIds);
    boolean ret = InternalConfiguration.getOperationTube().processMembers(this.client.getClientId(), this.conversationId,
            getType(), JSON.toJSONString(params), Conversation.AVIMOperation.CONVERSATION_RM_MEMBER, callback);
    if (!ret && null != callback) {
      callback.internalDone(null,
              new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  /**
   * 更新成员的角色信息
   * @param memberId  成员的 client id
   * @param role      角色
   * @param callback  结果回调函数
   */
  public void updateMemberRole(final String memberId, final ConversationMemberRole role, final AVIMConversationCallback callback) {
    AVIMConversationMemberInfo info = new AVIMConversationMemberInfo(this.conversationId, memberId, role);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CONVERSATION_MEMBER_DETAILS, info.getUpdateAttrs());
    boolean ret = InternalConfiguration.getOperationTube().processMembers(this.client.getClientId(), this.conversationId,
            getType(), JSON.toJSONString(params), Conversation.AVIMOperation.CONVERSATION_PROMOTE_MEMBER, callback);
    if (!ret && null != callback) {
      callback.internalDone(new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  /**
   * 将部分成员禁言
   * @param memberIds  成员列表
   * @param callback   结果回调函数
   */
  public void muteMembers(final List<String> memberIds, final AVIMOperationPartiallySucceededCallback callback) {
    if (null == memberIds || memberIds.size() < 1) {
      if (null != callback) {
        callback.done(new AVIMException(new IllegalArgumentException("memberIds is null")), null, null);
      }
      return;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CONVERSATION_MEMBER, memberIds);
    boolean ret = InternalConfiguration.getOperationTube().processMembers(this.client.getClientId(), this.conversationId,
            getType(), JSON.toJSONString(params), Conversation.AVIMOperation.CONVERSATION_MUTE_MEMBER, callback);
    if (!ret && null != callback) {
      callback.internalDone(null,
              new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  /**
   * 将部分成员解除禁言
   * @param memberIds  成员列表
   * @param callback   结果回调函数
   */
  public void unmuteMembers(final List<String> memberIds, final AVIMOperationPartiallySucceededCallback callback) {
    if (null == memberIds || memberIds.size() < 1) {
      if (null != callback) {
        callback.done(new AVIMException(new IllegalArgumentException("memberIds is null")), null, null);
      }
      return;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CONVERSATION_MEMBER, memberIds);
    boolean ret = InternalConfiguration.getOperationTube().processMembers(this.client.getClientId(), this.conversationId,
            getType(), JSON.toJSONString(params), Conversation.AVIMOperation.CONVERSATION_UNMUTE_MEMBER, callback);
    if (!ret && null != callback) {
      callback.internalDone(null,
              new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  /**
   * 查询被禁言的成员列表
   * @param offset    查询结果的起始点
   * @param limit     查询结果集上限
   * @param callback  结果回调函数
   */
  public void queryMutedMembers(int offset, int limit, final AVIMConversationSimpleResultCallback callback) {
    if (null == callback) {
      return;
    } else if (offset < 0 || limit > 100) {
      callback.internalDone(null, new AVIMException(new IllegalArgumentException("offset/limit is illegal.")));
      return;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.QUERY_PARAM_LIMIT, limit);
    params.put(Conversation.QUERY_PARAM_OFFSET, offset);
    boolean ret = InternalConfiguration.getOperationTube().processMembers(this.client.getClientId(), this.conversationId,
            getType(), JSON.toJSONString(params), Conversation.AVIMOperation.CONVERSATION_MUTED_MEMBER_QUERY, callback);
    if (!ret) {
      callback.internalDone(null,
              new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  /**
   * 将部分成员加入黑名单
   * @param memberIds  成员列表
   * @param callback   结果回调函数
   */
  public void blockMembers(final List<String> memberIds, final AVIMOperationPartiallySucceededCallback callback) {
    if (null == memberIds || memberIds.size() < 1) {
      if (null != callback) {
        callback.done(new AVIMException(new IllegalArgumentException("memberIds is null")), null, null);
      }
      return;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CONVERSATION_MEMBER, memberIds);
    boolean ret = InternalConfiguration.getOperationTube().processMembers(this.client.getClientId(), this.conversationId,
            getType(), JSON.toJSONString(params), Conversation.AVIMOperation.CONVERSATION_BLOCK_MEMBER, callback);
    if (!ret && null != callback) {
      callback.internalDone(new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  /**
   * 将部分成员从黑名单移出来
   * @param memberIds  成员列表
   * @param callback   结果回调函数
   */
  public void unblockMembers(final List<String> memberIds, final AVIMOperationPartiallySucceededCallback callback) {
    if (null == memberIds || memberIds.size() < 1) {
      if (null != callback) {
        callback.done(new AVIMException(new IllegalArgumentException("memberIds is null")), null, null);
      }
      return;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CONVERSATION_MEMBER, memberIds);
    boolean ret = InternalConfiguration.getOperationTube().processMembers(this.client.getClientId(), this.conversationId,
            getType(), JSON.toJSONString(params), Conversation.AVIMOperation.CONVERSATION_UNBLOCK_MEMBER, callback);
    if (!ret && null != callback) {
      callback.internalDone(new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  /**
   * 查询黑名单的成员列表
   * @param offset    查询结果的起始点
   * @param limit     查询结果集上限
   * @param callback  结果回调函数
   */
  public void queryBlockedMembers(int offset, int limit, final AVIMConversationSimpleResultCallback callback) {
    if (null == callback) {
      return;
    } else if (offset < 0 || limit > 100) {
      callback.internalDone(null, new AVIMException(new IllegalArgumentException("offset/limit is illegal.")));
      return;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.QUERY_PARAM_LIMIT, limit);
    params.put(Conversation.QUERY_PARAM_OFFSET, offset);
    boolean ret = InternalConfiguration.getOperationTube().processMembers(this.client.getClientId(), this.conversationId,
            getType(), JSON.toJSONString(params), Conversation.AVIMOperation.CONVERSATION_BLOCKED_MEMBER_QUERY, callback);
    if (!ret) {
      callback.internalDone(null,
              new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
    }
  }

  /**
   * 查询成员数量
   * @param callback callback function.
   */
  public void getMemberCount(AVIMConversationMemberCountCallback callback) {
    if (StringUtil.isEmpty(getConversationId())) {
      if (null != callback) {
        callback.internalDone(new AVException(AVException.INVALID_QUERY, "ConversationId is empty"));
      } else {
        LOGGER.w("ConversationId is empty");
      }
      return;
    }
    InternalConfiguration.getOperationTube().processMembers(client.getClientId(), conversationId, getType(), null,
            Conversation.AVIMOperation.CONVERSATION_MEMBER_COUNT_QUERY, callback);
  }

  /**
   * 静音，客户端拒绝收到服务器端的离线推送通知
   *
   * @param callback callback function.
   */
  public void mute(final AVIMConversationCallback callback) {
    if (StringUtil.isEmpty(getConversationId())) {
      if (null != callback) {
        callback.internalDone(new AVException(AVException.INVALID_QUERY, "ConversationId is empty"));
      } else {
        LOGGER.w("ConversationId is empty");
      }
      return;
    }
    InternalConfiguration.getOperationTube().participateConversation(client.getClientId(), conversationId, getType(),
            null, Conversation.AVIMOperation.CONVERSATION_MUTE, callback);
  }

  /**
   * 取消静音，客户端取消静音设置
   *
   * @param callback callback function.
   */
  public void unmute(final AVIMConversationCallback callback) {
    if (StringUtil.isEmpty(getConversationId())) {
      if (null != callback) {
        callback.internalDone(new AVException(AVException.INVALID_QUERY, "ConversationId is empty"));
      } else {
        LOGGER.w("ConversationId is empty");
      }
      return;
    }
    InternalConfiguration.getOperationTube().participateConversation(client.getClientId(), conversationId, getType(),
            null, Conversation.AVIMOperation.CONVERSATION_UNMUTE, callback);
  }

  /**
   * 退出当前的聊天对话
   *
   * @param callback callback function.
   * @since 3.0
   */
  public void quit(final AVIMConversationCallback callback) {
    if (StringUtil.isEmpty(getConversationId())) {
      if (null != callback) {
        callback.internalDone(new AVException(AVException.INVALID_QUERY, "ConversationId is empty"));
      } else {
        LOGGER.w("ConversationId is empty");
      }
      return;
    }
    InternalConfiguration.getOperationTube().participateConversation(client.getClientId(), conversationId, getType(),
            null, Conversation.AVIMOperation.CONVERSATION_QUIT, callback);
  }

  /**
   * 加入当前聊天对话
   *
   * @param callback callback function.
   */
  public void join(AVIMConversationCallback callback) {
    if (StringUtil.isEmpty(getConversationId())) {
      if (null != callback) {
        callback.internalDone(new AVException(AVException.INVALID_QUERY, "ConversationId is empty"));
      } else {
        LOGGER.w("ConversationId is empty");
      }
      return;
    }
    InternalConfiguration.getOperationTube().participateConversation(client.getClientId(), conversationId, getType(),
            null, Conversation.AVIMOperation.CONVERSATION_JOIN, callback);
  }

  /**
   * 清除未读消息
   */
  public void read() {
    if (!isTransient) {
      AVIMMessage lastMessage = getLastMessage();
      Map<String, Object> params = new HashMap<String, Object>();
      if (null != lastMessage) {
        params.put(Conversation.PARAM_MESSAGE_QUERY_MSGID, lastMessage.getMessageId());
        params.put(Conversation.PARAM_MESSAGE_QUERY_TIMESTAMP, lastMessage.getTimestamp());
      }
      InternalConfiguration.getOperationTube().markConversationRead(client.getClientId(), conversationId, getType(), params);
    }
  }

  /**
   * 更新当前对话的属性至服务器端
   *
   * @param callback callback function.
   * @since 3.0
   */
  public void updateInfoInBackground(final AVIMConversationCallback callback) {
    if (!pendingAttributes.isEmpty() || !pendingInstanceData.isEmpty()) {
      Map<String, Object> attributesMap = new HashMap<>();
      if (!pendingAttributes.isEmpty()) {
        final Map<String, Object> pendingAttrMap = processAttributes(pendingAttributes, false);
        if (null != pendingAttrMap) {
          attributesMap.putAll(pendingAttrMap);
        }
      }

      if (!pendingInstanceData.isEmpty()) {
        attributesMap.putAll(pendingInstanceData);
      }

//      Map<String, Object> params = new HashMap<String, Object>();
//      if (!attributesMap.isEmpty()) {
//        params.put(Conversation.PARAM_CONVERSATION_ATTRIBUTE, attributesMap);
//      }
      final AVIMCommonJsonCallback tmpCallback = new AVIMCommonJsonCallback() {
        @Override
        public void done(Map<String, Object> result, AVIMException e) {
          if (null == e) {
            attributes.putAll(pendingAttributes);
            pendingAttributes.clear();
            instanceData.putAll(pendingInstanceData);
            pendingInstanceData.clear();
            storage.insertConversations(Arrays.asList(AVIMConversation.this));
          }
          if (null != callback) {
            callback.internalDone(e);
          }
        }
      };
      InternalConfiguration.getOperationTube().updateConversation(this.client.getClientId(), this.conversationId, this.getType(),
              attributesMap, tmpCallback);
    } else {
      if (null != callback) {
        callback.internalDone(null);
      }
    }
  }

  /**
   * Fetch info in async mode.
   * @param callback callback function.
   */
  public void fetchInfoInBackground(final AVIMConversationCallback callback) {
    if (StringUtil.isEmpty(getConversationId())) {
      if (null != callback) {
        callback.internalDone(new AVException(AVException.INVALID_QUERY, "ConversationId is empty"));
      } else {
        LOGGER.w("ConversationId is empty");
      }
      return;
    }
    Map<String, Object> params = getFetchRequestParams();
    InternalConfiguration.getOperationTube().queryConversations(this.client.getClientId(),
            JSON.toJSONString(params), new AVIMCommonJsonCallback() {
              @Override
              public void done(Map<String, Object> result, AVIMException e) {
                if (null == e && null != result) {
                  e = processQueryResult((String)result.get(Conversation.callbackData));
                }
                if (null != callback) {
                  callback.internalDone(null, e);
                }
              }
            });
  }

  void updateLocalMessage(AVIMMessage message) {
    storage.updateMessageForPatch(message);
  }

  public static void mergeConversationFromJsonObject(AVIMConversation conversation, JSONObject jsonObj) {
    if (null == conversation || null == jsonObj) {
      return;
    }
    // Notice: cannot update deleted attr.
    HashMap<String, Object> attributes = new HashMap<String, Object>();
    if (jsonObj.containsKey(Conversation.NAME)) {
      attributes.put(Conversation.NAME, jsonObj.getString(Conversation.NAME));
    }
    if (jsonObj.containsKey(Conversation.ATTRIBUTE)) {
      JSONObject moreAttributes = jsonObj.getJSONObject(Conversation.ATTRIBUTE);
      if (moreAttributes != null) {
        Map<String, Object> moreAttributesMap = JSON.toJavaObject(moreAttributes, Map.class);
        attributes.putAll(moreAttributesMap);
      }
    }
    conversation.attributes.putAll(attributes);
    for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
      String key = entry.getKey();
      if (!Arrays.asList(Conversation.CONVERSATION_COLUMNS).contains(key)) {
        conversation.instanceData.put(key, entry.getValue());
      }
    }
    conversation.latestConversationFetch = System.currentTimeMillis();
  }

  public Map<String, Object> getFetchRequestParams() {
    Map<String, Object> params = new HashMap<String, Object>();
    if (conversationId.startsWith(Conversation.TEMPCONV_ID_PREFIX)) {
      params.put(Conversation.QUERY_PARAM_TEMPCONV, conversationId);
    } else {
      Map<String, Object> whereMap = new HashMap<String, Object>();
      whereMap.put("objectId", conversationId);
      params.put(Conversation.QUERY_PARAM_WHERE, whereMap);
    }
    return params;
  }

  /**
   * 处理 AVIMConversation attr 列
   * 因为 sdk 支持增量更新与覆盖更新，而增量更新与覆盖更新需要的结构不一样，所以这里处理一下
   * 具体格式可参照下边的注释，注意，两种格式不能同时存在，否则 server 会报错
   * @param attributes attribute map
   * @param isCovered flag indicating to overwrite or not.
   * @return new attribute map.
   */
  static Map<String, Object> processAttributes(Map<String, Object> attributes, boolean isCovered) {
    if (isCovered) {
      return processAttributesForCovering(attributes);
    } else {
      return processAttributesForIncremental(attributes);
    }
  }

  /**
   * 增量更新 attributes
   * 这里处理完的格式应该类似为 {"attr.key1":"value2", "attr.key2":"value2"}
   * @param attributes attribute map
   * @return new attribute map.
   */
  static Map<String, Object> processAttributesForIncremental(Map<String, Object> attributes) {
    Map<String, Object> attributeMap = new HashMap<>();
    if (attributes.containsKey(Conversation.NAME)) {
      attributeMap.put(Conversation.NAME, attributes.get(Conversation.NAME));
    }
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      String k = entry.getKey();
      if (!Arrays.asList(Conversation.CONVERSATION_COLUMNS).contains(k)) {
        attributeMap.put(ATTR_PERFIX + k, entry.getValue());
      }
    }
    if (attributeMap.isEmpty()) {
      return null;
    }
    return attributeMap;
  }

  /**
   * 覆盖更新 attributes
   * 这里处理完的格式应该类似为 {"attr":{"key1":"value1","key2":"value2"}}
   * @param attributes attribute map
   * @return json object.
   */
  static JSONObject processAttributesForCovering(Map<String, Object> attributes) {
    HashMap<String, Object> attributeMap = new HashMap<String, Object>();
    if (attributes.containsKey(Conversation.NAME)) {
      attributeMap.put(Conversation.NAME,
              attributes.get(Conversation.NAME));
    }
    Map<String, Object> innerAttribute = new HashMap<String, Object>();
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      String k = entry.getKey();
      if (!Arrays.asList(Conversation.CONVERSATION_COLUMNS).contains(k)) {
        innerAttribute.put(k, entry.getValue());
      }
    }
    if (!innerAttribute.isEmpty()) {
      attributeMap.put(Conversation.ATTRIBUTE, innerAttribute);
    }
    if (attributeMap.isEmpty()) {
      return null;
    }
    return new JSONObject(attributeMap);
  }

  /**
   * parse AVIMConversation from jsonObject
   * @param client client instance
   * @param jsonObj json object
   * @return conversation instance.
   */
  public static AVIMConversation parseFromJson(AVIMClient client, JSONObject jsonObj) {
    if (null == jsonObj || null == client) {
      return null;
    }

    String conversationId = jsonObj.getString(AVObject.KEY_OBJECT_ID);
    if (StringUtil.isEmpty(conversationId)) {
      return null;
    }
    boolean systemConv = false;
    boolean transientConv = false;
    boolean tempConv = false;
    if (jsonObj.containsKey(Conversation.SYSTEM)) {
      systemConv = jsonObj.getBoolean(Conversation.SYSTEM);
    }
    if (jsonObj.containsKey(Conversation.TRANSIENT)) {
      transientConv = jsonObj.getBoolean(Conversation.TRANSIENT);
    }
    if (jsonObj.containsKey(Conversation.TEMPORARY)) {
      tempConv = jsonObj.getBoolean(Conversation.TEMPORARY);
    }
    AVIMConversation originConv = null;
    if (systemConv) {
      originConv = new AVIMServiceConversation(client, conversationId);
    } else if (tempConv) {
      originConv = new AVIMTemporaryConversation(client, conversationId);
    } else if (transientConv) {
      originConv = new AVIMChatRoom(client, conversationId);
    } else {
      originConv = new AVIMConversation(client, conversationId);
    }
    originConv.latestConversationFetch = System.currentTimeMillis();

    return updateConversation(originConv, jsonObj);
  }

  static AVIMConversation updateConversation(AVIMConversation conversation, JSONObject jsonObj) {
    if (null == jsonObj || null == conversation) {
      return conversation;
    }

    String conversationId = jsonObj.getString(AVObject.KEY_OBJECT_ID);
    List<String> m = jsonObj.getObject(Conversation.MEMBERS, List.class);
    conversation.setMembers(m);
    conversation.setCreator(jsonObj.getString(Conversation.CREATOR));
    HashMap<String, Object> attributes = new HashMap<String, Object>();
    if (jsonObj.containsKey(Conversation.NAME)) {
      attributes.put(Conversation.NAME, jsonObj.getString(Conversation.NAME));
    }
    if (jsonObj.containsKey(Conversation.ATTRIBUTE)) {
      JSONObject moreAttributes = jsonObj.getJSONObject(Conversation.ATTRIBUTE);
      if (moreAttributes != null) {
        Map<String, Object> moreAttributesMap = JSON.toJavaObject(moreAttributes, Map.class);
        attributes.putAll(moreAttributesMap);
      }
    }
    conversation.setAttributesForInit(attributes);

    conversation.instanceData.clear();
    for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
      String key = entry.getKey();
      if (!Arrays.asList(Conversation.CONVERSATION_COLUMNS).contains(key)) {
        conversation.instanceData.put(key, entry.getValue());
      }
    }

    if (jsonObj.containsKey(Conversation.SYSTEM)) {
      conversation.instanceData.put(Conversation.SYSTEM, jsonObj.get(Conversation.SYSTEM));
    }

    if (jsonObj.containsKey(Conversation.MUTE)) {
      conversation.instanceData.put(Conversation.MUTE, jsonObj.get(Conversation.MUTE));
    }

    if (jsonObj.containsKey(AVObject.KEY_CREATED_AT)) {
      conversation.setCreatedAt(jsonObj.getString(AVObject.KEY_CREATED_AT));
    }

    if (jsonObj.containsKey(AVObject.KEY_UPDATED_AT)) {
      conversation.setUpdatedAt(jsonObj.getString(AVObject.KEY_UPDATED_AT));
    }

    AVIMMessage message = AVIMTypedMessage.parseMessage(conversationId, jsonObj);
    conversation.setLastMessage(message);

    if (jsonObj.containsKey(Conversation.LAST_MESSAGE_AT)) {
      conversation.setLastMessageAt(Utils.dateFromMap(jsonObj.getObject(Conversation.LAST_MESSAGE_AT, Map.class)));
    }

    if (jsonObj.containsKey(Conversation.TRANSIENT)) {
      conversation.isTransient = jsonObj.getBoolean(Conversation.TRANSIENT);
    }

    return conversation;
  }

  public AVIMException processQueryResult(String result) {
    if (null != result) {
      try {
        JSONArray jsonArray = JSON.parseArray(String.valueOf(result));
        if (null != jsonArray && !jsonArray.isEmpty()) {
          JSONObject jsonObject = jsonArray.getJSONObject(0);
          updateConversation(this, jsonObject);
          client.mergeConversationCache(this, true, null);
          storage.insertConversations(Arrays.asList(this));
          latestConversationFetch = System.currentTimeMillis();
        } else {
          // not found conversation
          return new AVIMException(9100, "Conversation not found");
        }
      } catch (Exception e) {
        return AVIMException.wrapperAVException(e);
      }
    } else {
      return new AVIMException(9100, "Conversation not found");
    }
    return null;
  }
  static Comparator<AVIMMessage> messageComparator = new Comparator<AVIMMessage>() {
    @Override
    public int compare(AVIMMessage m1, AVIMMessage m2) {
      if (m1.getTimestamp() < m2.getTimestamp()) {
        return -1;
      } else if (m1.getTimestamp() > m2.getTimestamp()) {
        return 1;
      } else {
        return m1.messageId.compareTo(m2.messageId);
      }
    }
  };

  interface OperationCompleteCallback {
    void onComplete();

    void onFailure();
  }
}
