package cn.leancloud.im;

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
  }
}
