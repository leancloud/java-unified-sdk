package cn.leancloud.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.Messages;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMClient.AVIMClientStatus;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.Conversation.AVIMOperation;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMClientStatusCallback;
import cn.leancloud.im.v2.callback.AVIMCommonJsonCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCallback;
import cn.leancloud.im.v2.callback.AVIMMessagesQueryCallback;
import cn.leancloud.im.v2.callback.AVIMOnlineClientsCallback;
import cn.leancloud.livequery.AVLiveQuery;
import cn.leancloud.livequery.AVLiveQuerySubscribeCallback;
import cn.leancloud.push.PushService;
import cn.leancloud.session.AVSession;
import cn.leancloud.session.AVSessionManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by fengjunwen on 2018/7/3.
 */

public class AndroidOperationTube implements OperationTube {
  private static AVLogger LOGGER = LogUtil.getLogger(AndroidOperationTube.class);

  public boolean openClient(final String clientId, String tag, String userSessionToken,
                            boolean reConnect, final AVIMClientCallback callback) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CLIENT_TAG, tag);
    params.put(Conversation.PARAM_CLIENT_USERSESSIONTOKEN, userSessionToken);
    params.put(Conversation.PARAM_CLIENT_RECONNECTION, reConnect);

    LOGGER.d("openClient. clientId:" + clientId + ", tag:" + tag + ", callback:" + callback);
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          LOGGER.d("openClient get response. error:" + error);
          callback.internalDone(AVIMClient.getInstance(clientId), AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, JSON.toJSONString(params), receiver,
        AVIMOperation.CLIENT_OPEN);
  }

  public boolean queryClientStatus(String clientId, final AVIMClientStatusCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          AVIMClientStatus status = null;
          if (null != intentResult
              && intentResult.containsKey(Conversation.callbackClientStatus)) {
            status = AVIMClientStatus.getClientStatus((int) intentResult.get(Conversation.callbackClientStatus));
          }
          callback.internalDone(status, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, null, receiver, AVIMOperation.CLIENT_STATUS);
  }

  public boolean closeClient(final String self, final AVIMClientCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          AVIMClient client = AVIMClient.getInstance(self);
          callback.internalDone(client, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(self, null, receiver, AVIMOperation.CLIENT_DISCONNECT);
  }

  public boolean renewSessionToken(String clientId, final AVIMClientCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(null, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, null, receiver, AVIMOperation.CLIENT_REFRESH_TOKEN);
  }

  public boolean queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_ONLINE_CLIENTS, clients);

    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          if (error != null) {
            callback.internalDone(null, AVIMException.wrapperAVException(error));
          } else {
            List<String> onlineClients = null;
            if (null != intentResult && intentResult.containsKey(Conversation.callbackOnlineClients)) {
              onlineClients = (List<String>) intentResult.get(Conversation.callbackOnlineClients);
            }
            callback.internalDone(onlineClients, null);
          }
        }
      };
    }

    return this.sendClientCMDToPushService(self, JSON.toJSONString(params), receiver, AVIMOperation.CLIENT_ONLINE_QUERY);
  }

  public boolean createConversation(final String self, final List<String> members,
                                    final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                    final boolean isTemp, int tempTTL, final AVIMCommonJsonCallback callback) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CONVERSATION_MEMBER, members);
    params.put(Conversation.PARAM_CONVERSATION_ISUNIQUE, isUnique);
    params.put(Conversation.PARAM_CONVERSATION_ISTRANSIENT, isTransient);
    params.put(Conversation.PARAM_CONVERSATION_ISTEMPORARY, isTemp);
    if (isTemp) {
      params.put(Conversation.PARAM_CONVERSATION_TEMPORARY_TTL, tempTTL);
    }
    if (null != attributes && attributes.size() > 0) {
      params.put(Conversation.PARAM_CONVERSATION_ATTRIBUTE, attributes);
    }
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(self, JSON.toJSONString(params), receiver,
        AVIMOperation.CONVERSATION_CREATION);
  }

  public boolean updateConversation(final String clientId, String conversationId, int convType,
                                    final Map<String, Object> param, final AVIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {

        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, conversationId, convType, JSON.toJSONString(param),
        null, null, AVIMOperation.CONVERSATION_UPDATE, receiver);
  }

  public boolean participateConversation(final String clientId, String conversationId, int convType, final Map<String, Object> param,
                                         Conversation.AVIMOperation operation, final AVIMConversationCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {

        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, AVIMException.wrapperAVException(error));
        }
      };
    }
    String paramString = null != param ? JSON.toJSONString(param) : null;
    return this.sendClientCMDToPushService(clientId, conversationId, convType, paramString,
        null, null, operation, receiver);
  }

  public boolean queryConversations(final String clientId, final String queryString, final AVIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {

        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, queryString, receiver, AVIMOperation.CONVERSATION_QUERY);
  }

  public boolean queryConversationsInternally(final String clientId, final String queryString,
                                              final AVIMCommonJsonCallback callback) {
    // internal query conversation.
    LOGGER.d("queryConversationsInternally...");
    int requestId = WindTalker.getNextIMRequestId();
    RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    session.queryConversations(JSON.parseObject(queryString, Map.class), requestId);
    return true;
  }

  public boolean sendMessage(String clientId, String conversationId, int convType, final AVIMMessage message,
                             final AVIMMessageOption messageOption, final AVIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, conversationId, convType, null,
        message, messageOption, AVIMOperation.CONVERSATION_SEND_MESSAGE, receiver);
  }

  public boolean updateMessage(String clientId, int convType, AVIMMessage oldMessage, AVIMMessage newMessage,
                               final AVIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService2(clientId, oldMessage.getConversationId(), convType, oldMessage,
        newMessage, AVIMOperation.CONVERSATION_UPDATE_MESSAGE, receiver);
  }

  public boolean recallMessage(String clientId, int convType, AVIMMessage message,
                               final AVIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, message.getConversationId(), convType, null,
        message, null, AVIMOperation.CONVERSATION_RECALL_MESSAGE, receiver);
  }

  public boolean fetchReceiptTimestamps(String clientId, String conversationId, int convType, Conversation.AVIMOperation operation,
                                        final AVIMCommonJsonCallback callback) {
    return false;
  }

  public boolean queryMessages(String clientId, String conversationId, int convType, String params,
                               final Conversation.AVIMOperation operation, final AVIMMessagesQueryCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          List<AVIMMessage> msg = (null == intentResult) ?
              null : (List<AVIMMessage>) intentResult.get(Conversation.callbackHistoryMessages);
          callback.internalDone(msg, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, conversationId, convType, params, null, null,
        AVIMOperation.CONVERSATION_MESSAGE_QUERY, receiver);
  }

  public boolean processMembers(String clientId, String conversationId, int convType, String params,
                                Conversation.AVIMOperation op, final AVCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, AVIMException.wrapperAVException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, conversationId, convType, params, null, null,
        op, receiver);
  }

  public boolean markConversationRead(String clientId, String conversationId, int convType,
                                      Map<String, Object> lastMessageParam) {
    String dataString = null == lastMessageParam ? null : JSON.toJSONString(lastMessageParam);
    return this.sendClientCMDToPushService(clientId, conversationId, convType, dataString,
        null, null, AVIMOperation.CONVERSATION_READ, null);
  }

  public boolean loginLiveQuery(String subscriptionId, final AVLiveQuerySubscribeCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new AVIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          if (null != callback) {
            callback.internalDone(null == error ? null : new AVException(error));
          }
        }
      };
    }
    int requestId = WindTalker.getNextIMRequestId();
    LocalBroadcastManager.getInstance(AVOSCloud.getContext()).registerReceiver(receiver,
        new IntentFilter(AVLiveQuery.LIVEQUERY_PRIFIX + requestId));
    try {
      Intent i = new Intent(AVOSCloud.getContext(), PushService.class);
      i.setAction(AVLiveQuery.ACTION_LIVE_QUERY_LOGIN);
      i.putExtra(AVLiveQuery.SUBSCRIBE_ID, subscriptionId);
      i.putExtra(Conversation.INTENT_KEY_REQUESTID, requestId);
      AVOSCloud.getContext().startService(IntentUtil.setupIntentFlags(i));
    } catch (Exception ex) {
      LOGGER.e("failed to start PushServer. cause: " + ex.getMessage());
      return false;
    }
    return true;
  }

  protected boolean sendClientCMDToPushService(String clientId, String dataAsString, BroadcastReceiver receiver,
                                               AVIMOperation operation) {

    int requestId = WindTalker.getNextIMRequestId();

    if (receiver != null) {
      LocalBroadcastManager.getInstance(AVOSCloud.getContext()).registerReceiver(receiver,
          new IntentFilter(operation.getOperation() + requestId));
    }
    Intent i = new Intent(AVOSCloud.getContext(), PushService.class);
    i.setAction(Conversation.AV_CONVERSATION_INTENT_ACTION);
    if (!StringUtil.isEmpty(dataAsString)) {
      i.putExtra(Conversation.INTENT_KEY_DATA, dataAsString);
    }

    i.putExtra(Conversation.INTENT_KEY_CLIENT, clientId);
    i.putExtra(Conversation.INTENT_KEY_REQUESTID, requestId);
    i.putExtra(Conversation.INTENT_KEY_OPERATION, operation.getCode());
    try {
      AVOSCloud.getContext().startService(IntentUtil.setupIntentFlags(i));
    } catch (Exception ex) {
      LOGGER.e("failed to startService. cause: " + ex.getMessage());
      return false;
    }
    return true;
  }

  protected boolean sendClientCMDToPushService(String clientId, String conversationId, int convType,
                                               String dataAsString, final AVIMMessage message,
                                               final AVIMMessageOption option, final AVIMOperation operation,
                                               BroadcastReceiver receiver) {
    int requestId = WindTalker.getNextIMRequestId();
    if (null != receiver) {
      LocalBroadcastManager.getInstance(AVOSCloud.getContext()).registerReceiver(receiver,
          new IntentFilter(operation.getOperation() + requestId));
    }
    Intent i = new Intent(AVOSCloud.getContext(), PushService.class);
    i.setAction(Conversation.AV_CONVERSATION_INTENT_ACTION);
    if (!StringUtil.isEmpty(dataAsString)) {
      i.putExtra(Conversation.INTENT_KEY_DATA, dataAsString);
    }
    if (null != message) {
      i.putExtra(Conversation.INTENT_KEY_DATA, message.toJSONString());
      if (null != option) {
        i.putExtra(Conversation.INTENT_KEY_MESSAGE_OPTION, option.toJSONString());
      }
    }
    i.putExtra(Conversation.INTENT_KEY_CLIENT, clientId);
    i.putExtra(Conversation.INTENT_KEY_CONVERSATION, conversationId);
    i.putExtra(Conversation.INTENT_KEY_CONV_TYPE, convType);
    i.putExtra(Conversation.INTENT_KEY_OPERATION, operation.getCode());
    i.putExtra(Conversation.INTENT_KEY_REQUESTID, requestId);
    try {
      AVOSCloud.getContext().startService(IntentUtil.setupIntentFlags(i));
    } catch (Exception ex) {
      LOGGER.e("failed to startService. cause: " + ex.getMessage());
      return false;
    }
    return true;
  }

  protected boolean sendClientCMDToPushService2(String clientId, String conversationId, int convType,
                                                final AVIMMessage message, final AVIMMessage message2,
                                                final AVIMOperation operation,
                                                BroadcastReceiver receiver) {
    int requestId = WindTalker.getNextIMRequestId();
    if (null != receiver) {
      LocalBroadcastManager.getInstance(AVOSCloud.getContext()).registerReceiver(receiver,
          new IntentFilter(operation.getOperation() + requestId));
    }
    Intent i = new Intent(AVOSCloud.getContext(), PushService.class);
    i.setAction(Conversation.AV_CONVERSATION_INTENT_ACTION);

    if (null != message) {
      i.putExtra(Conversation.INTENT_KEY_DATA, message.toJSONString());
    }
    if (null != message2) {
      i.putExtra(Conversation.INTENT_KEY_MESSAGE_EX, message2.toJSONString());
    }
    i.putExtra(Conversation.INTENT_KEY_CLIENT, clientId);
    i.putExtra(Conversation.INTENT_KEY_CONVERSATION, conversationId);
    i.putExtra(Conversation.INTENT_KEY_CONV_TYPE, convType);
    i.putExtra(Conversation.INTENT_KEY_OPERATION, operation.getCode());
    i.putExtra(Conversation.INTENT_KEY_REQUESTID, requestId);
    try {
      AVOSCloud.getContext().startService(IntentUtil.setupIntentFlags(i));
    } catch (Exception ex) {
      LOGGER.e("failed to startService. cause: " + ex.getMessage());
      return false;
    }
    return true;
  }

  // response notifier
  public void onOperationCompleted(String clientId, String conversationId, int requestId,
                                   Conversation.AVIMOperation operation, Throwable throwable) {
    if (AVIMOperation.CONVERSATION_QUERY == operation) {
      AVCallback callback = RequestCache.getInstance().getRequestCallback(clientId, null, requestId);
      if (null != callback) {
        // internal query conversation.
        callback.internalDone(null, AVIMException.wrapperAVException(throwable));
        RequestCache.getInstance().cleanRequestCallback(clientId, null, requestId);
        return;
      }
    }
    IntentUtil.sendIMLocalBroadcast(clientId, conversationId, requestId, throwable, operation);
  }

  public void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                                     Conversation.AVIMOperation operation, HashMap<String, Object> resultData) {
    if (AVIMOperation.CONVERSATION_QUERY == operation) {
      AVCallback callback = RequestCache.getInstance().getRequestCallback(clientId, null, requestId);
      if (null != callback) {
        // internal query conversation.
        callback.internalDone(resultData, null);
        RequestCache.getInstance().cleanRequestCallback(clientId, null, requestId);
        return;
      }
    }
    IntentUtil.sendMap2LocalBroadcase(clientId, conversationId, requestId, resultData, null, operation);
    return;
  }

  public void onLiveQueryCompleted(int requestId, Throwable throwable) {
    IntentUtil.sendLiveQueryLocalBroadcast(requestId, throwable);
  }

  public void onPushMessage(String message, String messageId) {
    return;
  }
}
