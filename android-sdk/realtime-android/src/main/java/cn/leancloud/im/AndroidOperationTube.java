package cn.leancloud.im;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leancloud.utils.LocalBroadcastManager;

import cn.leancloud.LCException;
import cn.leancloud.LCInstallation;
import cn.leancloud.LCLogger;
import cn.leancloud.LeanCloud;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.codec.MDFive;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMClient.LCIMClientStatus;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.LCIMMessage;
import cn.leancloud.im.v2.LCIMMessageOption;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.Conversation.LCIMOperation;
import cn.leancloud.im.v2.callback.LCIMClientCallback;
import cn.leancloud.im.v2.callback.LCIMClientStatusCallback;
import cn.leancloud.im.v2.callback.LCIMCommonJsonCallback;
import cn.leancloud.im.v2.callback.LCIMConversationCallback;
import cn.leancloud.im.v2.callback.LCIMConversationIterableResult;
import cn.leancloud.im.v2.callback.LCIMConversationIterableResultCallback;
import cn.leancloud.im.v2.callback.LCIMMessagesQueryCallback;
import cn.leancloud.im.v2.callback.LCIMOnlineClientsCallback;
import cn.leancloud.json.JSON;
import cn.leancloud.livequery.LCLiveQuery;
import cn.leancloud.livequery.LCLiveQuerySubscribeCallback;
import cn.leancloud.push.PushService;
import cn.leancloud.session.LCConnectionManager;
import cn.leancloud.session.LCSession;
import cn.leancloud.session.LCSessionManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by fengjunwen on 2018/7/3.
 */

public class AndroidOperationTube implements OperationTube {
  private static LCLogger LOGGER = LogUtil.getLogger(AndroidOperationTube.class);

  public boolean openClient(LCConnectionManager connectionManager, final String clientId, String tag, String userSessionToken,
                            boolean reConnect, final LCIMClientCallback callback) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_CLIENT_TAG, tag);
    params.put(Conversation.PARAM_CLIENT_USERSESSIONTOKEN, userSessionToken);
    params.put(Conversation.PARAM_CLIENT_RECONNECTION, reConnect);

    LOGGER.d("openClient. clientId:" + clientId + ", tag:" + tag + ", callback:" + callback);
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          LOGGER.d("openClient get response. error:" + error);
          callback.internalDone(LCIMClient.getInstance(clientId), LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, JSON.toJSONString(params), receiver,
        LCIMOperation.CLIENT_OPEN);
  }

  public boolean queryClientStatus(LCConnectionManager connectionManager, String clientId, final LCIMClientStatusCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          LCIMClientStatus status = null;
          if (null != intentResult
              && intentResult.containsKey(Conversation.callbackClientStatus)) {
            status = LCIMClientStatus.getClientStatus((int) intentResult.get(Conversation.callbackClientStatus));
          }
          callback.internalDone(status, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, null, receiver, LCIMOperation.CLIENT_STATUS);
  }

  public boolean closeClient(LCConnectionManager connectionManager, final String self, final LCIMClientCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          LCIMClient client = LCIMClient.getInstance(self);
          callback.internalDone(client, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(self, null, receiver, LCIMOperation.CLIENT_DISCONNECT);
  }

  public boolean renewSessionToken(LCConnectionManager connectionManager, String clientId, final LCIMClientCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(null, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, null, receiver, LCIMOperation.CLIENT_REFRESH_TOKEN);
  }

  public boolean queryOnlineClients(LCConnectionManager connectionManager, String self, List<String> clients, final LCIMOnlineClientsCallback callback) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Conversation.PARAM_ONLINE_CLIENTS, clients);

    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          if (error != null) {
            callback.internalDone(null, LCIMException.wrapperException(error));
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

    return this.sendClientCMDToPushService(self, JSON.toJSONString(params), receiver, LCIMOperation.CLIENT_ONLINE_QUERY);
  }

  public boolean createConversation(LCConnectionManager connectionManager, final String self, final List<String> members,
                                    final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                    final boolean isTemp, int tempTTL, final LCIMCommonJsonCallback callback) {
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
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(self, JSON.toJSONString(params), receiver,
        LCIMOperation.CONVERSATION_CREATION);
  }

  public boolean updateConversation(LCConnectionManager connectionManager, final String clientId, String conversationId, int convType,
                                    final Map<String, Object> param, final LCIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {

        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, conversationId, convType, JSON.toJSONString(param),
        null, null, LCIMOperation.CONVERSATION_UPDATE, receiver);
  }

  public boolean participateConversation(LCConnectionManager connectionManager, final String clientId, String conversationId, int convType, final Map<String, Object> param,
                                         Conversation.LCIMOperation operation, final LCIMConversationCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {

        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, LCIMException.wrapperException(error));
        }
      };
    }
    String paramString = null != param ? JSON.toJSONString(param) : null;
    return this.sendClientCMDToPushService(clientId, conversationId, convType, paramString,
        null, null, operation, receiver);
  }

  public boolean queryConversations(LCConnectionManager connectionManager, final String clientId, final String queryString, final LCIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (callback != null) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {

        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, queryString, receiver, LCIMOperation.CONVERSATION_QUERY);
  }

  public boolean queryConversationsInternally(LCConnectionManager connectionManager, final String clientId, final String queryString,
                                              final LCIMCommonJsonCallback callback) {
    // internal query conversation.
    LOGGER.d("queryConversationsInternally...");
    int requestId = WindTalker.getNextIMRequestId();
    RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    LCSession session = LCSessionManager.getInstance().getOrCreateSession(clientId, LCInstallation.getCurrentInstallation().getInstallationId(), connectionManager);
    session.queryConversations(JSON.parseObject(queryString, Map.class), requestId, MDFive.computeMD5(queryString));
    return true;
  }

  public boolean sendMessage(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, final LCIMMessage message,
                             final LCIMMessageOption messageOption, final LCIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, conversationId, convType, null,
        message, messageOption, LCIMOperation.CONVERSATION_SEND_MESSAGE, receiver);
  }

  public boolean updateMessage(LCConnectionManager connectionManager, String clientId, int convType, LCIMMessage oldMessage, LCIMMessage newMessage,
                               final LCIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService2(clientId, oldMessage.getConversationId(), convType, oldMessage,
        newMessage, LCIMOperation.CONVERSATION_UPDATE_MESSAGE, receiver);
  }

  public boolean recallMessage(LCConnectionManager connectionManager, String clientId, int convType, LCIMMessage message,
                               final LCIMCommonJsonCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          callback.internalDone(intentResult, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, message.getConversationId(), convType, null,
        message, null, LCIMOperation.CONVERSATION_RECALL_MESSAGE, receiver);
  }

  public boolean fetchReceiptTimestamps(LCConnectionManager connectionManager, String clientId,
                                        String conversationId, int convType, Conversation.LCIMOperation operation,
                                        final LCIMCommonJsonCallback callback) {
    return false;
  }

  public boolean queryMessages(LCConnectionManager connectionManager, String clientId, String conversationId, int convType, String params,
                               final Conversation.LCIMOperation operation, final LCIMMessagesQueryCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          List<LCIMMessage> msg = (null == intentResult) ?
              null : (List<LCIMMessage>) intentResult.get(Conversation.callbackHistoryMessages);
          callback.internalDone(msg, LCIMException.wrapperException(error));
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, conversationId, convType, params, null, null,
        LCIMOperation.CONVERSATION_MESSAGE_QUERY, receiver);
  }

  public boolean processMembers(LCConnectionManager connectionManager, String clientId,
                                String conversationId, int convType, String params,
                                Conversation.LCIMOperation op, final LCCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          if (LCIMOperation.CONVERSATION_MEMBER_COUNT_QUERY == op) {
            int result = 0;
            if (null != intentResult) {
              Object memberCount = intentResult.get(Conversation.callbackMemberCount);
              if (memberCount instanceof Integer) {
                result = (Integer) memberCount;
              }
            }
            callback.internalDone(result, LCIMException.wrapperException(error));
          } else if (LCIMOperation.CONVERSATION_BLOCKED_MEMBER_QUERY == op
              || LCIMOperation.CONVERSATION_MUTED_MEMBER_QUERY == op) {
            List<String> result = new ArrayList<>();
            String next = null;
            if (null != intentResult) {
              Object memberList = intentResult.get(Conversation.callbackData);
              next = (String) intentResult.get(Conversation.callbackIterableNext);
              if (memberList instanceof Collection) {
                result.addAll((Collection<? extends String>) memberList);
              } else if (memberList instanceof String[]) {
                result.addAll(Arrays.asList((String[])memberList));
              }
            }
            if (callback instanceof LCIMConversationIterableResultCallback) {
              LCIMConversationIterableResult iterableResult = new LCIMConversationIterableResult();
              iterableResult.setMembers(result);
              iterableResult.setNext(next);
              callback.internalDone(iterableResult, LCIMException.wrapperException(error));
            } else {
              callback.internalDone(result, LCIMException.wrapperException(error));
            }
          } else {
            callback.internalDone(intentResult, LCIMException.wrapperException(error));
          }
        }
      };
    }
    return this.sendClientCMDToPushService(clientId, conversationId, convType, params, null, null,
        op, receiver);
  }

  public boolean markConversationRead(LCConnectionManager connectionManager, String clientId, String conversationId, int convType,
                                      Map<String, Object> lastMessageParam) {
    String dataString = null == lastMessageParam ? null : JSON.toJSONString(lastMessageParam);
    return this.sendClientCMDToPushService(clientId, conversationId, convType, dataString,
        null, null, LCIMOperation.CONVERSATION_READ, null);
  }

  public boolean loginLiveQuery(LCConnectionManager connectionManager, String subscriptionId, final LCLiveQuerySubscribeCallback callback) {
    BroadcastReceiver receiver = null;
    if (null != callback) {
      receiver = new LCIMBaseBroadcastReceiver(callback) {
        @Override
        public void execute(Map<String, Object> intentResult, Throwable error) {
          if (null != callback) {
            callback.internalDone(null == error ? null : new LCException(error));
          }
        }
      };
    }
    if (LeanCloud.getContext() == null) {
      LOGGER.e("failed to startService. cause: root Context is null.");
      if (null != callback) {
        callback.internalDone(new LCException(LCException.OTHER_CAUSE,
            "root Context is null, please initialize at first."));
      }
      return false;
    }
    int requestId = WindTalker.getNextIMRequestId();
    LocalBroadcastManager.getInstance(LeanCloud.getContext()).registerReceiver(receiver,
        new IntentFilter(LCLiveQuery.LIVEQUERY_PRIFIX + requestId));
    try {
      Intent i = new Intent(LeanCloud.getContext(), PushService.class);
      i.setAction(LCLiveQuery.ACTION_LIVE_QUERY_LOGIN);
      i.putExtra(LCLiveQuery.SUBSCRIBE_ID, subscriptionId);
      i.putExtra(Conversation.INTENT_KEY_REQUESTID, requestId);
      LeanCloud.getContext().startService(IntentUtil.setupIntentFlags(i));
    } catch (Exception ex) {
      LOGGER.e("failed to start PushServer. cause: " + ex.getMessage());
      return false;
    }
    return true;
  }

  protected boolean sendClientCMDToPushService(String clientId, String dataAsString, BroadcastReceiver receiver,
                                               LCIMOperation operation) {

    if (LeanCloud.getContext() == null) {
      LOGGER.e("failed to startService. cause: root Context is null.");
      if (null != receiver && receiver instanceof LCIMBaseBroadcastReceiver) {
        ((LCIMBaseBroadcastReceiver)receiver).execute(new HashMap<>(),
            new LCException(LCException.OTHER_CAUSE, "root Context is null, please initialize at first."));
      }
      return false;
    }
    int requestId = WindTalker.getNextIMRequestId();

    if (receiver != null) {
      LocalBroadcastManager.getInstance(LeanCloud.getContext()).registerReceiver(receiver,
          new IntentFilter(operation.getOperation() + requestId));
    }
    Intent i = new Intent(LeanCloud.getContext(), PushService.class);
    i.setAction(Conversation.LC_CONVERSATION_INTENT_ACTION);
    if (!StringUtil.isEmpty(dataAsString)) {
      i.putExtra(Conversation.INTENT_KEY_DATA, dataAsString);
    }

    i.putExtra(Conversation.INTENT_KEY_CLIENT, clientId);
    i.putExtra(Conversation.INTENT_KEY_REQUESTID, requestId);
    i.putExtra(Conversation.INTENT_KEY_OPERATION, operation.getCode());
    try {
      LeanCloud.getContext().startService(IntentUtil.setupIntentFlags(i));
    } catch (Exception ex) {
      LOGGER.e("failed to startService. cause: " + ex.getMessage());
      return false;
    }
    return true;
  }

  protected boolean sendClientCMDToPushService(String clientId, String conversationId, int convType,
                                               String dataAsString, final LCIMMessage message,
                                               final LCIMMessageOption option, final LCIMOperation operation,
                                               BroadcastReceiver receiver) {
    if (LeanCloud.getContext() == null) {
      LOGGER.e("failed to startService. cause: root Context is null.");
      if (null != receiver && receiver instanceof LCIMBaseBroadcastReceiver) {
        ((LCIMBaseBroadcastReceiver)receiver).execute(new HashMap<>(),
            new LCException(LCException.OTHER_CAUSE, "root Context is null, please initialize at first."));
      }
      return false;
    }

    int requestId = WindTalker.getNextIMRequestId();
    if (null != receiver) {
      LocalBroadcastManager.getInstance(LeanCloud.getContext()).registerReceiver(receiver,
          new IntentFilter(operation.getOperation() + requestId));
    }
    Intent i = new Intent(LeanCloud.getContext(), PushService.class);
    i.setAction(Conversation.LC_CONVERSATION_INTENT_ACTION);
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
      LeanCloud.getContext().startService(IntentUtil.setupIntentFlags(i));
    } catch (Exception ex) {
      LOGGER.e("failed to startService. cause: " + ex.getMessage());
      return false;
    }
    return true;
  }

  protected boolean sendClientCMDToPushService2(String clientId, String conversationId, int convType,
                                                final LCIMMessage message, final LCIMMessage message2,
                                                final LCIMOperation operation,
                                                BroadcastReceiver receiver) {
    if (LeanCloud.getContext() == null) {
      LOGGER.e("failed to startService. cause: root Context is null.");
      if (null != receiver && receiver instanceof LCIMBaseBroadcastReceiver) {
        ((LCIMBaseBroadcastReceiver)receiver).execute(new HashMap<>(),
            new LCException(LCException.OTHER_CAUSE, "root Context is null, please initialize at first."));
      }
      return false;
    }
    int requestId = WindTalker.getNextIMRequestId();
    if (null != receiver) {
      LocalBroadcastManager.getInstance(LeanCloud.getContext()).registerReceiver(receiver,
          new IntentFilter(operation.getOperation() + requestId));
    }
    Intent i = new Intent(LeanCloud.getContext(), PushService.class);
    i.setAction(Conversation.LC_CONVERSATION_INTENT_ACTION);

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
      LeanCloud.getContext().startService(IntentUtil.setupIntentFlags(i));
    } catch (Exception ex) {
      LOGGER.e("failed to startService. cause: " + ex.getMessage());
      return false;
    }
    return true;
  }

  // response notifier
  public void onOperationCompleted(String clientId, String conversationId, int requestId,
                                   Conversation.LCIMOperation operation, Throwable throwable) {
    if (LCIMOperation.CONVERSATION_QUERY == operation) {
      LCCallback callback = RequestCache.getInstance().getRequestCallback(clientId, null, requestId);
      if (null != callback) {
        // internal query conversation.
        callback.internalDone(null, LCIMException.wrapperException(throwable));
        RequestCache.getInstance().cleanRequestCallback(clientId, null, requestId);
        return;
      }
    }
    IntentUtil.sendIMLocalBroadcast(clientId, conversationId, requestId, throwable, operation);
  }

  public void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                                     Conversation.LCIMOperation operation, HashMap<String, Object> resultData) {
    if (LCIMOperation.CONVERSATION_QUERY == operation) {
      LCCallback callback = RequestCache.getInstance().getRequestCallback(clientId, null, requestId);
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
