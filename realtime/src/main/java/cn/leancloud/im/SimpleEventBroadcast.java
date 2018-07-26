package cn.leancloud.im;

import cn.leancloud.Messages;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.Conversation;

import java.util.Map;

public class SimpleEventBroadcast implements EventBroadcast {
  public void onOperationCompleted(String clientId, String conversationId, int requestId,
                            Conversation.AVIMOperation operation, Throwable throwable) {
    AVCallback callback = RequestCache.getInstance().getRequestCallback(clientId, conversationId, requestId);
    if (null == callback) {
      return;
    }
    switch (operation) {
      case CLIENT_OPEN:
        callback.internalDone(AVIMClient.getInstance(clientId), AVIMException.wrapperAVException(throwable));
        break;
    }
  }
  public void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                              Conversation.AVIMOperation operation, Map<String, Object> resultData) {
    ;
  }
  public void onMessageArrived(String clientId, String conversationId, int requestId,
                        Conversation.AVIMOperation operation, Messages.GenericCommand command) {
    ;
  }
}
