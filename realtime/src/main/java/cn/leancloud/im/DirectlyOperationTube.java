package cn.leancloud.im;

import cn.leancloud.LCException;
import cn.leancloud.LCLogger;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.codec.MDFive;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.livequery.LCLiveQuerySubscribeCallback;
import cn.leancloud.livequery.LiveQueryOperationDelegate;
import cn.leancloud.session.LCConnectionManager;
import cn.leancloud.session.LCConversationHolder;
import cn.leancloud.session.LCSession;
import cn.leancloud.session.LCSessionManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectlyOperationTube implements OperationTube {
  private static final LCLogger LOGGER = LogUtil.getLogger(DirectlyOperationTube.class);

  private final boolean needCacheRequestKey;
  public DirectlyOperationTube(boolean needCacheRequestKey) {
    this.needCacheRequestKey = needCacheRequestKey;
  }

  public boolean openClient(LCConnectionManager connectionManager, String clientId, String tag, String userSessionToken,
                            boolean reConnect, final LCIMClientCallback callback) {
    LOGGER.d("openClient...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return openClientDirectly(connectionManager, clientId, tag, userSessionToken, reConnect, requestId);
  }

  public boolean queryClientStatus(LCConnectionManager connectionManager, String clientId, final LCIMClientStatusCallback callback) {
    LOGGER.d("queryClientStatus...");

    String installationId = getInstallationId(clientId);
    LCIMClient.AVIMClientStatus status = LCIMClient.AVIMClientStatus.AVIMClientStatusNone;
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    if (LCSession.Status.Opened == session.getCurrentStatus()) {
      status = LCIMClient.AVIMClientStatus.AVIMClientStatusOpened;
    } else {
      status = LCIMClient.AVIMClientStatus.AVIMClientStatusPaused;
    }
    if (null != callback) {
      callback.internalDone(status, null);
    }
    return true;
  }

  public boolean renewSessionToken(LCConnectionManager connectionManager, String clientId, final LCIMClientCallback callback) {
    LOGGER.d("renewSessionToken...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return renewSessionTokenDirectly(connectionManager, clientId, requestId);
  }

  public boolean closeClient(LCConnectionManager connectionManager, String self, final LCIMClientCallback callback) {
    LOGGER.d("closeClient...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    return closeClientDirectly(connectionManager, self, requestId);
  }

  public boolean queryOnlineClients(LCConnectionManager connectionManager, String self, List<String> clients, final LCIMOnlineClientsCallback callback) {
    LOGGER.d("queryOnlineClients...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    return queryOnlineClientsDirectly(connectionManager, self, clients, requestId);
  }

  public boolean createConversation(LCConnectionManager connectionManager, final String self, final List<String> memberList,
                                    final Map<String, Object> attribute, final boolean isTransient, final boolean isUnique,
                                    final boolean isTemp, int tempTTL, final LCIMCommonJsonCallback callback) {
    LOGGER.d("createConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    return createConversationDirectly(connectionManager, self, memberList, attribute, isTransient, isUnique, isTemp, tempTTL, requestId);
  }

  public boolean updateConversation(LCConnectionManager connectionManager, final String clientId, String conversationId, int convType,
                                    final Map<String, Object> param, final LCIMCommonJsonCallback callback) {
    LOGGER.d("updateConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.updateConversationDirectly(connectionManager, clientId, conversationId, convType, param, requestId);
  }

  public boolean participateConversation(LCConnectionManager connectionManager, final String clientId, String conversationId, int convType, final Map<String, Object> param,
                                         Conversation.AVIMOperation operation, final LCIMConversationCallback callback) {
    LOGGER.d("participateConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.participateConversationDirectly(connectionManager, clientId, conversationId, convType, param, operation, requestId);
  }

  public boolean queryConversations(LCConnectionManager connectionManager, final String clientId, final String queryString, final LCIMCommonJsonCallback callback) {
    return queryConversationsInternally(connectionManager, clientId, queryString, callback);
  }

  public boolean queryConversationsInternally(LCConnectionManager connectionManager, final String clientId, final String queryString, final LCIMCommonJsonCallback callback) {
    LOGGER.d("queryConversationsInternally...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.queryConversationsDirectly(connectionManager, clientId, queryString, requestId);
  }

  public boolean sendMessage(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, final LCIMMessage message,
                             final LCIMMessageOption messageOption, final LCIMCommonJsonCallback callback) {
    LOGGER.d("sendMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.sendMessageDirectly(connectionManager, clientId, conversationId, convType, message, messageOption, requestId);
  }

  public boolean updateMessage(LCConnectionManager connectionManager, String clientId, int convType, LCIMMessage oldMessage, LCIMMessage newMessage,
                               final LCIMCommonJsonCallback callback) {
    LOGGER.d("updateMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.updateMessageDirectly(connectionManager, clientId, convType, oldMessage, newMessage, requestId);
  }

  public boolean recallMessage(LCConnectionManager connectionManager, String clientId, int convType, LCIMMessage message, final LCIMCommonJsonCallback callback) {
    LOGGER.d("recallMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.recallMessageDirectly(connectionManager, clientId, convType, message, requestId);
  }

  public boolean fetchReceiptTimestamps(LCConnectionManager connectionManager, String clientId, String conversationId, int convType,
                                        Conversation.AVIMOperation operation, final LCIMCommonJsonCallback callback) {
    LOGGER.d("fetchReceiptTimestamps...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.fetchReceiptTimestampsDirectly(connectionManager, clientId, conversationId, convType, operation, requestId);
  }

  public boolean processMembers(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, String params, Conversation.AVIMOperation op,
                                final LCCallback callback) {
    LOGGER.d("processMembers...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.processMembersDirectly(connectionManager, clientId, conversationId, convType, params, op, requestId);
  }

  public boolean queryMessages(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, String params,
                               Conversation.AVIMOperation operation, final LCIMMessagesQueryCallback callback) {
    LOGGER.d("queryMessages...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    return this.queryMessagesDirectly(connectionManager, clientId, conversationId, convType, params, operation, requestId);
  }

  public boolean markConversationRead(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, Map<String, Object> lastMessageParam) {
    LOGGER.d("markConversationRead...");
    int requestId = WindTalker.getNextIMRequestId();
    return this.markConversationReadDirectly(connectionManager, clientId, conversationId, convType, lastMessageParam, requestId);
  }

  public boolean loginLiveQuery(LCConnectionManager connectionManager, String subscriptionId, final LCLiveQuerySubscribeCallback callback) {
    LOGGER.d("loginLiveQuery...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID, null, requestId, callback);
    } else {
      LOGGER.d("don't cache livequery login request.");
    }
    return loginLiveQueryDirectly(connectionManager, subscriptionId, requestId);
  }

  public boolean loginLiveQueryDirectly(LCConnectionManager connectionManager, String subscriptionId, int requestId) {
    if (StringUtil.isEmpty(subscriptionId)) {
      return false;
    }
    LiveQueryOperationDelegate.getInstance().login(subscriptionId, requestId);
    return true;
  }

  private String getInstallationId(String clientId) {
    LCIMClient client = LCIMClient.peekInstance(clientId);
    if (null != client) {
      return client.getInstallationId();
    }
    return null;
  }

  public boolean openClientDirectly(LCConnectionManager connectionManager, String clientId, String tag, String userSessionToken,
                                    boolean reConnect, int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    session.open(tag, userSessionToken, reConnect, requestId);
    return true;
  }

  public boolean closeClientDirectly(LCConnectionManager connectionManager, String self, int requestId) {
    String installationId = getInstallationId(self);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(self, installationId, connectionManager);
    session.close(requestId);
    return true;
  }
  public boolean renewSessionTokenDirectly(LCConnectionManager connectionManager, String clientId, int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    session.renewRealtimeSesionToken(requestId);
    return true;
  }
  public boolean queryOnlineClientsDirectly(LCConnectionManager connectionManager, String self, List<String> clients, int requestId) {
    String installationId = getInstallationId(self);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(self, installationId, connectionManager);
    session.queryOnlinePeers(clients, requestId);
    return true;
  }

  public boolean createConversationDirectly(LCConnectionManager connectionManager, final String self, final List<String> members,
                                            final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                            final boolean isTemp, int tempTTL, int requestId) {
    String installationId = getInstallationId(self);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(self, installationId, connectionManager);
    session.createConversation(members, attributes, isTransient, isUnique, isTemp, tempTTL, false, requestId);
    return true;
  }

  public boolean queryConversationsDirectly(LCConnectionManager connectionManager, final String clientId,
                                            final String queryString, int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    Map<String, Object> map = new HashMap<>();
    session.queryConversations(JSON.parseObject(queryString, map.getClass()), requestId, MDFive.computeMD5(queryString));
    return true;
  }

  public boolean updateConversationDirectly(LCConnectionManager connectionManager, final String clientId,
                                            String conversationId, int convType,
                                            final Map<String, Object> param, int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    LCConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.updateInfo(param, requestId);
    return true;
  }

  public boolean participateConversationDirectly(LCConnectionManager connectionManager, final String clientId,
                                                 String conversationId, int convType,
                                                 final Map<String, Object> param,
                                                 Conversation.AVIMOperation operation, int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    LCConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.processConversationCommandFromClient(operation, param, requestId);
    return true;
  }

  public boolean sendMessageDirectly(LCConnectionManager connectionManager, String clientId, String conversationId,
                                     int convType, final LCIMMessage message,
                                     final LCIMMessageOption messageOption, int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    LCConversationHolder holder = session.getConversationHolder(conversationId, convType);
    message.setFrom(clientId);
    holder.sendMessage(message, requestId, null == messageOption? new LCIMMessageOption() : messageOption);
    return true;
  }
  public boolean updateMessageDirectly(LCConnectionManager connectionManager, String clientId, int convType,
                                       LCIMMessage oldMessage, LCIMMessage newMessage,
                                       int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    LCConversationHolder holder = session.getConversationHolder(oldMessage.getConversationId(), convType);
    holder.patchMessage(oldMessage, newMessage, null, Conversation.AVIMOperation.CONVERSATION_UPDATE_MESSAGE,
            requestId);
    return true;
  }
  public boolean recallMessageDirectly(LCConnectionManager connectionManager, String clientId, int convType,
                                       LCIMMessage message, int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    LCConversationHolder holder = session.getConversationHolder(message.getConversationId(), convType);
    holder.patchMessage(null, null, message, Conversation.AVIMOperation.CONVERSATION_RECALL_MESSAGE,
            requestId);
    return true;
  }
  public boolean fetchReceiptTimestampsDirectly(LCConnectionManager connectionManager, String clientId,
                                                String conversationId, int convType, Conversation.AVIMOperation operation,
                                                int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    LCConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.getReceiptTime(requestId);
    return true;
  }
  public boolean queryMessagesDirectly(LCConnectionManager connectionManager, String clientId, String conversationId,
                                       int convType, String params,
                                       Conversation.AVIMOperation operation, int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    LCConversationHolder holder = session.getConversationHolder(conversationId, convType);
    Map<String, Object> queryParam = JSON.parseObject(params, Map.class);
    holder.processConversationCommandFromClient(operation, queryParam, requestId);
    return true;
  }

  public boolean processMembersDirectly(LCConnectionManager connectionManager, String clientId, String conversationId,
                                        int convType, String params, Conversation.AVIMOperation op,
                                        int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    LCConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.processConversationCommandFromClient(op, JSON.parseObject(params, Map.class), requestId);
    return true;
  }

  public boolean markConversationReadDirectly(LCConnectionManager connectionManager, String clientId,
                                              String conversationId, int convType,
                                              Map<String, Object> lastMessageParam, int requestId) {
    String installationId = getInstallationId(clientId);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, installationId, connectionManager);
    LCConversationHolder holder = session.getConversationHolder(conversationId, convType);
    holder.processConversationCommandFromClient(Conversation.AVIMOperation.CONVERSATION_READ, lastMessageParam, requestId);
    return true;
  }

  private LCCallback getCachedCallback(final String clientId, final String conversationId, int requestId,
                                       Conversation.AVIMOperation operation) {
    return RequestCache.getInstance().getRequestCallback(clientId, null, requestId);
  }

  public void onOperationCompleted(String clientId, String conversationId, int requestId,
                                   Conversation.AVIMOperation operation, Throwable throwable) {

    LCCallback callback = getCachedCallback(clientId, conversationId, requestId, operation);
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
        callback.internalDone(LCIMClient.getInstance(clientId), LCIMException.wrapperAVException(throwable));
        break;
      default:
        callback.internalDone(LCIMException.wrapperAVException(throwable));
        break;
    }
    RequestCache.getInstance().cleanRequestCallback(clientId, conversationId, requestId);
  }

  public void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                                     Conversation.AVIMOperation operation, HashMap<String, Object> resultData) {
    LCCallback callback = getCachedCallback(clientId, conversationId, requestId, operation);
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
        if (callback instanceof LCIMConversationIterableResultCallback) {
          LCIMConversationIterableResult iterableResult = new LCIMConversationIterableResult();
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
    LCCallback callback = getCachedCallback(LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID, null, requestId, null);
    if (null != callback) {
      LOGGER.d("call livequery login callback with exception:" + throwable);
      callback.internalDone(null == throwable? null : new LCException(throwable));
    } else {
      LOGGER.d("no callback found for livequery login request.");
    }
    RequestCache.getInstance().cleanRequestCallback(LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID, null, requestId);
  }
}
