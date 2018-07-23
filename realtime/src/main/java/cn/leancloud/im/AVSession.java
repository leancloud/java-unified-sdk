package cn.leancloud.im;

import cn.leancloud.AVException;
import cn.leancloud.command.ConversationAckPacket;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AVSession {
  static final int OPERATION_OPEN_SESSION = 10004;
  static final int OPERATION_CLOSE_SESSION = 10005;
  static final int OPERATION_UNKNOW = -1;

  public static final String ERROR_INVALID_SESSION_ID = "Null id in session id list.";

  /**
   * 用于 read 的多端同步
   */
  private final String LAST_NOTIFY_TIME = "lastNotifyTime";

  /**
   * 用于 patch 的多端同步
   */
  private final String LAST_PATCH_TIME = "lastPatchTime";

  /**
   * 用于存储相关联的 AVUser 的 sessionToken
   */
  private final String AVUSER_SESSION_TOKEN = "avuserSessionToken";

  private final String selfId;
  String tag;
  private String userSessionToken = null;
  private String realtimeSessionToken = null;
  private long realtimeSessionTokenExpired = 0l;
  private long lastNotifyTime = 0;
  private long lastPatchTime = 0;

  final AtomicBoolean sessionOpened = new AtomicBoolean(false);
  final AtomicBoolean sessionPaused = new AtomicBoolean(false);
  // 标识是否需要从缓存恢复
  final AtomicBoolean sessionResume = new AtomicBoolean(false);

  private final AtomicLong lastServerAckReceived = new AtomicLong(0);

  PendingMessageCache<PendingMessageCache.Message> pendingMessages;
  AVIMOperationQueue conversationOperationCache;
  private final ConcurrentHashMap<String, AVConversationHolder> conversationHolderCache =
          new ConcurrentHashMap<String, AVConversationHolder>();

  final AVSessionListener sessionListener;
  private final AVConnectionListener websocketListener;

  /**
   * 离线消息推送模式
   * true 为仅推送数量，false 为推送具体消息
   */
  private static boolean onlyPushCount = false;

  public AVSession(String selfId, AVSessionListener sessionListener) {
    this.selfId = selfId;
    this.sessionListener = sessionListener;
    pendingMessages = new PendingMessageCache<PendingMessageCache.Message>(selfId, PendingMessageCache.Message.class);
    conversationOperationCache = new AVIMOperationQueue(selfId);
    this.websocketListener = new AVDefaultConnectionListener(this);
  }

  public void open(final String clientTag, final String sessionToken, boolean isReconnection, final int requestId) {
    ;
  }
  void reopen() {
    ;
  }
  public void renewRealtimeSesionToken(final int requestId) {
    ;
  }
  void updateRealtimeSessionToken(String sessionToken, int expireInSec) {
    ;
  }
  private void openWithSessionToken(String rtmSessionToken) {
    ;
  }
  private void openWithSignature(final int requestId, final boolean reconnectionFlag,
                                 final boolean notifyListener) {
    ;
  }
  public void close() {
    ;
  }
  public void cleanUp() {
    ;
  }
  protected void close(int requestId) {
    ;
  }
  protected void storeMessage(PendingMessageCache.Message cacheMessage, int requestId) {
    ;
  }
  public String getSelfPeerId() {
    return this.selfId;
  }
  protected void setServerAckReceived(long lastAckReceivedTimestamp) {
    ;
  }
  protected void queryOnlinePeers(List<String> peerIds, int requestId) {
    ;
  }
  protected void conversationQuery(Map<String, Object> params, int requestId) {
    ;
  }
  public AVException checkSessionStatus() {
    return null;
  }
  public AVConversationHolder getConversationHolder(String conversationId, int convType) {
    return null;
  }
  protected void removeConversation(String conversationId) {
    ;
  }
  protected void createConversation(final List<String> members,
                                    final Map<String, Object> attributes,
                                    final boolean isTransient, final boolean isUnique, final boolean isTemp, final int tempTTL,
                                    final boolean isSystem, final int requestId) {
    ;
  }

  public static void setUnreadNotificationEnabled(boolean isOnlyCount) {
    onlyPushCount = isOnlyCount;
  }

  /**
   * 是否被设置为离线消息仅推送数量
   * @return
   */
  public static boolean isOnlyPushCount() {
    return onlyPushCount;
  }

  long getLastNotifyTime() {
    if (lastNotifyTime <= 0) {
      lastNotifyTime = AppConfiguration.getDefaultSetting().getLong(selfId,
              LAST_NOTIFY_TIME, 0L);
    }
    return lastNotifyTime;
  }

  void updateLastNotifyTime(long notifyTime) {
    long currentTime = getLastNotifyTime();
    if (notifyTime > currentTime) {
      lastNotifyTime = notifyTime;
      AppConfiguration.getDefaultSetting().saveLong(selfId, LAST_NOTIFY_TIME, notifyTime);
    }
  }

  /**
   * 获取最后接收到 server patch 的时间
   * 按照业务需求，当本地没有缓存此数据时，返回最初始的客户端值
   * @return
   */
  long getLastPatchTime() {
    if (lastPatchTime <= 0) {
      lastPatchTime = AppConfiguration.getDefaultSetting().getLong(selfId,
              LAST_PATCH_TIME, 0L);
    }

    if (lastPatchTime <= 0) {
      lastPatchTime = System.currentTimeMillis();
      AppConfiguration.getDefaultSetting().saveLong(selfId, LAST_PATCH_TIME, lastPatchTime);
    }
    return lastPatchTime;
  }

  void updateLastPatchTime(long patchTime) {
    long currentTime = getLastPatchTime();
    if (patchTime > currentTime) {
      lastPatchTime = patchTime;
      AppConfiguration.getDefaultSetting().saveLong(selfId, LAST_PATCH_TIME, patchTime);
    }
  }

  String getUserSessionToken() {
    if (StringUtil.isEmpty(userSessionToken)) {
      userSessionToken = AppConfiguration.getDefaultSetting().getString(selfId,
              AVUSER_SESSION_TOKEN, "");
    }
    return userSessionToken;
  }

  void updateUserSessionToken(String token) {
    userSessionToken = token;
    if (!StringUtil.isEmpty(userSessionToken)) {
      AppConfiguration.getDefaultSetting().saveString(selfId, AVUSER_SESSION_TOKEN,
              userSessionToken);
    }
  }

  /**
   * 确认客户端已经拉取到未推送到本地的离线消息
   * 因为没有办法判断哪些消息是离线消息，所以对所有拉取到的消息都发送 ack
   * @param messages
   * @param conversationId
   */
  public void sendUnreadMessagesAck(ArrayList<AVIMMessage> messages, String conversationId) {
    if (onlyPushCount && null != messages && messages.size() > 0) {
      Long largestTimeStamp = 0L;
      for (AVIMMessage message : messages) {
        if (largestTimeStamp < message.getTimestamp()) {
          largestTimeStamp = message.getTimestamp();
        }
      }
      AVConnectionManager.getInstance().sendPacket(ConversationAckPacket.getConversationAckPacket(getSelfPeerId(),
              conversationId, largestTimeStamp));
    }
  }
}
