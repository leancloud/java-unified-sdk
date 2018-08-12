package cn.leancloud.im;

import cn.leancloud.Messages;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.livequery.AVLiveQuerySubscribeCallback;

import java.util.List;
import java.util.Map;

public interface OperationTube {
  // request sender
  boolean openClient(String clientId, String tag, String userSessionToken,
                  boolean reConnect, AVIMClientCallback callback);
  boolean queryClientStatus(String clientId, final AVIMClientStatusCallback callback);
  boolean closeClient(String self, AVIMClientCallback callback);
  boolean renewSessionToken(String clientId, AVIMClientCallback callback);
  boolean queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback);

  boolean createConversation(final String self, final List<String> members,
                             final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                             final boolean isTemp, int tempTTL, final AVIMCommonJsonCallback callback);

  boolean updateConversation(final String clientId, String conversationId, int convType, final Map<String, Object> param,
                             final AVIMCommonJsonCallback callback);

  boolean participateConversation(final String clientId, String conversationId, int convType, final Map<String, Object> param,
                                  Conversation.AVIMOperation operation, final AVIMConversationCallback callback);

  boolean queryConversations(final String clientId, final String queryString, final AVIMCommonJsonCallback callback);
  boolean queryConversationsInternally(final String clientId, final String queryString, final AVIMCommonJsonCallback callback);

  boolean sendMessage(String clientId, String conversationId, int convType, final AVIMMessage message, final AVIMMessageOption messageOption,
                      final AVIMCommonJsonCallback callback);
  boolean updateMessage(String clientId, int convType, AVIMMessage oldMessage, AVIMMessage newMessage,
                        AVIMCommonJsonCallback callback);
  boolean recallMessage(String clientId, int convType, AVIMMessage message, AVIMCommonJsonCallback callback);
  boolean fetchReceiptTimestamps(String clientId, String conversationId, int convType, Conversation.AVIMOperation operation,
                                 AVIMCommonJsonCallback callback);
  boolean queryMessages(String clientId, String conversationId, int convType, String params,
                        Conversation.AVIMOperation operation, AVIMMessagesQueryCallback callback);

  boolean processMembers(String clientId, String conversationId, int convType, String params, Conversation.AVIMOperation op,
                         AVCallback callback);

  boolean markConversationRead(String clientId, String conversationId, int convType, Map<String, Object> lastMessageParam);

  boolean loginLiveQuery(String subscriptionId, AVLiveQuerySubscribeCallback callback);
  
  // response notifier
  void onOperationCompleted(String clientId, String conversationId, int requestId,
                            Conversation.AVIMOperation operation, Throwable throwable);
  void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                              Conversation.AVIMOperation operation, Map<String, Object> resultData);
//  void onMessageArrived(String clientId, String conversationId, int requestId,
//                        Conversation.AVIMOperation operation, Messages.GenericCommand command);
  void onLiveQueryCompleted(int requestId, Throwable throwable);
  void onPushMessage(String message, String messageId);
}
