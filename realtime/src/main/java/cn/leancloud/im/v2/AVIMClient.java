package cn.leancloud.im.v2;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import cn.leancloud.callback.GenericObjectCallback;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.OperationTube;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.im.v2.conversation.AVIMConversationMemberInfo;
import cn.leancloud.query.QueryConditions;
import cn.leancloud.service.RealtimeClient;
import cn.leancloud.session.AVSession;
import cn.leancloud.session.AVSessionManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AVIMClient {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVIMClient.class);

  private static ConcurrentMap<String, AVIMClient> clients =
          new ConcurrentHashMap<String, AVIMClient>();
  private static AVIMClientEventHandler clientEventHandler;

  /**
   * 当前client的状态
   */
  public enum AVIMClientStatus {
    /**
     * 当前client尚未open，或者已经close
     */
    AVIMClientStatusNone(110),
    /**
     * 当前client已经打开，连接正常
     */
    AVIMClientStatusOpened(111),
    /**
     * 当前client由于网络因素导致的连接中断
     */
    AVIMClientStatusPaused(120);

    int code;

    AVIMClientStatus(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }

    public static AVIMClientStatus getClientStatus(int code) {
      switch (code) {
        case 110:
          return AVIMClientStatusNone;
        case 111:
          return AVIMClientStatusOpened;
        case 120:
          return AVIMClientStatusPaused;
        default:
          return null;
      }
    };
  }

  // client id
  private String clientId = null;
  // client tag
  private String tag = null;
  // AVUser authentication session token
  private String userSessionToken = null;
  // realtime session token
  private String realtimeSessionToken = null;
  // realtime session token expired timestamp.
  private long realtimeSessionTokenExpired = 0l;

  private AVIMMessageStorage storage;
  private ConcurrentMap<String, AVIMConversation> conversationCache =
          new ConcurrentHashMap<String, AVIMConversation>();

  private AVIMClient(String clientId) {
    this.clientId = clientId;
    this.storage = AVIMMessageStorage.getInstance(clientId);
  }

  /**
   * 设置AVIMClient的事件处理单元，
   *
   * 包括Client断开链接和重连成功事件
   *
   * @param handler event handler.
   */
  public static void setClientEventHandler(AVIMClientEventHandler handler) {
    AVIMClient.clientEventHandler = handler;
  }

  public static AVIMClientEventHandler getClientEventHandler() {
    return AVIMClient.clientEventHandler;
  }

  /**
   * get AVIMClient instance by clientId.
   * @param clientId client id.
   * @return imclient instance.
   */
  public static AVIMClient getInstance(String clientId) {
    if (StringUtil.isEmpty(clientId)) {
      return null;
    }
    AVIMClient client = clients.get(clientId);
    if (null == client) {
      client = new AVIMClient(clientId);
      AVIMClient elderClient = clients.putIfAbsent(clientId, client);
      if (null != elderClient) {
        client = elderClient;
      }
    }
    return client;
  }

  /**
   * count used clients.
   * @return current client count.
   */
  public static int getClientsCount() {
    return clients.size();
  }

  /**
   * get default clientId.
   * @return the default client id.
   */
  public static String getDefaultClient() {
    if (getClientsCount() == 1) {
      return clients.keySet().iterator().next();
    }
    return "";
  }

  /**
   * get AVIMClient instance by clientId and tag.
   * @param clientId client id.
   * @param tag optional tag.
   * @return  imclient instance.
   */
  public static AVIMClient getInstance(String clientId, String tag) {
    AVIMClient client = getInstance(clientId);
    client.tag = tag;
    return client;
  }

  /**
   * get AVIMClient instance by AVUser
   * @param user user instance.
   * @return imclient instance.
   */
  public static AVIMClient getInstance(AVUser user) {
    if (null == user) {
      return null;
    }
    String clientId = user.getObjectId();
    String sessionToken = user.getSessionToken();
    if (StringUtil.isEmpty(clientId) || StringUtil.isEmpty(sessionToken)) {
      return null;
    }
    AVIMClient client = getInstance(clientId);
    client.userSessionToken = sessionToken;
    return client;
  }
  public static AVIMClient getInstance(AVUser user, String tag) {
    AVIMClient client = getInstance(user);
    client.tag = tag;
    return client;
  }

  public void getClientStatus(final AVIMClientStatusCallback callback) {
    OperationTube operationTube = InternalConfiguration.getOperationTube();
    operationTube.queryClientStatus(this.clientId, callback);
  }

  /**
   * 获取当前用户的 clientId
   * @return 返回clientId
   */
  public String getClientId() {
    return this.clientId;
  }

  /**
   * Open client.
   * @param callback callback function.
   */
  public void open(final AVIMClientCallback callback) {
    this.open(null, callback);
  }

  /**
   * Open Client with options.
   *
   * @param option open option.
   * @param callback callback function.
   */
  public void open(AVIMClientOpenOption option, final AVIMClientCallback callback) {
    boolean reConnect = null == option? false : option.isReconnect();
    OperationTube operationTube = InternalConfiguration.getOperationTube();
    operationTube.openClient(clientId, tag, userSessionToken, reConnect, callback);
  }

  /**
   * Query online clients.
   *
   * @param clients client list.
   * @param callback callback function.
   */
  public void getOnlineClients(List<String> clients, final AVIMOnlineClientsCallback callback) {
    InternalConfiguration.getOperationTube().queryOnlineClients(this.clientId, clients, callback);
  }

  /**
   * Create a new Conversation
   *
   * @param conversationMembers member list.
   * @param attributes attribute map.
   * @param callback callback function.
   */
  public void createConversation(final List<String> conversationMembers,
                                 final Map<String, Object> attributes, final AVIMConversationCreatedCallback callback) {
    this.createConversation(conversationMembers, null, attributes, false, callback);
  }

  /**
   * Create a new Conversation
   *
   * @param conversationMembers member list.
   * @param name conversation name.
   * @param attributes attribute map.
   * @param callback callback function.
   */
  public void createConversation(final List<String> conversationMembers, String name,
                                 final Map<String, Object> attributes, final AVIMConversationCreatedCallback callback) {
    this.createConversation(conversationMembers, name, attributes, false, callback);
  }

  /**
   * Create a new Conversation
   *
   * @param members member list.
   * @param name conversation name.
   * @param attributes attribute map.
   * @param isTransient flag of transient.
   * @param callback callback function.
   */
  public void createConversation(final List<String> members, final String name, final Map<String, Object> attributes,
                                 final boolean isTransient, final AVIMConversationCreatedCallback callback) {
    this.createConversation(members, name, attributes, isTransient, !isTransient, callback);
  }

  /**
   * Create a new Conversation
   *
   * @param members member list.
   * @param name conversation name.
   * @param attributes attribute map.
   * @param isTransient flag of transient.
   * @param isUnique flag of unique.
   * @param callback callback function.
   */
  public void createConversation(final List<String> members, final String name, final Map<String, Object> attributes,
                                 final boolean isTransient, final boolean isUnique, final AVIMConversationCreatedCallback callback) {
    this.createConversation(members, name, attributes, isTransient, isUnique, false, 0, callback);
  }

  /**
   * Create a new temporary Conversation
   *
   * @param conversationMembers member list.
   * @param callback callback function.
   */
  public void createTemporaryConversation(final List<String> conversationMembers, final AVIMConversationCreatedCallback callback) {
    this.createTemporaryConversation(conversationMembers, 86400*3, callback);
  }

  /**
   * Create a new temporary Conversation
   *
   * @param conversationMembers member list.
   * @param ttl ttl value in seconds.
   * @param callback callback function.
   */
  public void createTemporaryConversation(final List<String> conversationMembers, int ttl, final AVIMConversationCreatedCallback callback) {
    this.createConversation(conversationMembers, null, null, false, true, true, ttl, callback);
  }

  /**
   * Create a new Chatroom
   *
   * @param conversationMembers member list.
   * @param name conversation name
   * @param attributes conversation attribute map.
   * @param isUnique deprecated chatroom is always not unique.
   * @param callback callback function.
   */
  public void createChatRoom(final List<String> conversationMembers, String name, final Map<String, Object> attributes,
                             final boolean isUnique, final AVIMConversationCreatedCallback callback) {
    this.createConversation(conversationMembers, name, attributes, true, false, callback);
  }

  /**
   * Create a new Chatroom
   * @param name conversation name
   * @param attributes conversation attribute map.
   * @param callback callback function.
   */
  public void createChatRoom(String name, final Map<String, Object> attributes, final AVIMConversationCreatedCallback callback) {
    this.createConversation(null, name, attributes, true, false, callback);
  }

  private void createServiceConversation(String name, final Map<String, Object> attributes,
                                         final AVIMConversationCreatedCallback callback) {
    throw new UnsupportedOperationException("can't invoke createServiceConversation within SDK.");
  }

  private void createConversation(final List<String> members, final String name,
                                  final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                  final boolean isTemp, int tempTTL, final AVIMConversationCreatedCallback callback) {
    final HashMap<String, Object> conversationAttributes = new HashMap<String, Object>();
    if (attributes != null) {
      conversationAttributes.putAll(attributes);
    }
    if (!StringUtil.isEmpty(name)) {
      conversationAttributes.put(Conversation.NAME, name);
    }
    Map<String, Object> assembledAttributes = null;
    if (conversationAttributes.size() > 0) {
      assembledAttributes = AVIMConversation.processAttributes(conversationAttributes, true);
    }
    final List<String> conversationMembers = new ArrayList<String>();
    if (null != members && members.size() > 0) {
      conversationMembers.addAll(members);
    }
    if (!conversationMembers.contains(clientId)) {
      conversationMembers.add(clientId);
    }
    final AVIMCommonJsonCallback middleCallback = new AVIMCommonJsonCallback() {
      @Override
      public void done(Map<String, Object> result, AVIMException e) {
        AVIMConversation conversation = null;
        if (null != result) {
          String conversationId =
                  (String) result.get(Conversation.callbackConversationKey);
          conversation = getConversation(conversationId, isTransient, isTemp);

          String createdAt = (String) result.get(Conversation.callbackCreatedAt);
          int tempTTLFromServer = 0;
          if (result.containsKey(Conversation.callbackTemporaryTTL)) {
            tempTTLFromServer = (int) result.get(Conversation.callbackTemporaryTTL);
          }
          if (result.containsKey(Conversation.callbackUniqueId)) {
            conversation.setUniqueId((String) result.get(Conversation.callbackUniqueId));
          }
          conversation.setMembers(conversationMembers);
          conversation.setAttributesForInit(conversationAttributes);
          conversation.setTransientForInit(isTransient);
          conversation.setConversationId(conversationId);
          conversation.setCreator(clientId);
          conversation.setCreatedAt(createdAt);
          conversation.setUpdatedAt(createdAt);
          conversation.setTemporary(isTemp);
          conversation.updateFetchTimestamp(System.currentTimeMillis());
          conversation.setTemporaryExpiredat(System.currentTimeMillis()/1000 + tempTTLFromServer);
          if (AVIMOptions.getGlobalOptions().isMessageQueryCacheEnabled()) {
            storage.insertConversations(Arrays.asList(conversation));
          }
        }
        if (null != callback) {
          callback.internalDone(conversation, AVIMException.wrapperAVException(e));
        }
      }
    };
    InternalConfiguration.getOperationTube().createConversation(getClientId(), conversationMembers, assembledAttributes,
            isTransient, isUnique, isTemp, tempTTL, middleCallback);
  }

  /**
   * get conversation by id
   *
   * @param conversationId  conversation id.
   * @return conversation instance.
   */
  public AVIMConversation getConversation(String conversationId) {
    if (StringUtil.isEmpty(conversationId)) {
      return null;
    }
    return this.getConversation(conversationId, false, conversationId.startsWith(Conversation.TEMPCONV_ID_PREFIX));
  }

  /**
   * get conversation by id and type
   *
   * @param conversationId  conversation id.
   * @param convType conversation type.
   * @return conversation instance.
   */
  public AVIMConversation getConversation(String conversationId, int convType) {
    AVIMConversation result = null;
    switch (convType) {
      case Conversation.CONV_TYPE_SYSTEM:
        result = getServiceConversation(conversationId);
        break;
      case Conversation.CONV_TYPE_TEMPORARY:
        result = getTemporaryConversation(conversationId);
        break;
      case Conversation.CONV_TYPE_TRANSIENT:
        result = getChatRoom(conversationId);
        break;
      default:
        result = getConversation(conversationId);
        break;
    }
    return result;
  }

  /**
   * get an existed conversation
   *
   * @param conversationId conversation id.
   * @param isTransient flag of transient.
   * @param isTemporary flag of temporary.
   * @return conversation instance.
   */
  public AVIMConversation getConversation(String conversationId, boolean isTransient, boolean isTemporary) {
    return this.getConversation(conversationId, isTransient, isTemporary,false);
  }

  /**
   * get an existed Chatroom by id
   * @param conversationId conversation id.
   * @return chatroom conversation instance.
   */
  public AVIMConversation getChatRoom(String conversationId) {
    return this.getConversation(conversationId, true, false);
  }

  /**
   * get an existed Service Conversation
   *
   * @param conversationId conversation id.
   * @return service conversation instance.
   */
  public AVIMConversation getServiceConversation(String conversationId) {
    return this.getConversation(conversationId, false, false, true);
  }

  /**
   * get an existed temporary conversation
   *
   * @param conversationId conversation id.
   * @return temporary conversation instance.
   */
  public AVIMConversation getTemporaryConversation(String conversationId) {
    return this.getConversation(conversationId, false, true);
  }

  private AVIMConversation getConversation(String conversationId, boolean isTransient, boolean isTemporary, boolean isSystem) {
    if (StringUtil.isEmpty(conversationId)) {
      return null;
    }
    AVIMConversation conversation = conversationCache.get(conversationId);
    if (null != conversation) {
      return conversation;
    } else {
      if (isSystem) {
        conversation = new AVIMServiceConversation(this, conversationId);
      } else if (isTemporary || conversationId.startsWith(Conversation.TEMPCONV_ID_PREFIX)) {
        conversation = new AVIMTemporaryConversation(this, conversationId);
      } else if (isTransient) {
        conversation = new AVIMChatRoom(this, conversationId);
      } else {
        conversation = new AVIMConversation(this, conversationId);
      }
      AVIMConversation elder = conversationCache.putIfAbsent(conversationId, conversation);
      return null == elder? conversation : elder;
    }
  }

  /**
   * 获取 AVIMConversationsQuery 对象，以此来查询 conversation
   * @return ConversationsQuery instance
   */
  public AVIMConversationsQuery getConversationsQuery() {
    return new AVIMConversationsQuery(this);
  }

  /**
   * 获取服务号的查询对象
   * 开发者拿到这个对象之后，就可以像 AVIMConversationsQuery 以前的接口一样对目标属性（如名字）等进行查询。
   * @return ConversationsQuery instance
   */
  public AVIMConversationsQuery getServiceConversationQuery() {
    AVIMConversationsQuery query = new AVIMConversationsQuery(this);
    query.whereEqualTo("sys", true);
    return query;
  }

  /**
   * 获取临时对话的查询对象
   * 开发者拿到这个对象之后，就可以像 AVIMConversationsQuery 以前的接口一样对目标属性（如名字）等进行查询。
   * @return ConversationsQuery instance
   */
  private AVIMConversationsQuery getTemporaryConversationQuery() {
    throw new UnsupportedOperationException("only conversationId query is allowed, please invoke #getTemporaryConversaton with conversationId.");
  }

  /**
   * 获取开放聊天室的查询对象
   * 开发者拿到这个对象之后，就可以像 AVIMConversationsQuery 以前的接口一样对目标属性（如名字）等进行查询。
   * @return ConversationsQuery instance
   */
  public AVIMConversationsQuery getChatRoomQuery() {
    AVIMConversationsQuery query = new AVIMConversationsQuery(this);
    query.whereEqualTo("tr", true);
    return query;
  }

  /**
   * close client.
   *
   * @param callback callback function.
   */
  public void close(final AVIMClientCallback callback) {
    final AVIMClientCallback internalCallback = new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null == e) {
          AVIMClient.this.localClose();
        }
        if (null != callback) {
          callback.done(client, e);
        }
      }
    };
    InternalConfiguration.getOperationTube().closeClient(this.clientId, internalCallback);
  }

  /**
   * [internal use only]
   * update realtime session token
   *
   * @param token session token
   * @param expiredInSec expired interval.
   */
  public void updateRealtimeSessionToken(String token, long expiredInSec) {
    this.realtimeSessionToken = token;
    this.realtimeSessionTokenExpired = expiredInSec;
  }

  private boolean realtimeSessionTokenExpired() {
    long now = System.currentTimeMillis()/1000;
    return (now + AVSession.REALTIME_TOKEN_WINDOW_INSECONDS) >= this.realtimeSessionTokenExpired;
  }

  AVIMConversation mergeConversationCache(AVIMConversation allNewConversation, boolean forceReplace, JSONObject deltaObject) {
    if (null == allNewConversation || StringUtil.isEmpty(allNewConversation.getConversationId())) {
      return null;
    }
    String convId = allNewConversation.getConversationId();
    if (forceReplace) {
      this.conversationCache.put(convId, allNewConversation);
      return allNewConversation;
    } else {
      AVIMConversation origin = this.conversationCache.get(convId);
      if (null == origin) {
        this.conversationCache.put(convId, allNewConversation);
        origin = allNewConversation;
      } else {
        // update cache object again.
        origin = AVIMConversation.updateConversation(origin, deltaObject);
      }
      return origin;
    }
  }

  void queryConversationMemberInfo(final QueryConditions queryConditions, final AVIMConversationMemberQueryCallback cb) {
    if (null == queryConditions || null == cb) {
      return;
    }
    if (!realtimeSessionTokenExpired()) {
      queryConvMemberThroughNetwork(queryConditions, cb);
    } else {
      // refresh realtime session token.
      LOGGER.d("realtime session token expired, start to refresh...");
      boolean ret = InternalConfiguration.getOperationTube().renewSessionToken(this.getClientId(), new AVIMClientCallback() {
        @Override
        public void done(AVIMClient client, AVIMException e) {
          if (null != e) {
            cb.internalDone(null, AVIMException.wrapperAVException(e));
          } else {
            queryConvMemberThroughNetwork(queryConditions, cb);
          }
        }
      });
      if (!ret) {
        cb.internalDone(null, new AVException(AVException.OPERATION_FORBIDDEN, "couldn't start service in background."));
      }
    }
  }


  private void queryConvMemberThroughNetwork(final QueryConditions queryConditions, final AVIMConversationMemberQueryCallback callback) {
    if (null == callback || null == queryConditions) {
      return;
    }
    queryConditions.assembleParameters();
    Map<String, String> queryParams = queryConditions.getParameters();
    queryParams.put("client_id", this.clientId);
    RealtimeClient.getInstance().queryMemberInfo(queryParams, this.realtimeSessionToken)
            .subscribe(new Observer<List<AVIMConversationMemberInfo>>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(List<AVIMConversationMemberInfo> avimConversationMemberInfos) {
        callback.internalDone(avimConversationMemberInfos, null);
      }

      @Override
      public void onError(Throwable throwable) {
        callback.internalDone(null, AVIMException.wrapperAVException(throwable));
      }

      @Override
      public void onComplete() {

      }
    });
  }

  protected void localClose() {
    clients.remove(this.clientId);
    conversationCache.clear();
    storage.deleteClientData();
  }

  AVIMMessageStorage getStorage() {
    return this.storage;
  }

}
