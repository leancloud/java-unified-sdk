package cn.leancloud.im;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.codec.MDFive;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.livequery.AVLiveQuerySubscribeCallback;
import cn.leancloud.livequery.LiveQueryOperationDelegate;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.session.AVConversationHolder;
import cn.leancloud.session.AVSession;
import cn.leancloud.session.AVSessionManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectlyOperationTube implements OperationTube {
  private static final AVLogger LOGGER = LogUtil.getLogger(DirectlyOperationTube.class);

  private final boolean needCacheRequestKey;
  public DirectlyOperationTube(boolean needCacheRequestKey) {
    this.needCacheRequestKey = needCacheRequestKey;
  }

  public boolean openClient(AVConnectionManager connectionManager, String clientId, String tag, String userSessionToken,
                            boolean reConnect, final AVIMClientCallback callback) {
    LOGGER.d("openClient...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return openClientDirectly(connectionManager, clientId, tag, userSessionToken, reConnect, requestId);
  }

  public boolean queryClientStatus(AVConnectionManager connectionManager, String clientId, final AVIMClientStatusCallback callback) {
    LOGGER.d("queryClientStatus...");

    String installationId = getInstallationId(clientId);
    AVIMClient.AVIMClientStatus status = AVIMClient.AVIMClientStatus.AVIMClientStatusNone;
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
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

  public boolean renewSessionToken(AVConnectionManager connectionManager, String clientId, final AVIMClientCallback callback) {
    LOGGER.d("renewSessionToken...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return renewSessionTokenDirectly(connectionManager, clientId, requestId);
  }

  public boolean closeClient(AVConnectionManager connectionManager, String self, final AVIMClientCallback callback) {
    LOGGER.d("closeClient...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    return closeClientDirectly(connectionManager, self, requestId);
  }

  public boolean queryOnlineClients(AVConnectionManager connectionManager, String self, List<String> clients, final AVIMOnlineClientsCallback callback) {
    LOGGER.d("queryOnlineClients...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    return queryOnlineClientsDirectly(connectionManager, self, clients, requestId);
  }

  public boolean createConversation(AVConnectionManager connectionManager, final String self, final List<String> memberList,
                          final Map<String, Object> attribute, final boolean isTransient, final boolean isUnique,
                          final boolean isTemp, int tempTTL, final AVIMCommonJsonCallback callback) {
    LOGGER.d("createConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    return createConversationDirectly(connectionManager, self, memberList, attribute, isTransient, isUnique, isTemp, tempTTL, requestId);
  }

  public boolean updateConversation(AVConnectionManager connectionManager, final String clientId, String conversationId, int convType,
                                    final Map<String, Object> param, final AVIMCommonJsonCallback callback) {
    LOGGER.d("updateConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.updateConversationDirectly(connectionManager, clientId, conversationId, convType, param, requestId);
  }

  public boolean participateConversation(AVConnectionManager connectionManager, final String clientId, String conversationId, int convType, final Map<String, Object> param,
                                         Conversation.AVIMOperation operation, final AVIMConversationCallback callback) {
    LOGGER.d("participateConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.participateConversationDirectly(connectionManager, clientId, conversationId, convType, param, operation, requestId);
  }

  public boolean queryConversations(AVConnectionManager connectionManager, final String clientId, final String queryString, final AVIMCommonJsonCallback callback) {
    return queryConversationsInternally(connectionManager, clientId, queryString, callback);
  }

  public boolean queryConversationsInternally(AVConnectionManager connectionManager, final String clientId, final String queryString, final AVIMCommonJsonCallback callback) {
    LOGGER.d("queryConversationsInternally...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.queryConversationsDirectly(connectionManager, clientId, queryString, requestId);
  }

  public boolean sendMessage(AVConnectionManager connectionManager, String clientId, String conversationId, int convType, final AVIMMessage message,
                             final AVIMMessageOption messageOption, final AVIMCommonJsonCallback callback) {
    LOGGER.d("sendMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.sendMessageDirectly(connectionManager, clientId, conversationId, convType, message, messageOption, requestId);
  }

  public boolean updateMessage(AVConnectionManager connectionManager, String clientId, int convType, AVIMMessage oldMessage, AVIMMessage newMessage,
                               final AVIMCommonJsonCallback callback) {
    LOGGER.d("updateMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.updateMessageDirectly(connectionManager, clientId, convType, oldMessage, newMessage, requestId);
  }

  public boolean recallMessage(AVConnectionManager connectionManager, String clientId, int convType, AVIMMessage message, final AVIMCommonJsonCallback callback) {
    LOGGER.d("recallMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.recallMessageDirectly(connectionManager, clientId, convType, message, requestId);
  }

  public boolean fetchReceiptTimestamps(AVConnectionManager connectionManager, String clientId, String conversationId, int convType,
                                        Conversation.AVIMOperation operation, final AVIMCommonJsonCallback callback) {
    LOGGER.d("fetchReceiptTimestamps...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.fetchReceiptTimestampsDirectly(connectionManager, clientId, conversationId, convType, operation, requestId);
  }

  public boolean processMembers(AVConnectionManager connectionManager, String clientId, String conversationId, int convType, String params, Conversation.AVIMOperation op,
                                final AVCallback callback) {
    LOGGER.d("processMembers...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.processMembersDirectly(connectionManager, clientId, conversationId, convType, params, op, requestId);
  }

  public boolean queryMessages(AVConnectionManager connectionManager, String clientId, String conversationId, int convType, String params,
                        Conversation.AVIMOperation operation, final AVIMMessagesQueryCallback callback) {
    LOGGER.d("queryMessages...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.queryMessagesDirectly(connectionManager, clientId, conversationId, convType, params, operation, requestId);
  }

  public boolean markConversationRead(AVConnectionManager connectionManager, String clientId, String conversationId, int convType, Map<String, Object> lastMessageParam) {
    LOGGER.d("markConversationRead...");
    int requestId = WindTalker.getNextIMRequestId();
    return this.markConversationReadDirectly(connectionManager, clientId, conversationId, convType, lastMessageParam, requestId);
  }

  public boolean loginLiveQuery(AVConnectionManager connectionManager, String subscriptionId, final AVLiveQuerySubscribeCallback callback) {
    LOGGER.d("loginLiveQuery...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID, null, requestId, callback);
    } else {
      LOGGER.d("don't cache livequery login request.");
    }
    return loginLiveQueryDirectly(connectionManager, subscriptionId, requestId);
  }

  public boolean loginLiveQueryDirectly(AVConnectionManager connectionManager, String subscriptionId, int requestId) {
    if (StringUtil.isEmpty(subscriptionId)) {
      return false;
    }
    LiveQueryOperationDelegate.getInstance().login(subscriptionId, requestId);
    return true;
  }

  private String getInstallationId(String clientId) {
    AVIMClient client = AVIMClient.peekInstance(clientId);
    if (null != client) {
      return client.getInstallationId();
    }
    return null;
  }

  public boolean openClientDirectly(AVConnectionManager connectionManager, String clientId, String tag, String userSessionToken,
                             boolean reConnect, int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    session.open(tag, userSessionToken, reConnect, requestId);
    return true;
  }

  public boolean closeClientDirectly(AVConnectionManager connectionManager, String self, int requestId) {
    String installationId = getInstallationId(self);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(self, installationId, connectionManager);
    session.close(requestId);
    return true;
  }
  public boolean renewSessionTokenDirectly(AVConnectionManager connectionManager, String clientId, int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    session.renewRealtimeSesionToken(requestId);
    return true;
  }
  public boolean queryOnlineClientsDirectly(AVConnectionManager connectionManager, String self, List<String> clients, int requestId) {
    String installationId = getInstallationId(self);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(self, installationId, connectionManager);
    session.queryOnlinePeers(clients, requestId);
    return true;
  }

  public boolean createConversationDirectly(AVConnectionManager connectionManager, final String self, final List<String> members,
                                     final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                     final boolean isTemp, int tempTTL, int requestId) {
    String installationId = getInstallationId(self);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(self, installationId, connectionManager);
    session.createConversation(members, attributes, isTransient, isUnique, isTemp, tempTTL, false, requestId);
    return true;
  }

  public boolean queryConversationsDirectly(AVConnectionManager connectionManager, final String clientId,
                                            final String queryString, int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    Map<String, Object> map = new HashMap<>();
    session.queryConversations(JSON.parseObject(queryString, map.getClass()), requestId, MDFive.computeMD5(queryString));
    return true;
  }

  public boolean updateConversationDirectly(AVConnectionManager connectionManager, final String clientId,
                                            String conversationId, int convType,
                                            final Map<String, Object> param, int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.updateInfo(param, requestId);
    return true;
  }

  public boolean participateConversationDirectly(AVConnectionManager connectionManager, final String clientId,
                                                 String conversationId, int convType,
                                                 final Map<String, Object> param,
                                                 Conversation.AVIMOperation operation, int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.processConversationCommandFromClient(operation, param, requestId);
    return true;
  }

  public boolean sendMessageDirectly(AVConnectionManager connectionManager, String clientId, String conversationId,
                                     int convType, final AVIMMessage message,
                              final AVIMMessageOption messageOption, int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    message.setFrom(clientId);
    holder.sendMessage(message, requestId, null == messageOption? new AVIMMessageOption() : messageOption);
    return true;
  }
  public boolean updateMessageDirectly(AVConnectionManager connectionManager, String clientId, int convType,
                                       AVIMMessage oldMessage, AVIMMessage newMessage,
                                int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    AVConversationHolder holder = session.getConversationHolder(oldMessage.getConversationId(), convType);
    holder.patchMessage(oldMessage, newMessage, null, Conversation.AVIMOperation.CONVERSATION_UPDATE_MESSAGE,
            requestId);
    return true;
  }
  public boolean recallMessageDirectly(AVConnectionManager connectionManager, String clientId, int convType,
                                       AVIMMessage message, int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    AVConversationHolder holder = session.getConversationHolder(message.getConversationId(), convType);
    holder.patchMessage(null, null, message, Conversation.AVIMOperation.CONVERSATION_RECALL_MESSAGE,
            requestId);
    return true;
  }
  public boolean fetchReceiptTimestampsDirectly(AVConnectionManager connectionManager, String clientId,
                                                String conversationId, int convType, Conversation.AVIMOperation operation,
                                         int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.getReceiptTime(requestId);
    return true;
  }
  public boolean queryMessagesDirectly(AVConnectionManager connectionManager, String clientId, String conversationId,
                                       int convType, String params,
                                Conversation.AVIMOperation operation, int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    Map<String, Object> queryParam = JSON.parseObject(params, Map.class);
    holder.processConversationCommandFromClient(operation, queryParam, requestId);
    return true;
  }

  public boolean processMembersDirectly(AVConnectionManager connectionManager, String clientId, String conversationId,
                                        int convType, String params, Conversation.AVIMOperation op,
                                int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.processConversationCommandFromClient(op, JSON.parseObject(params, Map.class), requestId);
    return true;
  }

  public boolean markConversationReadDirectly(AVConnectionManager connectionManager, String clientId,
                                              String conversationId, int convType,
                                              Map<String, Object> lastMessageParam, int requestId) {
    String installationId = getInstallationId(clientId);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
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

    AVCallback callback = getCachedCallback(clientId, conversationId, requestId, operation);
    if (null == callback) {
      LOGGER.w("onOperationCompleted encounter illegal response, ignore it: clientId=" + clientId
              + ", convId=" + conversationId + ", requestId=" + requestId + ", operation=" + operation);
      return;
    }
    LOGGER.d("enter onOperationCompleted with clientId=" + clientId + ", convId=" + conversationId + ", requestId="
            + requestId + ", operation=" + operation);
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
                                     Conversation.AVIMOperation operation, HashMap<String, Object> resultData) {
    AVCallback callback = getCachedCallback(clientId, conversationId, requestId, operation);
    if (null == callback) {
      LOGGER.w("onOperationCompletedEx encounter illegal response, ignore it: clientId=" + clientId + ", convId="
              + conversationId + ", requestId=" + requestId + ", operation=" + operation + ", resultData=" + resultData.toString());
      return;
    }
    LOGGER.d("enter onOperationCompletedEx with clientId=" + clientId + ", convId=" + conversationId + ", requestId="
            + requestId + ", operation=" + operation + ", resultData=" + resultData.toString());
    switch (operation) {
      case CLIENT_ONLINE_QUERY:
        callback.internalDone((List<String>)resultData.get(Conversation.callbackOnlineClients), null);
        break;
      case CONVERSATION_UPDATE_MESSAGE:
      case CONVERSATION_RECALL_MESSAGE:
      case CONVERSATION_CREATION:
      case CONVERSATION_SEND_MESSAGE:
      case CONVERSATION_QUERY:
        // wrapper callback would parse the HashMap result correctly.
        callback.internalDone(resultData, null);
        break;
      case CONVERSATION_MUTED_MEMBER_QUERY:
      case CONVERSATION_BLOCKED_MEMBER_QUERY:
        String[] result = (String[])resultData.get(Conversation.callbackData);
        String next = resultData.containsKey(Conversation.callbackIterableNext)?
                (String) resultData.get(Conversation.callbackIterableNext) : null;
        if (callback instanceof AVIMConversationIterableResultCallback) {
          AVIMConversationIterableResult iterableResult = new AVIMConversationIterableResult();
          iterableResult.setNext(next);
          iterableResult.setMembers(Arrays.asList(result));
          callback.internalDone(iterableResult, null);
        } else {
          callback.internalDone(Arrays.asList(result), null);
        }
        break;
      case CONVERSATION_MESSAGE_QUERY:
        callback.internalDone(resultData.get(Conversation.callbackHistoryMessages), null);
        break;
      case CONVERSATION_MEMBER_COUNT_QUERY:
        callback.internalDone(resultData.get(Conversation.callbackMemberCount), null);
        break;
      default:
        callback.internalDone(resultData, null);
        break;
    }
    RequestCache.getInstance().cleanRequestCallback(clientId, conversationId, requestId);
  }

  public void onLiveQueryCompleted(int requestId, Throwable throwable) {
    AVCallback callback = getCachedCallback(LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID, null, requestId, null);
    if (null != callback) {
      LOGGER.d("call livequery login callback with exception:" + throwable);
      callback.internalDone(null == throwable? null : new AVException(throwable));
    } else {
      LOGGER.d("no callback found for livequery login request.");
    }
    RequestCache.getInstance().cleanRequestCallback(LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID, null, requestId);
  }
}
