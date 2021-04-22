package cn.leancloud.im;

import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMMessage;
import cn.leancloud.im.v2.LCIMMessageOption;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.livequery.LCLiveQuerySubscribeCallback;
import cn.leancloud.session.LCConnectionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OperationTube {
  // request sender
  boolean openClient(LCConnectionManager connectionManager, String clientId, String tag, String userSessionToken,
                     boolean reConnect, final LCIMClientCallback callback);
  boolean queryClientStatus(LCConnectionManager connectionManager, String clientId, final LCIMClientStatusCallback callback);
  boolean closeClient(LCConnectionManager connectionManager, String self, final LCIMClientCallback callback);
  boolean renewSessionToken(LCConnectionManager connectionManager, String clientId, final LCIMClientCallback callback);
  boolean queryOnlineClients(LCConnectionManager connectionManager, String self, List<String> clients, final LCIMOnlineClientsCallback callback);

  boolean createConversation(LCConnectionManager connectionManager, final String self, final List<String> members,
                             final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                             final boolean isTemp, int tempTTL, final LCIMCommonJsonCallback callback);

  boolean updateConversation(LCConnectionManager connectionManager, final String clientId, String conversationId, int convType, final Map<String, Object> param,
                             final LCIMCommonJsonCallback callback);

  boolean participateConversation(LCConnectionManager connectionManager, final String clientId, String conversationId, int convType, final Map<String, Object> param,
                                  Conversation.AVIMOperation operation, final LCIMConversationCallback callback);

  boolean queryConversations(LCConnectionManager connectionManager, final String clientId, final String queryString, final LCIMCommonJsonCallback callback);
  boolean queryConversationsInternally(LCConnectionManager connectionManager, final String clientId, final String queryString, final LCIMCommonJsonCallback callback);

  boolean sendMessage(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, final LCIMMessage message, final LCIMMessageOption messageOption,
                      final LCIMCommonJsonCallback callback);
  boolean updateMessage(LCConnectionManager connectionManager, String clientId, int convType, LCIMMessage oldMessage, LCIMMessage newMessage,
                        final LCIMCommonJsonCallback callback);
  boolean recallMessage(LCConnectionManager connectionManager, String clientId, int convType, LCIMMessage message, final LCIMCommonJsonCallback callback);
  boolean fetchReceiptTimestamps(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, Conversation.AVIMOperation operation,
                                 final LCIMCommonJsonCallback callback);
  boolean queryMessages(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, String params,
                        Conversation.AVIMOperation operation, final LCIMMessagesQueryCallback callback);

  boolean processMembers(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, String params, Conversation.AVIMOperation op,
                         final LCCallback callback);

  boolean markConversationRead(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, Map<String, Object> lastMessageParam);

  boolean loginLiveQuery(LCConnectionManager connectionManager, String subscriptionId, final LCLiveQuerySubscribeCallback callback);
  
  // response notifier
  void onOperationCompleted(String clientId, String conversationId, int requestId,
                            Conversation.AVIMOperation operation, Throwable throwable);
  void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                              Conversation.AVIMOperation operation, HashMap<String, Object> resultData);
  void onLiveQueryCompleted(int requestId, Throwable throwable);
}
