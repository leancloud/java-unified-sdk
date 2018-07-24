package cn.leancloud.im;

import cn.leancloud.Messages;
import cn.leancloud.im.v2.Conversation;

import java.util.Map;

public class SimpleEventBroadcast implements EventBroadcast {
  public void onOperationCompleted(String clientId, String conversationId, int requestId,
                            Conversation.AVIMOperation operation, Throwable throwable) {
    ;
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
