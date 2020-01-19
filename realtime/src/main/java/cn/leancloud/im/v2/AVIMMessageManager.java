package cn.leancloud.im.v2;

import cn.leancloud.AVLogger;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.v2.annotation.AVIMMessageType;
import cn.leancloud.im.v2.messages.*;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class AVIMMessageManager {
  protected static final AVLogger LOGGER = LogUtil.getLogger(AVIMMessageManager.class);

  static Map<Integer, Class<? extends AVIMTypedMessage>> messageTypesRepository =
          new HashMap<>();
  static AVIMMessageHandler defaultMessageHandler;
  static ConcurrentMap<Class<? extends AVIMMessage>, Set<MessageHandler>> messageHandlerRepository =
          new ConcurrentHashMap<Class<? extends AVIMMessage>, Set<MessageHandler>>();

  static AVIMConversationEventHandler conversationEventHandler;

  static {
    registerAVIMMessageType(AVIMTextMessage.class);
    registerAVIMMessageType(AVIMFileMessage.class);
    registerAVIMMessageType(AVIMImageMessage.class);
    registerAVIMMessageType(AVIMAudioMessage.class);
    registerAVIMMessageType(AVIMVideoMessage.class);
    registerAVIMMessageType(AVIMLocationMessage.class);
    registerAVIMMessageType(AVIMRecalledMessage.class);
  }

  /**
   * 注册自定义的消息类型
   *
   * @param messageType message type.
   */
  public static void registerAVIMMessageType(Class<? extends AVIMTypedMessage> messageType) {
    AVIMMessageType type = messageType.getAnnotation(AVIMMessageType.class);
    if (type == null) {
      throw new IncompleteAnnotationException(AVIMMessageType.class, "type");
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
  public static void registerDefaultMessageHandler(AVIMMessageHandler handler) {
    defaultMessageHandler = handler;
  }

  /**
   * 注册特定消息格式的处理单元
   *
   * @param clazz 特定的消息类
   * @param handler message handler.
   */
  public static void registerMessageHandler(Class<? extends AVIMMessage> clazz,
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
  public static void unregisterMessageHandler(Class<? extends AVIMMessage> clazz,
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
  public static void setConversationEventHandler(AVIMConversationEventHandler handler) {
    conversationEventHandler = handler;
  }

  protected static AVIMConversationEventHandler getConversationEventHandler() {
    return conversationEventHandler;
  }

  protected static void processMessage(AVIMMessage message, int convType, AVIMClient client, boolean hasMore,
                                       boolean isTransient) {
    // 如果已经通过拉获得了消息，则推的消息不回调
    if (client.getStorage().containMessage(message)) {
      return;
    }
    // hasMore 才设为 breakpoint
    // LogUtil.log.i("hasMore = " + hasMore);\
    if (!isTransient && AVIMOptions.getGlobalOptions().isMessageQueryCacheEnabled()) {
      client.getStorage().insertMessage(message, hasMore);
    }
    message = parseTypedMessage(message);
    final AVIMConversation conversation = client.getConversation(message.getConversationId(), convType);
    conversation.setLastMessage(message);
    if (!isTransient) {
      conversation.increaseUnreadCount(1, message.mentioned());
    }
    conversation.setLastMessageAt(new Date(message.getTimestamp()));

    retrieveAllMessageHandlers(message, conversation, false);
  }

  protected static void processMessageReceipt(AVIMMessage message, AVIMClient client) {
    client.getStorage().updateMessage(message, message.getMessageId());
    message = parseTypedMessage(message);
    final AVIMConversation conversation = client.getConversation(message.getConversationId());
    retrieveAllMessageHandlers(message, conversation, true);
  }

  private static void retrieveAllMessageHandlers(AVIMMessage message,
                                                 AVIMConversation conversation, boolean receipt) {
    boolean messageProcessed = false;
    for (Map.Entry<Class<? extends AVIMMessage>, Set<MessageHandler>> entry : messageHandlerRepository.entrySet()) {
      if (entry.getKey().isAssignableFrom(message.getClass())) {
        Set<MessageHandler> handlers = entry.getValue();
        if (handlers.size() > 0) {
          messageProcessed = true;
        }
        for (MessageHandler handler : handlers) {
          if (receipt) {
            handler.processEvent(Conversation.STATUS_ON_MESSAGE_RECEIPTED, null, message,
                    conversation);
          } else {
            handler.processEvent(Conversation.STATUS_ON_MESSAGE, null, message,
                    conversation);
          }
        }

      }
    }
    if (!messageProcessed && defaultMessageHandler != null) {
      if (receipt) {
        defaultMessageHandler.processEvent(Conversation.STATUS_ON_MESSAGE_RECEIPTED, null, message,
                conversation);
      } else {
        defaultMessageHandler.processEvent(Conversation.STATUS_ON_MESSAGE, null, message,
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
  protected static AVIMMessage parseTypedMessage(AVIMMessage message) {
    int messageType = getMessageType(message.getContent());
    if (messageType != 0) {
      Class<? extends AVIMTypedMessage> clazz = messageTypesRepository.get(messageType);
      if (clazz != null) {
        try {
          AVIMMessage typedMessage = clazz.newInstance();
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
        } catch (Exception e) {
        }
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
        LOGGER.d("Parsing json data failed and use default messageType 0. cause: " + e.getMessage());
      }
    }
    return 0;
  }
}
