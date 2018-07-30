package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.session.AVSession;
import cn.leancloud.session.AVSessionManager;
import cn.leancloud.utils.LogUtil;

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
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    session.open(tag, userSessionToken, reConnect, requestId);
    return true;
  }

  public boolean queryClientStatus(String clientId, final AVIMClientStatusCallback callback) {
    return false;
  }

  public boolean closeClient(String self, AVIMClientCallback callback) {
    return false;
  }

  public boolean queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback) {
    return false;
  }

  public boolean createConversation(final List<String> members, final String name,
                          final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                          final boolean isTemp, int tempTTL, final AVIMConversationCreatedCallback callback) {
    return false;
  }


  public boolean sendMessage(final AVIMMessage message, final AVIMMessageOption messageOption, final AVIMConversationCallback callback) {
    return false;
  }

  public boolean updateMessage(AVIMMessage oldMessage, AVIMMessage newMessage, AVIMMessageUpdatedCallback callback) {
    return false;
  }

  public boolean recallMessage(AVIMMessage message, AVIMMessageRecalledCallback callback) {
    return false;
  }

  public boolean fetchReceiptTimestamps(String clientId, String conversationId, Conversation.AVIMOperation operation,
                                 AVIMConversationCallback callback) {
    return true;
  }

  public boolean queryMessages(String clientId, String conversationId, int convType, String params,
                        Conversation.AVIMOperation operation, AVIMMessagesQueryCallback callback) {
    return false;
  }


  public void onOperationCompleted(String clientId, String conversationId, int requestId,
                                   Conversation.AVIMOperation operation, Throwable throwable) {
    LOGGER.d("enter onOperationCompleted with clientId=" + clientId + ", convId=" + conversationId + ", requestId="
      + requestId + ", operation=" + operation);
    AVCallback callback = RequestCache.getInstance().getRequestCallback(clientId, conversationId, requestId);
    if (null == callback) {
      LOGGER.w("encounter illegal response, ignore it: clientId=" + clientId + ", convId=" + conversationId + ", requestId=" + requestId);
      return;
    }
    switch (operation) {
      case CLIENT_OPEN:
        callback.internalDone(AVIMClient.getInstance(clientId), AVIMException.wrapperAVException(throwable));
        break;
      default:
        LOGGER.w("no operation matched, ignore response.");
        break;
    }
    RequestCache.getInstance().cleanRequestCallback(clientId, conversationId, requestId);
  }

  public void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                                     Conversation.AVIMOperation operation, Map<String, Object> resultData) {
    ;
  }
  public void onMessageArrived(String clientId, String conversationId, int requestId,
                               Conversation.AVIMOperation operation, Messages.GenericCommand command) {
    ;
  }
  public void onLiveQueryCompleted(int requestId, Throwable throwable) {
    ;
  }
  public void onPushMessage(String message, String messageId) {
    ;
  }
}
