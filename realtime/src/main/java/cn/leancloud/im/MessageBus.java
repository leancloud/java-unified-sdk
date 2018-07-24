package cn.leancloud.im;

import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.callback.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageBus {
  private static final MessageBus gInstance = new MessageBus();
  private MessageBus() {}

  public static MessageBus getInstance() {
    return MessageBus.gInstance;
  }

  public void openClient(String clientId, String tag, String userSessionToken,
                         boolean reConnect, AVIMClientCallback callback) {
    CommandCourier carrier = InternalConfiguration.getCommandCourier();
    if (null != carrier) {
      carrier.openClient(clientId, tag, userSessionToken, reConnect, callback);
    }
  }

  public void queryClientStatus(String clientId, final AVIMClientStatusCallback callback) {
    CommandCourier carrier = InternalConfiguration.getCommandCourier();
    if (null != carrier) {
      carrier.queryClientStatus(clientId, callback);
    }
  }

  public void closeClient(String self, AVIMClientCallback callback) {
    CommandCourier carrier = InternalConfiguration.getCommandCourier();
    if (null != carrier) {
      carrier.closeClient(self, callback);
    }
  }

  public void queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback) {
    CommandCourier carrier = InternalConfiguration.getCommandCourier();
    if (null != carrier) {
      carrier.queryOnlineClients(self, clients, callback);
    }
  }

  public void createConversation(final List<String> members, final String name,
                                 final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                 final boolean isTemp, int tempTTL, final AVIMConversationCreatedCallback callback) {
    CommandCourier carrier = InternalConfiguration.getCommandCourier();
    if (null != carrier) {
      carrier.createConversation(members, name, attributes, isTransient, isUnique, isTemp, tempTTL, callback);
    }
  }

  public void sendMessage(final AVIMMessage message, final AVIMMessageOption messageOption, final AVIMConversationCallback callback) {
    CommandCourier carrier = InternalConfiguration.getCommandCourier();
    if (null != carrier) {
      carrier.sendMessage(message, messageOption, callback);
    }
  }

  public void updateMessage(AVIMMessage oldMessage, AVIMMessage newMessage, AVIMMessageUpdatedCallback callback) {
    CommandCourier carrier = InternalConfiguration.getCommandCourier();
    if (null != carrier) {
      carrier.updateMessage(oldMessage, newMessage, callback);
    }
  }

  public void recallMessage(AVIMMessage message, AVIMMessageRecalledCallback callback) {
    CommandCourier carrier = InternalConfiguration.getCommandCourier();
    if (null != carrier) {
      carrier.recallMessage(message, callback);
    }
  }
  public boolean fetchReceiptTimestamps(String clientId, String conversationId, final Conversation.AVIMOperation operation, AVIMConversationCallback callback) {
    return false;
  }

  public boolean queryMessages(String clientId, String conversationId, int conversationType, String query,
                               Conversation.AVIMOperation operation, AVIMMessagesQueryCallback callback) {
    return false;
  }
}
