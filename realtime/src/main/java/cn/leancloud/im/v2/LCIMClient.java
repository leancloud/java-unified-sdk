package cn.leancloud.im.v2;

import cn.leancloud.LCException;
import cn.leancloud.LCInstallation;
import cn.leancloud.LCLogger;
import cn.leancloud.LCUser;
import cn.leancloud.im.LCIMOptions;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.OperationTube;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.im.v2.conversation.LCIMConversationMemberInfo;
import cn.leancloud.query.QueryConditions;
import cn.leancloud.service.RealtimeClient;
import cn.leancloud.session.LCConnectionManager;
import cn.leancloud.session.LCSession;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LCIMClient {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCIMClient.class);

  private static ConcurrentMap<String, LCIMClient> clients =
          new ConcurrentHashMap<String, LCIMClient>();
  private static LCIMClientEventHandler clientEventHandler;

  /**
   * 当前client的状态
   */
  public enum LCIMClientStatus {
    /**
     * 当前client尚未open，或者已经close
     */
    LCIMClientStatusNone(110),
    /**
     * 当前client已经打开，连接正常
     */
    LCIMClientStatusOpened(111),
    /**
     * 当前client由于网络因素导致的连接中断
     */
    LCIMClientStatusPaused(120);

    int code;

    LCIMClientStatus(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }

    public static LCIMClientStatus getClientStatus(int code) {
      switch (code) {
        case 110:
          return LCIMClientStatusNone;
        case 111:
          return LCIMClientStatusOpened;
        case 120:
          return LCIMClientStatusPaused;
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

  private LCIMMessageStorage storage;
  private ConcurrentMap<String, LCIMConversation> conversationCache =
          new ConcurrentHashMap<String, LCIMConversation>();

  private LCConnectionManager connectionManager;
  private LCInstallation currentInstallation;

  private LCIMClient(LCConnectionManager connectionManager, String clientId, LCInstallation installation) {
    this.clientId = clientId;
    this.storage = LCIMMessageStorage.getInstance(clientId);
    this.connectionManager = connectionManager;
    this.currentInstallation = installation;
  }

  /**
   * 设置AVIMClient的事件处理单元，
   *
   * 包括Client断开链接和重连成功事件
   *
   * @param handler event handler.
   */
  public static void setClientEventHandler(LCIMClientEventHandler handler) {
    LCIMClient.clientEventHandler = handler;
  }

  public static LCIMClientEventHandler getClientEventHandler() {
    return LCIMClient.clientEventHandler;
  }

  LCConnectionManager getConnectionManager() {
    return this.connectionManager;
  }

  public String getInstallationId() {
    if (null == currentInstallation) {
      return null;
    }
    return currentInstallation.getInstallationId();
  }

  /**
   * get AVIMClient instance by clientId.
   * @param clientId client id.
   * @return imclient instance.
   */
  public static LCIMClient getInstance(String clientId) {
    return getInstance(LCConnectionManager.getInstance(), clientId, LCInstallation.getCurrentInstallation());
  }

  /**
   * peek AVIMClient instance by clientId.
   * @param clientId client id.
   * @return imclient instance, NULL if not existed.
   */
  public static LCIMClient peekInstance(String clientId) {
    if (StringUtil.isEmpty(clientId)) {
      return null;
    }
    return clients.get(clientId);
  }

  /**
   * get AVIMClient instance by clientId.
   * @param connectionManager  connection manager.
   * @param clientId client id.
   * @param installation installation id.
   * @return imclient instance.
   */
  public static LCIMClient getInstance(LCConnectionManager connectionManager, String clientId, LCInstallation installation) {
    if (StringUtil.isEmpty(clientId)) {
      return null;
    }
    LCIMClient client = clients.get(clientId);
    if (null == client) {
      client = new LCIMClient(connectionManager, clientId, installation);
      LCIMClient elderClient = clients.putIfAbsent(clientId, client);
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
  public static LCIMClient getInstance(String clientId, String tag) {
    LCIMClient client = getInstance(clientId);
    client.tag = tag;
    return client;
  }

  /**
   * get AVIMClient instance by AVUser
   * @param user user instance.
   * @return imclient instance.
   */
  public static LCIMClient getInstance(LCUser user) {
    if (null == user) {
      return null;
    }
    String clientId = user.getObjectId();
    String sessionToken = user.getSessionToken();
    if (StringUtil.isEmpty(clientId) || StringUtil.isEmpty(sessionToken)) {
      return null;
    }
    LCIMClient client = getInstance(clientId);
    client.userSessionToken = sessionToken;
    return client;
  }

  /**
   * get AVIMClient instance by AVUser
   * @param user user instance.
   * @param tag client tag.
   * @return imclient instance.
   */
  public static LCIMClient getInstance(LCUser user, String tag) {
    LCIMClient client = getInstance(user);
    client.tag = tag;
    return client;
  }

  public void getClientStatus(final LCIMClientStatusCallback callback) {
    OperationTube operationTube = InternalConfiguration.getOperationTube();
    operationTube.queryClientStatus(this.connectionManager, this.clientId, callback);
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
  public void open(final LCIMClientCallback callback) {
    this.open(null, callback);
  }

  /**
   * Open Client with options.
   *
   * @param option open option.
   * @param callback callback function.
   */
  public void open(LCIMClientOpenOption option, final LCIMClientCallback callback) {
    boolean reConnect = null == option? false : option.isReconnect();
    OperationTube operationTube = InternalConfiguration.getOperationTube();
    operationTube.openClient(this.connectionManager, clientId, tag, userSessionToken, reConnect, callback);
  }

  /**
   * Query online clients.
   *
   * @param clients client list.
   * @param callback callback function.
   */
  public void getOnlineClients(List<String> clients, final LCIMOnlineClientsCallback callback) {
    InternalConfiguration.getOperationTube().queryOnlineClients(this.connectionManager, this.clientId, clients, callback);
  }

  /**
   * Create a new Conversation
   *
   * @param conversationMembers member list.
   * @param attributes attribute map.
   * @param callback callback function.
   */
  public void createConversation(final List<String> conversationMembers,
                                 final Map<String, Object> attributes, final LCIMConversationCreatedCallback callback) {
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
                                 final Map<String, Object> attributes, final LCIMConversationCreatedCallback callback) {
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
                                 final boolean isTransient, final LCIMConversationCreatedCallback callback) {
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
                                 final boolean isTransient, final boolean isUnique, final LCIMConversationCreatedCallback callback) {
    this.createConversation(members, name, attributes, isTransient, isUnique, false, 0, callback);
  }

  /**
   * Create a new temporary Conversation
   *
   * @param conversationMembers member list.
   * @param callback callback function.
   */
  public void createTemporaryConversation(final List<String> conversationMembers, final LCIMConversationCreatedCallback callback) {
    this.createTemporaryConversation(conversationMembers, 86400*3, callback);
  }

  /**
   * Create a new temporary Conversation
   *
   * @param conversationMembers member list.
   * @param ttl ttl value in seconds.
   * @param callback callback function.
   */
  public void createTemporaryConversation(final List<String> conversationMembers, int ttl, final LCIMConversationCreatedCallback callback) {
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
                             final boolean isUnique, final LCIMConversationCreatedCallback callback) {
    this.createConversation(conversationMembers, name, attributes, true, false, callback);
  }

  /**
   * Create a new Chatroom
   * @param name conversation name
   * @param attributes conversation attribute map.
   * @param callback callback function.
   */
  public void createChatRoom(String name, final Map<String, Object> attributes, final LCIMConversationCreatedCallback callback) {
    this.createConversation(null, name, attributes, true, false, callback);
  }

  private void createServiceConversation(String name, final Map<String, Object> attributes,
                                         final LCIMConversationCreatedCallback callback) {
    throw new UnsupportedOperationException("can't invoke createServiceConversation within SDK.");
  }

  private void createConversation(final List<String> members, final String name,
                                  final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                  final boolean isTemp, final int tempTTL, final LCIMConversationCreatedCallback callback) {
    final HashMap<String, Object> conversationAttributes = new HashMap<String, Object>();
    if (attributes != null) {
      conversationAttributes.putAll(attributes);
    }
    if (!StringUtil.isEmpty(name)) {
      conversationAttributes.put(Conversation.NAME, name);
    }
    Map<String, Object> assembledAttributes = null;
    if (conversationAttributes.size() > 0) {
      assembledAttributes = LCIMConversation.processAttributes(conversationAttributes, true);
    }
    final List<String> conversationMembers = new ArrayList<String>();
    if (null != members && members.size() > 0) {
      conversationMembers.addAll(members);
    }
    if (!conversationMembers.contains(clientId)) {
      conversationMembers.add(clientId);
    }
    final LCIMCommonJsonCallback middleCallback = new LCIMCommonJsonCallback() {
      @Override
      public void done(Map<String, Object> result, LCIMException e) {
        LCIMConversation conversation = null;
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
          conversation.setAttributesForInit(attributes);
          conversation.setNameForInit(name);
          conversation.setTransientForInit(isTransient);
          conversation.setConversationId(conversationId);
          conversation.setCreator(clientId);
          conversation.setCreatedAt(createdAt);
          conversation.setUpdatedAt(createdAt);
          conversation.setTemporary(isTemp);
          conversation.setTemporaryExpiredat(tempTTL);
          conversation.updateFetchTimestamp(System.currentTimeMillis());
          conversation.setTemporaryExpiredat(System.currentTimeMillis()/1000 + tempTTLFromServer);
          if (LCIMOptions.getGlobalOptions().isMessageQueryCacheEnabled()) {
            storage.insertConversations(Arrays.asList(conversation));
          }
        }
        if (null != callback) {
          callback.internalDone(conversation, LCIMException.wrapperException(e));
        }
      }
    };
    InternalConfiguration.getOperationTube().createConversation(this.connectionManager,
            getClientId(), conversationMembers, assembledAttributes,
            isTransient, isUnique, isTemp, tempTTL, middleCallback);
  }

  /**
   * get conversation by id
   *
   * @param conversationId  conversation id.
   * @return conversation instance.
   */
  public LCIMConversation getConversation(String conversationId) {
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
  public LCIMConversation getConversation(String conversationId, int convType) {
    LCIMConversation result = null;
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
  public LCIMConversation getConversation(String conversationId, boolean isTransient, boolean isTemporary) {
    return this.getConversation(conversationId, isTransient, isTemporary,false);
  }

  /**
   * get an existed Chatroom by id
   * @param conversationId conversation id.
   * @return chatroom conversation instance.
   */
  public LCIMConversation getChatRoom(String conversationId) {
    return this.getConversation(conversationId, true, false);
  }

  /**
   * get an existed Service Conversation
   *
   * @param conversationId conversation id.
   * @return service conversation instance.
   */
  public LCIMConversation getServiceConversation(String conversationId) {
    return this.getConversation(conversationId, false, false, true);
  }

  /**
   * get an existed temporary conversation
   *
   * @param conversationId conversation id.
   * @return temporary conversation instance.
   */
  public LCIMConversation getTemporaryConversation(String conversationId) {
    return this.getConversation(conversationId, false, true);
  }

  private LCIMConversation getConversation(String conversationId, boolean isTransient, boolean isTemporary, boolean isSystem) {
    if (StringUtil.isEmpty(conversationId)) {
      return null;
    }
    LCIMConversation conversation = conversationCache.get(conversationId);
    if (null != conversation) {
      return conversation;
    } else {
      if (isSystem) {
        conversation = new LCIMServiceConversation(this, conversationId);
      } else if (isTemporary || conversationId.startsWith(Conversation.TEMPCONV_ID_PREFIX)) {
        conversation = new LCIMTemporaryConversation(this, conversationId);
      } else if (isTransient) {
        conversation = new LCIMChatRoom(this, conversationId);
      } else {
        conversation = new LCIMConversation(this, conversationId);
      }
      LCIMConversation elder = conversationCache.putIfAbsent(conversationId, conversation);
      return null == elder? conversation : elder;
    }
  }

  /**
   * 获取 AVIMConversationsQuery 对象，以此来查询 conversation
   * @return ConversationsQuery instance
   */
  public LCIMConversationsQuery getConversationsQuery() {
    return new LCIMConversationsQuery(this);
  }

  /**
   * 获取服务号的查询对象
   * 开发者拿到这个对象之后，就可以像 AVIMConversationsQuery 以前的接口一样对目标属性（如名字）等进行查询。
   * @return ConversationsQuery instance
   */
  public LCIMConversationsQuery getServiceConversationQuery() {
    LCIMConversationsQuery query = new LCIMConversationsQuery(this);
    query.whereEqualTo("sys", true);
    return query;
  }

  /**
   * 获取临时对话的查询对象
   * 开发者拿到这个对象之后，就可以像 AVIMConversationsQuery 以前的接口一样对目标属性（如名字）等进行查询。
   * @return ConversationsQuery instance
   */
  private LCIMConversationsQuery getTemporaryConversationQuery() {
    throw new UnsupportedOperationException("only conversationId query is allowed, please invoke #getTemporaryConversaton with conversationId.");
  }

  /**
   * 获取开放聊天室的查询对象
   * 开发者拿到这个对象之后，就可以像 AVIMConversationsQuery 以前的接口一样对目标属性（如名字）等进行查询。
   * @return ConversationsQuery instance
   */
  public LCIMConversationsQuery getChatRoomQuery() {
    LCIMConversationsQuery query = new LCIMConversationsQuery(this);
    query.whereEqualTo("tr", true);
    return query;
  }

  /**
   * close client.
   *
   * @param callback callback function.
   */
  public void close(final LCIMClientCallback callback) {
    final LCIMClientCallback internalCallback = new LCIMClientCallback() {
      @Override
      public void done(LCIMClient client, LCIMException e) {
        if (null == e) {
          LCIMClient.this.localClose();
        }
        if (null != callback) {
          callback.done(client, e);
        }
      }
    };
    InternalConfiguration.getOperationTube().closeClient(this.connectionManager, this.clientId, internalCallback);
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
    return (now + LCSession.REALTIME_TOKEN_WINDOW_INSECONDS) >= this.realtimeSessionTokenExpired;
  }


  LCIMConversation mergeConversationCache(LCIMConversation allNewConversation, boolean forceReplace, Map<String, Object> deltaObject) {
    if (null == allNewConversation || StringUtil.isEmpty(allNewConversation.getConversationId())) {
      return null;
    }
    String convId = allNewConversation.getConversationId();
    if (forceReplace) {
      this.conversationCache.put(convId, allNewConversation);
      return allNewConversation;
    } else {
      LCIMConversation origin = this.conversationCache.get(convId);
      if (null == origin) {
        this.conversationCache.put(convId, allNewConversation);
        origin = allNewConversation;
      } else {
        // update cache object again.
        origin = LCIMConversation.updateConversation(origin, deltaObject);
      }
      return origin;
    }
  }

//  AVIMConversation mergeConversationCache(AVIMConversation allNewConversation, boolean forceReplace, JSONObject deltaObject) {
//    if (null == allNewConversation || StringUtil.isEmpty(allNewConversation.getConversationId())) {
//      return null;
//    }
//    String convId = allNewConversation.getConversationId();
//    if (forceReplace) {
//      this.conversationCache.put(convId, allNewConversation);
//      return allNewConversation;
//    } else {
//      AVIMConversation origin = this.conversationCache.get(convId);
//      if (null == origin) {
//        this.conversationCache.put(convId, allNewConversation);
//        origin = allNewConversation;
//      } else {
//        // update cache object again.
//        origin = AVIMConversation.updateConversation(origin, deltaObject);
//      }
//      return origin;
//    }
//  }

  void queryConversationMemberInfo(final QueryConditions queryConditions, final LCIMConversationMemberQueryCallback cb) {
    if (null == queryConditions || null == cb) {
      return;
    }
    if (!realtimeSessionTokenExpired()) {
      queryConvMemberThroughNetwork(queryConditions, cb);
    } else {
      // refresh realtime session token.
      LOGGER.d("realtime session token expired, start to refresh...");
      boolean ret = InternalConfiguration.getOperationTube().renewSessionToken(this.connectionManager, this.getClientId(), new LCIMClientCallback() {
        @Override
        public void done(LCIMClient client, LCIMException e) {
          if (null != e) {
            cb.internalDone(null, LCIMException.wrapperException(e));
          } else {
            queryConvMemberThroughNetwork(queryConditions, cb);
          }
        }
      });
      if (!ret) {
        cb.internalDone(null, new LCException(LCException.OPERATION_FORBIDDEN, "couldn't start service in background."));
      }
    }
  }


  private void queryConvMemberThroughNetwork(final QueryConditions queryConditions, final LCIMConversationMemberQueryCallback callback) {
    if (null == callback || null == queryConditions) {
      return;
    }
    queryConditions.assembleParameters();
    Map<String, String> queryParams = queryConditions.getParameters();
    queryParams.put("client_id", this.clientId);
    RealtimeClient.getInstance().queryMemberInfo(queryParams, this.realtimeSessionToken)
            .subscribe(new Observer<List<LCIMConversationMemberInfo>>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(List<LCIMConversationMemberInfo> LCIMConversationMemberInfos) {
        callback.internalDone(LCIMConversationMemberInfos, null);
      }

      @Override
      public void onError(Throwable throwable) {
        callback.internalDone(null, LCIMException.wrapperException(throwable));
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

  LCIMMessageStorage getStorage() {
    return this.storage;
  }

}
