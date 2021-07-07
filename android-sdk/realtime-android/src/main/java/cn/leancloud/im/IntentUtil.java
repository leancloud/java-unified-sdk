package cn.leancloud.im;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import cn.leancloud.utils.LocalBroadcastManager;

import java.util.HashMap;

import cn.leancloud.LeanCloud;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.livequery.LCLiveQuery;
import cn.leancloud.utils.StringUtil;

/**
 * Created by fengjunwen on 2018/8/7.
 */

public class IntentUtil {
  public static final String CALLBACK_RESULT_KEY = "callbackHashMap";

  // http://stackoverflow.com/questions/9783704/broadcast-receiver-onreceive-never-called
  public static Intent setupIntentFlags(Intent i) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
      i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    }
    return i;
  }

  public static void sendIMLocalBroadcast(String clientId, String conversationId, int requestId,
                                          Conversation.LCIMOperation operation) {
    sendIMLocalBroadcast(clientId, conversationId, requestId, null, null, operation);
  }

  public static void sendIMLocalBroadcast(String clientId, String conversationId, int requestId,
                                          Throwable throwable, Conversation.LCIMOperation operation) {
    sendIMLocalBroadcast(clientId, conversationId, requestId, null, throwable, operation);
  }

  public static void sendIMLocalBroadcast(String clientId, String conversationId, int requestId,
                                          Bundle bundle, Conversation.LCIMOperation operation) {
    sendIMLocalBroadcast(clientId, conversationId, requestId, bundle, null, operation);
  }

  public static void sendMap2LocalBroadcase(String clientId, String conversationId, int requestId,
                                          HashMap<String, Object> result, Throwable throwable,
                                          Conversation.LCIMOperation operation) {
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
      if (null != result) {
        callbackIntent.putExtra(CALLBACK_RESULT_KEY, result);
      }
      if (LeanCloud.getContext() != null) {
        LocalBroadcastManager.getInstance(LeanCloud.getContext()).sendBroadcast(callbackIntent);
      }
    }
  }

  public static void sendLiveQueryLocalBroadcast(int requestId, Throwable throwable) {
    Intent callbackIntent = new Intent(LCLiveQuery.LIVEQUERY_PRIFIX + requestId);
    if (null != throwable) {
      callbackIntent.putExtra(Conversation.callbackExceptionKey, throwable);
    }
    if (LeanCloud.getContext() != null) {
      LocalBroadcastManager.getInstance(LeanCloud.getContext()).sendBroadcast(callbackIntent);
    }
  }

  private static void sendIMLocalBroadcast(String clientId, String conversationId, int requestId,
                                           Bundle bundle, Throwable throwable, Conversation.LCIMOperation operation) {
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
      if (LeanCloud.getContext() != null) {
        LocalBroadcastManager.getInstance(LeanCloud.getContext()).sendBroadcast(callbackIntent);
      }
    }
  }

  private static boolean isOperationValid(Conversation.LCIMOperation operation) {
    return null != operation &&
        Conversation.LCIMOperation.CONVERSATION_UNKNOWN.getCode() != operation.getCode();
  }
}
