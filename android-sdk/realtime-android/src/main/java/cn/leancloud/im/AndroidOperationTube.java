package cn.leancloud.im;

import java.util.List;
import java.util.Map;

import cn.leancloud.Messages;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMClientStatusCallback;
import cn.leancloud.im.v2.callback.AVIMCommonJsonCallback;
import cn.leancloud.im.v2.callback.AVIMMessagesQueryCallback;
import cn.leancloud.im.v2.callback.AVIMOnlineClientsCallback;

/**
 * Created by fengjunwen on 2018/7/3.
 */

public class AndroidOperationTube implements OperationTube {
  public boolean openClient(String clientId, String tag, String userSessionToken,
                     boolean reConnect, AVIMClientCallback callback) {
    return false;
  }
  public boolean queryClientStatus(String clientId, final AVIMClientStatusCallback callback) {
    return false;
  }
  public boolean closeClient(String self, AVIMClientCallback callback) {
    return false;
  }
  public boolean renewSessionToken(String clientId, AVIMClientCallback callback) {
    return false;
  }
  public boolean queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback) {
    return false;
  }

  public boolean createConversation(final String self, final List<String> members,
                             final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                             final boolean isTemp, int tempTTL, final AVIMCommonJsonCallback callback) {
    return false;
  }

  public boolean queryConversations(final String clientId, final String queryString, final AVIMCommonJsonCallback callback) {
    return false;
  }
  public boolean queryConversationsInternally(final String clientId, final String queryString, final AVIMCommonJsonCallback callback) {
    return false;
  }

  public boolean sendMessage(String clientId, String conversationId, int convType, final AVIMMessage message, final AVIMMessageOption messageOption,
                      final AVIMCommonJsonCallback callback) {
    return false;
  }
  public boolean updateMessage(String clientId, int convType, AVIMMessage oldMessage, AVIMMessage newMessage,
                        AVIMCommonJsonCallback callback) {
    return false;
  }
  public boolean recallMessage(String clientId, int convType, AVIMMessage message, AVIMCommonJsonCallback callback) {
    return false;
  }
  public boolean fetchReceiptTimestamps(String clientId, String conversationId, int convType, Conversation.AVIMOperation operation,
                                 AVIMCommonJsonCallback callback) {
    return false;
  }
  public boolean queryMessages(String clientId, String conversationId, int convType, String params,
                        Conversation.AVIMOperation operation, AVIMMessagesQueryCallback callback) {
    return false;
  }

  public boolean updateMembers(String clientId, String conversationId, int convType, String params, Conversation.AVIMOperation op,
                        AVCallback callback) {
    return false;
  }

  // response notifier
  public void onOperationCompleted(String clientId, String conversationId, int requestId,
                            Conversation.AVIMOperation operation, Throwable throwable) {
    return;
  }
  public void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                              Conversation.AVIMOperation operation, Map<String, Object> resultData) {
    return;
  }
  public void onMessageArrived(String clientId, String conversationId, int requestId,
                        Conversation.AVIMOperation operation, Messages.GenericCommand command) {
    return;
  }
  public void onLiveQueryCompleted(int requestId, Throwable throwable) {
    return;
  }
  public void onPushMessage(String message, String messageId) {
    return;
  }
}
