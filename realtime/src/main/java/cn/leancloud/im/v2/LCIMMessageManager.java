package cn.leancloud.im.v2;

import cn.leancloud.LCLogger;
import cn.leancloud.im.LCIMOptions;
import cn.leancloud.im.v2.annotation.LCIMMessageType;
import cn.leancloud.im.v2.messages.*;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;

import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class LCIMMessageManager {
  protected static final LCLogger LOGGER = LogUtil.getLogger(LCIMMessageManager.class);

  static Map<Integer, Class<? extends LCIMTypedMessage>> messageTypesRepository =
          new HashMap<>();
  static LCIMMessageHandler defaultMessageHandler;
  static ConcurrentMap<Class<? extends LCIMMessage>, Set<MessageHandler>> messageHandlerRepository =
          new ConcurrentHashMap<Class<? extends LCIMMessage>, Set<MessageHandler>>();

  static LCIMConversationEventHandler conversationEventHandler;

  static {
    registerAVIMMessageType(LCIMTextMessage.class);
    registerAVIMMessageType(LCIMFileMessage.class);
    registerAVIMMessageType(LCIMImageMessage.class);
    registerAVIMMessageType(LCIMAudioMessage.class);
    registerAVIMMessageType(LCIMVideoMessage.class);
    registerAVIMMessageType(LCIMLocationMessage.class);
    registerAVIMMessageType(LCIMRecalledMessage.class);
  }

  /**
   * 注册自定义的消息类型
   *
   * @param messageType message type.
   */
  public static void registerAVIMMessageType(Class<? extends LCIMTypedMessage> messageType) {
    LCIMMessageType type = messageType.getAnnotation(LCIMMessageType.class);
    if (type == null) {
      throw new IncompleteAnnotationException(LCIMMessageType.class, "type");
    }
    int messageTypeValue = type.type();

    messageTypesRepository.put(messageTypeValue, messageType);
    try {
      Method initializeMethod = messageType.getDeclaredMethod("computeFieldAttribute", Class.class);
      initializeMethod.setAccessible(true);
      initializeMethod.invoke(null, messageType);
    } catch (Exception e) {
      LOGGER.d("failed to initialize message Fields");
    }
  }

  /**
   * 注册一般情况下的消息handler，只有在没有类型的AVIMMessage或者没有其他handler时才会被调用
   *
   * 请在Application初始化时设置
   *
   * @param handler message handler.
   */
  public static void registerDefaultMessageHandler(LCIMMessageHandler handler) {
    defaultMessageHandler = handler;
  }

  /**
   * 注册特定消息格式的处理单元
   *
   * @param clazz 特定的消息类
   * @param handler message handler.
   */
  public static void registerMessageHandler(Class<? extends LCIMMessage> clazz,
                                            MessageHandler<?> handler) {
    Set<MessageHandler> handlerSet = new CopyOnWriteArraySet<>();

    Set<MessageHandler> set = messageHandlerRepository.putIfAbsent(clazz, handlerSet);
    if (set != null) {
      handlerSet = set;
    }
    handlerSet.add(handler);
  }

  /**
   * 取消特定消息格式的处理单元
   *
   * @param clazz message class.
   * @param handler message handler.
   */
  public static void unregisterMessageHandler(Class<? extends LCIMMessage> clazz,
                                              MessageHandler<?> handler) {
    Set<MessageHandler> handlerSet = messageHandlerRepository.get(clazz);
    if (handlerSet != null) {
      handlerSet.remove(handler);
    }
  }

  /**
   * 设置Conversataion相关事件的处理单元,
   *
   * 推荐在Application初始化时设置
   *
   * @param handler message handler.
   */
  public static void setConversationEventHandler(LCIMConversationEventHandler handler) {
    conversationEventHandler = handler;
  }

  protected static LCIMConversationEventHandler getConversationEventHandler() {
    return conversationEventHandler;
  }

  protected static void processMessage(LCIMMessage message, int convType, LCIMClient client, boolean hasMore,
                                       boolean isTransient) {
    // 如果已经通过拉获得了消息，则推的消息不回调
    if (client.getStorage().containMessage(message)) {
      return;
    }
    // hasMore 才设为 breakpoint
    // LogUtil.log.i("hasMore = " + hasMore);\
    if (!isTransient && LCIMOptions.getGlobalOptions().isMessageQueryCacheEnabled()) {
      client.getStorage().insertMessage(message, hasMore);
    }
    message = parseTypedMessage(message);
    message.setTransient(isTransient);

    final LCIMConversation conversation = client.getConversation(message.getConversationId(), convType);
    conversation.setLastMessage(message);
    if (!isTransient) {
      conversation.increaseUnreadCount(1, message.mentioned());
    }
    conversation.setLastMessageAt(new Date(message.getTimestamp()));

    // add notification: unreadMessageCountUpdated.
    if (!isTransient && null != conversationEventHandler) {
      if (LCIMOptions.getGlobalOptions().isOnlyPushCount()) {
        AbstractMap.SimpleEntry<Integer, Boolean> unreadInfo = new AbstractMap.SimpleEntry<>(conversation.getUnreadMessagesCount(),
                message.mentioned());
        conversationEventHandler.processEvent(Conversation.STATUS_ON_UNREAD_EVENT, message, unreadInfo, conversation);
      }
    }

    retrieveAllMessageHandlers(message, conversation, false, null);
  }

  protected static void processMessageReceipt(LCIMMessage message, LCIMClient client, String from) {
    client.getStorage().updateMessage(message, message.getMessageId());
    message = parseTypedMessage(message);
    final LCIMConversation conversation = client.getConversation(message.getConversationId());
    retrieveAllMessageHandlers(message, conversation, true, from);
  }

  private static void retrieveAllMessageHandlers(LCIMMessage message,
                                                 LCIMConversation conversation, boolean receipt, String operator) {
    boolean messageProcessed = false;
    for (Map.Entry<Class<? extends LCIMMessage>, Set<MessageHandler>> entry : messageHandlerRepository.entrySet()) {
      if (entry.getKey().isAssignableFrom(message.getClass())) {
        Set<MessageHandler> handlers = entry.getValue();
        if (handlers.size() > 0) {
          messageProcessed = true;
        }
        for (MessageHandler handler : handlers) {
          if (receipt) {
            handler.processEvent(Conversation.STATUS_ON_MESSAGE_RECEIPTED, operator, message,
                    conversation);
          } else {
            handler.processEvent(Conversation.STATUS_ON_MESSAGE, operator, message,
                    conversation);
          }
        }

      }
    }
    if (!messageProcessed && defaultMessageHandler != null) {
      if (receipt) {
        defaultMessageHandler.processEvent(Conversation.STATUS_ON_MESSAGE_RECEIPTED, operator, message,
                conversation);
      } else {
        defaultMessageHandler.processEvent(Conversation.STATUS_ON_MESSAGE, operator, message,
                conversation);
      }
    }
  }

  /**
   * 解析AVIMMessage对象的子类
   *
   * @param message message
   * @return Return the instance of AVIMTypedMessage
   */
  protected static LCIMMessage parseTypedMessage(LCIMMessage message) {
    int messageType = getMessageType(message.getContent());
    if (messageType != 0) {
      LCIMTypedMessage typedMessage = null;
      Class<? extends LCIMTypedMessage> clazz = messageTypesRepository.get(messageType);
      if (clazz != null) {
        try {
          typedMessage = clazz.newInstance();
        } catch (Exception e) {
          LOGGER.e("failed to create instance for TypedMessage: " + clazz.getCanonicalName()
                  + ", cause: " + e.getMessage());
        }
      } else {
        LOGGER.d("unknown message type: " + messageType);
        typedMessage = new LCIMTypedMessage(messageType);
      }
      if (null != typedMessage) {
        typedMessage.setConversationId(message.getConversationId());
        typedMessage.setFrom(message.getFrom());
        typedMessage.setDeliveredAt(message.getDeliveredAt());
        typedMessage.setTimestamp(message.getTimestamp());
        typedMessage.setContent(message.getContent());
        typedMessage.setMessageId(message.getMessageId());
        typedMessage.setMessageStatus(message.getMessageStatus());
        typedMessage.setMessageIOType(message.getMessageIOType());
        typedMessage.uniqueToken = message.uniqueToken;
        typedMessage.currentClient = message.currentClient;
        typedMessage.mentionAll = message.mentionAll;
        typedMessage.mentionList = message.mentionList;
        message = typedMessage;
      }
    }
    return message;
  }

  private static int getMessageType(String messageContent) {
    if (!StringUtil.isEmpty(messageContent)) {
      try {
        JSONObject object = JSON.parseObject(messageContent);
        int type = object.getInteger("_lctype");
        return type;
      } catch (Exception e) {
//        LOGGER.d("Parsing json data failed and use default messageType 0. cause: " + e.getMessage());
      }
    }
    return 0;
  }
}
