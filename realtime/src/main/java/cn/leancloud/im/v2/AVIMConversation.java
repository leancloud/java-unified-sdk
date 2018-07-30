package cn.leancloud.im.v2;

import cn.leancloud.AVException;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.v2.callback.AVIMConversationCallback;
import cn.leancloud.im.v2.callback.AVIMMessageRecalledCallback;
import cn.leancloud.im.v2.callback.AVIMMessageUpdatedCallback;
import cn.leancloud.im.v2.callback.AVIMMessagesQueryCallback;
import cn.leancloud.im.v2.messages.AVIMFileMessage;
import cn.leancloud.im.v2.messages.AVIMFileMessageAccessor;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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

  /**
   * 获取Conversation的创建时间
   *
   * @return
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
   * @return
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
    if (!AppConfiguration.getGlobalNetworkingDetector().isConnected()) {
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
            InternalConfiguration.getOperationTube().sendMessage(client.getClientId(), getConversationId(), getType(),
                    message, messageOption, callback);
          }
        }
      });
    } else {
      InternalConfiguration.getOperationTube().sendMessage(client.getClientId(), getConversationId(), getType(),
              message, messageOption, callback);
    }
  }

  /**
   * update message content
   * @param oldMessage the message need to be modified
   * @param newMessage the content of the old message will be covered by the new message's
   * @param callback
   */
  public void updateMessage(AVIMMessage oldMessage, AVIMMessage newMessage, AVIMMessageUpdatedCallback callback) {
    if (null == oldMessage || null == newMessage) {
      if (null != callback) {
        callback.internalDone(new AVException(new IllegalArgumentException("oldMessage/newMessage shouldn't be null")));
      }
      return;
    }
    InternalConfiguration.getOperationTube().updateMessage(client.getClientId(), getType(), oldMessage, newMessage, callback);
  }

  /**
   * racall message
   * @param message the message need to be recalled
   * @param callback
   */
  public void recallMessage(AVIMMessage message, AVIMMessageRecalledCallback callback) {
    if (null == message) {
      if (null != callback) {
        callback.internalDone(new AVException(new IllegalArgumentException("message shouldn't be null")));
      }
      return;
    }
    InternalConfiguration.getOperationTube().recallMessage(client.getClientId(), getType(), message, callback);
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

  public void fetchReceiptTimestamps(AVIMConversationCallback callback) {
    boolean ret = InternalConfiguration.getOperationTube().fetchReceiptTimestamps(client.getClientId(), getConversationId(),
            Conversation.AVIMOperation.CONVERSATION_FETCH_RECEIPT_TIME, callback);
    if (!ret && null != callback) {
      callback.internalDone(new AVException(AVException.OPERATION_FORBIDDEN, "couldn't send request in background."));
    }
  }

  /**
   * 查询最近的20条消息记录
   *
   * @param callback
   */
  public void queryMessages(final AVIMMessagesQueryCallback callback) {
    this.queryMessages(20, callback);
  }

  /**
   * 从服务器端拉取最新消息
   * @param limit
   * @param callback
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
   * @param limit
   * @param callback
   */
  public void queryMessagesFromCache(int limit, AVIMMessagesQueryCallback callback) {
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
                                       String toMsgId, long toTimestamp, AVIMMessagesQueryCallback callback) {
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
                  Collections.reverse(messages);
                  callback.internalDone(messages, null);
                }
              });
    }
  }

  /**
   * 获取最新的消息记录
   *
   * @param limit
   * @param callback
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
   * @param callback
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
    Set<String> keySet = jsonObj.keySet();
    if (!keySet.isEmpty()) {
      for (String key : keySet) {
        if (!Arrays.asList(Conversation.CONVERSATION_COLUMNS).contains(key)) {
          conversation.instanceData.put(key, jsonObj.get(key));
        }
      }
    }
    // conversation.latestConversationFetch = System.currentTimeMillis();
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
