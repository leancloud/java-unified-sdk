package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.push.AVNotificationManager;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.session.AVConversationHolder;
import cn.leancloud.session.AVSession;
import cn.leancloud.session.AVSessionManager;
import cn.leancloud.utils.LogUtil;
import com.alibaba.fastjson.JSON;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class DirectlyOperationTube implements OperationTube {
  private static final AVLogger LOGGER = LogUtil.getLogger(DirectlyOperationTube.class);

  private final boolean needCacheRequestKey;
  public DirectlyOperationTube(boolean needCacheRequestKey) {
    this.needCacheRequestKey = needCacheRequestKey;
  }

  public boolean openClient(String clientId, String tag, String userSessionToken,
                  boolean reConnect, AVIMClientCallback callback) {
    LOGGER.d("openClient...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return openClientDirectly(clientId, tag, userSessionToken, reConnect, requestId);
  }

  public boolean queryClientStatus(String clientId, final AVIMClientStatusCallback callback) {
    LOGGER.d("queryClientStatus...");

    AVIMClient.AVIMClientStatus status = AVIMClient.AVIMClientStatus.AVIMClientStatusNone;
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    if (AVSession.Status.Opened == session.getCurrentStatus()) {
      status = AVIMClient.AVIMClientStatus.AVIMClientStatusOpened;
    } else {
      status = AVIMClient.AVIMClientStatus.AVIMClientStatusPaused;
    }
    if (null != callback) {
      callback.internalDone(status, null);
    }
    return true;
  }

  public boolean renewSessionToken(String clientId, AVIMClientCallback callback) {
    LOGGER.d("renewSessionToken...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return renewSessionTokenDirectly(clientId, requestId);
  }

  public boolean closeClient(String self, AVIMClientCallback callback) {
    LOGGER.d("closeClient...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    return closeClientDirectly(self, requestId);
  }

  public boolean queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback) {
    LOGGER.d("queryOnlineClients...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    return queryOnlineClientsDirectly(self, clients, requestId);
  }

  public boolean createConversation(final String self, final List<String> memberList,
                          final Map<String, Object> attribute, final boolean isTransient, final boolean isUnique,
                          final boolean isTemp, int tempTTL, final AVIMCommonJsonCallback callback) {
    LOGGER.d("createConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    return createConversationDirectly(self, memberList, attribute, isTransient, isUnique, isTemp, tempTTL, requestId);
  }

  public boolean updateConversation(final String clientId, String conversationId, int convType,
                                    final Map<String, Object> param, final AVIMCommonJsonCallback callback) {
    LOGGER.d("updateConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.updateConversationDirectly(clientId, conversationId, convType, param, requestId);
  }

  public boolean participateConversation(final String clientId, String conversationId, int convType, final Map<String, Object> param,
                                         Conversation.AVIMOperation operation, final AVIMConversationCallback callback) {
    LOGGER.d("participateConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.participateConversationDirectly(clientId, conversationId, convType, param, operation, requestId);
  }

  public boolean queryConversations(final String clientId, final String queryString, final AVIMCommonJsonCallback callback) {
    return queryConversationsInternally(clientId, queryString, callback);
  }

  public boolean queryConversationsInternally(final String clientId, final String queryString, final AVIMCommonJsonCallback callback) {
    LOGGER.d("queryConversationsInternally...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.queryConversationsDirectly(clientId, queryString, requestId);
  }

  public boolean sendMessage(String clientId, String conversationId, int convType, final AVIMMessage message,
                             final AVIMMessageOption messageOption, final AVIMCommonJsonCallback callback) {
    LOGGER.d("sendMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.sendMessageDirectly(clientId, conversationId, convType, message, messageOption, requestId);
  }

  public boolean updateMessage(String clientId, int convType, AVIMMessage oldMessage, AVIMMessage newMessage, AVIMCommonJsonCallback callback) {
    LOGGER.d("updateMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.updateMessageDirectly(clientId, convType, oldMessage, newMessage, requestId);
  }

  public boolean recallMessage(String clientId, int convType, AVIMMessage message, AVIMCommonJsonCallback callback) {
    LOGGER.d("recallMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.recallMessageDirectly(clientId, convType, message, requestId);
  }

  public boolean fetchReceiptTimestamps(String clientId, String conversationId, int convType,
                                        Conversation.AVIMOperation operation, AVIMCommonJsonCallback callback) {
    LOGGER.d("fetchReceiptTimestamps...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.fetchReceiptTimestampsDirectly(clientId, conversationId, convType, operation, requestId);
  }

  public boolean processMembers(String clientId, String conversationId, int convType, String params, Conversation.AVIMOperation op,
                                AVCallback callback) {
    LOGGER.d("processMembers...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.processMembersDirectly(clientId, conversationId, convType, params, op, requestId);
  }

  public boolean queryMessages(String clientId, String conversationId, int convType, String params,
                        Conversation.AVIMOperation operation, AVIMMessagesQueryCallback callback) {
    LOGGER.d("queryMessages...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.queryMessagesDirectly(clientId, conversationId, convType, params, operation, requestId);
  }

  public boolean markConversationRead(String clientId, String conversationId, int convType, Map<String, Object> lastMessageParam) {
    LOGGER.d("markConversationRead...");
    int requestId = WindTalker.getNextIMRequestId();
    return this.markConversationReadDirectly(clientId, conversationId, convType, lastMessageParam, requestId);
  }

  public boolean openClientDirectly(String clientId, String tag, String userSessionToken,
                             boolean reConnect, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    session.open(tag, userSessionToken, reConnect, requestId);
    return true;
  }

  public boolean closeClientDirectly(String self, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(self);
    session.close(requestId);
    return true;
  }
  public boolean renewSessionTokenDirectly(String clientId, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    session.renewRealtimeSesionToken(requestId);
    return true;
  }
  public boolean queryOnlineClientsDirectly(String self, List<String> clients, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(self);
    session.queryOnlinePeers(clients, requestId);
    return true;
  }

  public boolean createConversationDirectly(final String self, final List<String> members,
                                     final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                     final boolean isTemp, int tempTTL, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(self);
    session.createConversation(members, attributes, isTransient, isUnique, isTemp, tempTTL, false, requestId);
    return true;
  }

  public boolean queryConversationsDirectly(final String clientId, final String queryString, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    session.queryConversations(JSON.parseObject(queryString, Map.class), requestId);
    return true;
  }

  public boolean updateConversationDirectly(final String clientId, String conversationId, int convType,
                                            final Map<String, Object> param, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.updateInfo(param, requestId);
    return true;
  }

  public boolean participateConversationDirectly(final String clientId, String conversationId, int convType,
                                                 final Map<String, Object> param,
                                                 Conversation.AVIMOperation operation, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.processConversationCommandFromClient(operation, param, requestId);
    return true;
  }

  public boolean sendMessageDirectly(String clientId, String conversationId, int convType, final AVIMMessage message,
                              final AVIMMessageOption messageOption, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    message.setFrom(clientId);
    holder.sendMessage(message, requestId, null == messageOption? new AVIMMessageOption() : messageOption);
    return true;
  }
  public boolean updateMessageDirectly(String clientId, int convType, AVIMMessage oldMessage, AVIMMessage newMessage,
                                int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(oldMessage.getConversationId(), convType);
    holder.patchMessage(oldMessage, newMessage, null, Conversation.AVIMOperation.CONVERSATION_UPDATE_MESSAGE,
            requestId);
    return true;
  }
  public boolean recallMessageDirectly(String clientId, int convType, AVIMMessage message, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(message.getConversationId(), convType);
    holder.patchMessage(null, null, message, Conversation.AVIMOperation.CONVERSATION_RECALL_MESSAGE,
            requestId);
    return true;
  }
  public boolean fetchReceiptTimestampsDirectly(String clientId, String conversationId, int convType, Conversation.AVIMOperation operation,
                                         int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.getReceiptTime(requestId);
    return true;
  }
  public boolean queryMessagesDirectly(String clientId, String conversationId, int convType, String params,
                                Conversation.AVIMOperation operation, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    Map<String, Object> queryParam = JSON.parseObject(params, Map.class);
    holder.processConversationCommandFromClient(operation, queryParam, requestId);
    return true;
  }

  public boolean processMembersDirectly(String clientId, String conversationId, int convType, String params, Conversation.AVIMOperation op,
                                int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.processConversationCommandFromClient(op, JSON.parseObject(params, Map.class), requestId);
    return true;
  }

  public boolean markConversationReadDirectly(String clientId, String conversationId, int convType,
                                              Map<String, Object> lastMessageParam, int requestId) {
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.processConversationCommandFromClient(Conversation.AVIMOperation.CONVERSATION_READ, lastMessageParam, requestId);
    return true;
  }

  private AVCallback getCachedCallback(final String clientId, final String conversationId, int requestId,
                                       Conversation.AVIMOperation operation) {
    return RequestCache.getInstance().getRequestCallback(clientId, null, requestId);
  }

  public void onOperationCompleted(String clientId, String conversationId, int requestId,
                                   Conversation.AVIMOperation operation, Throwable throwable) {
    LOGGER.d("enter onOperationCompleted with clientId=" + clientId + ", convId=" + conversationId + ", requestId="
      + requestId + ", operation=" + operation);
    AVCallback callback = getCachedCallback(clientId, conversationId, requestId, operation);
    if (null == callback) {
      LOGGER.w("encounter illegal response, ignore it: clientId=" + clientId + ", convId=" + conversationId + ", requestId=" + requestId);
      return;
    }
    switch (operation) {
      case CLIENT_OPEN:
      case CLIENT_DISCONNECT:
      case CLIENT_REFRESH_TOKEN:
        callback.internalDone(AVIMClient.getInstance(clientId), AVIMException.wrapperAVException(throwable));
        break;
      default:
        callback.internalDone(AVIMException.wrapperAVException(throwable));
        break;
    }
    RequestCache.getInstance().cleanRequestCallback(clientId, conversationId, requestId);
  }

  public void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                                     Conversation.AVIMOperation operation, Map<String, Object> resultData) {
    LOGGER.d("enter onOperationCompletedEx with clientId=" + clientId + ", convId=" + conversationId + ", requestId="
            + requestId + ", operation=" + operation + ", resultData=" + resultData.toString());
    AVCallback callback = getCachedCallback(clientId, conversationId, requestId, operation);
    if (null == callback) {
      LOGGER.w("encounter illegal response, ignore it: clientId=" + clientId + ", convId=" + conversationId
              + ", requestId=" + requestId);
      return;
    }
    switch (operation) {
      case CLIENT_ONLINE_QUERY:
        callback.internalDone((List<String>)resultData.get(Conversation.callbackOnlineClients), null);
        break;
      case CONVERSATION_UPDATE_MESSAGE:
      case CONVERSATION_RECALL_MESSAGE:
      case CONVERSATION_CREATION:
      case CONVERSATION_SEND_MESSAGE:
      case CONVERSATION_QUERY:
      case CONVERSATION_MUTED_MEMBER_QUERY:
      case CONVERSATION_BLOCKED_MEMBER_QUERY:
        callback.internalDone(resultData, null);
        break;
      case CONVERSATION_MESSAGE_QUERY:
        callback.internalDone(resultData.get(Conversation.callbackHistoryMessages), null);
        break;
      default:
        callback.internalDone(resultData, null);
        break;
    }
  }
  public void onMessageArrived(String clientId, String conversationId, int requestId,
                               Conversation.AVIMOperation operation, Messages.GenericCommand command) {
    ;
  }
  public void onLiveQueryCompleted(int requestId, Throwable throwable) {
    ;
  }

  public void onPushMessage(String message, String messageId) {
    AVNotificationManager.getInstance().processPushMessage(message, messageId);
  }
}
