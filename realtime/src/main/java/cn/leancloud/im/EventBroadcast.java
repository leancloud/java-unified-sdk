package cn.leancloud.im;

import cn.leancloud.Messages;
import cn.leancloud.im.v2.Conversation;

import java.util.Map;

public interface EventBroadcast {
  void onOperationCompleted(String clientId, String conversationId, int requestId,
                         Conversation.AVIMOperation operation, Throwable throwable);
  void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                            Conversation.AVIMOperation operation, Map<String, Object> resultData);
  void onMessageArrived(String clientId, String conversationId, int requestId,
                        Conversation.AVIMOperation operation, Messages.GenericCommand command);
}
