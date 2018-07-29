package cn.leancloud.im.v2;

import cn.leancloud.AVUser;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.OperationTube;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMClientStatusCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCreatedCallback;
import cn.leancloud.im.v2.callback.AVIMOnlineClientsCallback;
import cn.leancloud.utils.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AVIMClient {
  private static final int REALTIME_TOKEN_WINDOW_INSECONDS = 300;
  private static ConcurrentHashMap<String, AVIMClient> clients =
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

    static AVIMClientStatus getClientStatus(int code) {
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
  private ConcurrentHashMap<String, AVIMConversation> conversationCache =
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
   * @param handler
   */
  public static void setClientEventHandler(AVIMClientEventHandler handler) {
    AVIMClient.clientEventHandler = handler;
  }

  public static AVIMClientEventHandler getClientEventHandler() {
    return AVIMClient.clientEventHandler;
  }

  /**
   * get AVIMClient instance by clientId.
   * @param clientId
   * @return
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
   * @return
   */
  public static int getClientsCount() {
    return clients.size();
  }

  /**
   * get default clientId.
   * @return
   */
  public static String getDefaultClient() {
    if (getClientsCount() == 1) {
      return clients.keys().nextElement();
    }
    return "";
  }

  /**
   * get AVIMClient instance by clientId and tag.
   * @param clientId
   * @param tag
   * @return
   */
  public static AVIMClient getInstance(String clientId, String tag) {
    AVIMClient client = getInstance(clientId);
    client.tag = tag;
    return client;
  }

  /**
   * get AVIMClient instance by AVUser
   * @param user
   * @return
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

  public void open(final AVIMClientCallback callback) {
    this.open(null, callback);
  }

  public void open(AVIMClientOpenOption option, final AVIMClientCallback callback) {
    boolean reConnect = null == option? false : option.isReconnect();
    OperationTube operationTube = InternalConfiguration.getOperationTube();
    operationTube.openClient(clientId, tag, userSessionToken, reConnect, callback);
  }

  public void getOnlineClients(List<String> clients, final AVIMOnlineClientsCallback callback) {
    InternalConfiguration.getOperationTube().queryOnlineClients(this.clientId, clients, callback);
  }

  public void createConversation(final List<String> conversationMembers,
                                 final Map<String, Object> attributes, final AVIMConversationCreatedCallback callback) {
    this.createConversation(conversationMembers, null, attributes, false, callback);
  }

  public void createConversation(final List<String> conversationMembers, String name,
                                 final Map<String, Object> attributes, final AVIMConversationCreatedCallback callback) {
    this.createConversation(conversationMembers, name, attributes, false, callback);
  }

  public void createConversation(final List<String> members, final String name, final Map<String, Object> attributes,
                                 final boolean isTransient, final AVIMConversationCreatedCallback callback) {
    this.createConversation(members, name, attributes, isTransient, false, callback);
  }

  public void createConversation(final List<String> members, final String name, final Map<String, Object> attributes,
                                 final boolean isTransient, final boolean isUnique, final AVIMConversationCreatedCallback callback) {
    this.createConversation(members, name, attributes, isTransient, isUnique, false, 0, callback);
  }

  public void createTemporaryConversation(final List<String> conversationMembers, final AVIMConversationCreatedCallback callback) {
    this.createTemporaryConversation(conversationMembers, 86400*3, callback);
  }

  public void createTemporaryConversation(final List<String> conversationMembers, int ttl, final AVIMConversationCreatedCallback callback) {
    this.createConversation(conversationMembers, null, null, false, true, true, ttl, callback);
  }

  public void createChatRoom(final List<String> conversationMembers, String name, final Map<String, Object> attributes,
                             final boolean isUnique, final AVIMConversationCreatedCallback callback) {
    this.createConversation(conversationMembers, name, attributes, true, isUnique, callback);
  }

  private void createServiceConversation(String name, final Map<String, Object> attributes,
                                         final AVIMConversationCreatedCallback callback) {
    throw new UnsupportedOperationException("can't invoke createServiceConversation within SDK.");
  }

  private void createConversation(final List<String> members, final String name,
                                  final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                  final boolean isTemp, int tempTTL, final AVIMConversationCreatedCallback callback) {
    InternalConfiguration.getOperationTube().createConversation(members, name, attributes, isTransient, isUnique, isTemp, tempTTL, callback);
  }

  public AVIMConversation getConversation(String conversationId) {
    if (StringUtil.isEmpty(conversationId)) {
      return null;
    }
    return this.getConversation(conversationId, false, conversationId.startsWith(Conversation.TEMPCONV_ID_PREFIX));
  }

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

  public AVIMConversation getConversation(String conversationId, boolean isTransient, boolean isTemporary) {
    return this.getConversation(conversationId, isTransient, isTemporary,false);
  }

  public AVIMConversation getChatRoom(String conversationId) {
    return this.getConversation(conversationId, true, false);
  }

  public AVIMConversation getServiceConversation(String conversationId) {
    return this.getConversation(conversationId, false, false, true);
  }

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
   * @return
   */
  public AVIMConversationsQuery getConversationsQuery() {
    return new AVIMConversationsQuery(this);
  }

  /**
   * 获取服务号的查询对象
   * 开发者拿到这个对象之后，就可以像 AVIMConversationsQuery 以前的接口一样对目标属性（如名字）等进行查询。
   * @return
   */
  public AVIMConversationsQuery getServiceConversationQuery() {
    AVIMConversationsQuery query = new AVIMConversationsQuery(this);
    query.whereEqualTo("sys", true);
    return query;
  }

  /**
   * 获取临时对话的查询对象
   * 开发者拿到这个对象之后，就可以像 AVIMConversationsQuery 以前的接口一样对目标属性（如名字）等进行查询。
   * @return
   */
  private AVIMConversationsQuery getTemporaryConversationQuery() {
    throw new UnsupportedOperationException("only conversationId query is allowed, please invoke #getTemporaryConversaton with conversationId.");
  }

  /**
   * 获取开放聊天室的查询对象
   * 开发者拿到这个对象之后，就可以像 AVIMConversationsQuery 以前的接口一样对目标属性（如名字）等进行查询。
   * @return
   */
  public AVIMConversationsQuery getChatRoomQuery() {
    AVIMConversationsQuery query = new AVIMConversationsQuery(this);
    query.whereEqualTo("tr", true);
    return query;
  }

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

  protected void localClose() {
    clients.remove(this.clientId);
    conversationCache.clear();
    storage.deleteClientData();
  }

  AVIMMessageStorage getStorage() {
    return this.storage;
  }

}
