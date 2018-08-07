package cn.leancloud.im;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import cn.leancloud.AVOSCloud;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.utils.StringUtil;

/**
 * Created by fengjunwen on 2018/8/7.
 */

public class IntentUtil {
  // http://stackoverflow.com/questions/9783704/broadcast-receiver-onreceive-never-called
  public static Intent setupIntentFlags(Intent i) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
      i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    }
    return i;
  }

  public static void sendIMLocalBroadcast(String clientId, String conversationId, int requestId,
                                          Conversation.AVIMOperation operation) {
    sendIMLocalBroadcast(clientId, conversationId, requestId, null, null, operation);
  }

  public static void sendIMLocalBroadcast(String clientId, String conversationId, int requestId,
                                          Throwable throwable, Conversation.AVIMOperation operation) {
    sendIMLocalBroadcast(clientId, conversationId, requestId, null, throwable, operation);
  }

  public static void sendIMLocalBroadcast(String clientId, String conversationId, int requestId,
                                          Bundle bundle, Conversation.AVIMOperation operation) {
    sendIMLocalBroadcast(clientId, conversationId, requestId, bundle, null, operation);
  }

  private static void sendIMLocalBroadcast(String clientId, String conversationId, int requestId,
                                           Bundle bundle, Throwable throwable, Conversation.AVIMOperation operation) {
    if (isOperationValid(operation)) {
      String keyHeader = operation.getOperation();

      Intent callbackIntent = new Intent(keyHeader + requestId);

      callbackIntent.putExtra(Conversation.callbackClientKey, clientId);
      if (!StringUtil.isEmpty(conversationId)) {
        callbackIntent.putExtra(Conversation.callbackConversationKey, conversationId);
      }

      if (null != throwable) {
        callbackIntent.putExtra(Conversation.callbackExceptionKey, throwable);
      }

      if (null != bundle) {
        callbackIntent.putExtras(bundle);
      }
      LocalBroadcastManager.getInstance(AVOSCloud.getContext()).sendBroadcast(callbackIntent);
    }
  }

  private static boolean isOperationValid(Conversation.AVIMOperation operation) {
    return null != operation &&
        Conversation.AVIMOperation.CONVERSATION_UNKNOWN.getCode() != operation.getCode();
  }
}
