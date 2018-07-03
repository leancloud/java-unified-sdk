package cn.leancloud.im.v2;

import cn.leancloud.AVUser;
import cn.leancloud.utils.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AVIMClient {
  private static final int REALTIME_TOKEN_WINDOW_INSECONDS = 300;
  static ConcurrentHashMap<String, AVIMClient> clients =
          new ConcurrentHashMap<String, AVIMClient>();
  private String clientId = null;
  private String tag = null;
  private String userSessionToken = null;
  private String realtimeSessionToken = null;
  private long realtimeSessionTokenExpired = 0l;
  private boolean isAutoOpen = true;

  private AVIMClient(String clientId) {
    this.clientId = clientId;
  }
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
  public static int getClientsCount() {
    return clients.size();
  }
  public static String getDefaultClient() {
    if (getClientsCount() == 1) {
      return clients.keys().nextElement();
    }
    return "";
  }
  public static AVIMClient getInstance(String clientId, String tag) {
    AVIMClient client = getInstance(clientId);
    client.tag = tag;
    return client;
  }
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

  public void open() {
    ;
  }
  public void open(AVIMClientOpenOption option) {
    ;
  }

  public void getOnlineClients(List<String> clients) {
    ;
  }

  public void createConversation(final List<String> conversationMembers,
                                 final Map<String, Object> attributes) {
    ;
  }

  public void createConversation(final List<String> conversationMembers, String name,
                                 final Map<String, Object> attributes) {
    ;
  }

  public void createConversation(final List<String> members, final String name,
                                 final Map<String, Object> attributes, final boolean isTransient) {
    ;
  }

  public void createConversation(final List<String> members, final String name,
                                 final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique) {
    ;
  }

  public void createTemporaryConversation(final List<String> conversationMembers) {
    ;
  }

  public void createTemporaryConversation(final List<String> conversationMembers, int ttl) {
    ;
  }

  public void createChatRoom(final List<String> conversationMembers, String name,
                             final Map<String, Object> attributes, final boolean isUnique) {
    ;
  }

  public AVIMConversation getConversation(String conversationId) {
    return null;
  }

  public AVIMConversation getConversation(String conversationId, int convType) {
    return null;
  }

  public AVIMConversation getConversation(String conversationId, boolean isTransient, boolean isTemporary) {
    return null;
  }

  public AVIMConversation getChatRoom(String conversationId) {
    return null;
  }

  public AVIMConversation getServiceConversation(String conversationId) {
    return null;
  }

  public AVIMConversation getTemporaryConversation(String conversationId) {
    return null;
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

  public void close() {
    ;
  }

  /**
   * 获取当前用户的 clientId
   * @return 返回clientId
   */
  public String getClientId() {
    return this.clientId;
  }

  public void updateRealtimeSessionToken(String sessionToken, long expireInSec) {
    this.realtimeSessionToken = sessionToken;
    this.realtimeSessionTokenExpired = expireInSec;
  }

  public String getRealtimeSessionToken() {
    return this.realtimeSessionToken;
  }

  boolean realtimeSessionTokenExpired() {
    long now = System.currentTimeMillis()/1000;
    return (now + REALTIME_TOKEN_WINDOW_INSECONDS) >= this.realtimeSessionTokenExpired;
  }

}
