package cn.leancloud.session;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.command.*;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.im.*;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.Conversation.AVIMOperation;
import cn.leancloud.session.AVIMOperationQueue.Operation;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AVSession {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVSession.class);
  public static final int REALTIME_TOKEN_WINDOW_INSECONDS = 300;

  static final int OPERATION_OPEN_SESSION = 10004;
  static final int OPERATION_CLOSE_SESSION = 10005;
  static final int OPERATION_UNKNOW = -1;

  private static final String ERROR_INVALID_SESSION_ID = "Null id in session id list.";
  private static final String CREATE_CONV = "create";

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

  /**
   * client id
   */
  private final String selfId;
  /**
   * client tag(optional)
   */
  private String tag;
  /**
   * AVUser session token(only for AVIMClient.open with AVUser)
   */
  private String userSessionToken = null;
  /**
   * RTM sessionToken(only available after AVIMClient.open)
   */
  private String realtimeSessionToken = null;
  /**
   * RTM sessionToken expired timestamp.
   */
  private long realtimeSessionTokenExpired = 0l;

  /**
   * last notified time.
   */
  private long lastNotifyTime = 0;
  /**
   * last patch time.
   */
  private long lastPatchTime = 0;

  public enum Status{
    Opened, Closed, Resuming
  }

  private volatile Status currentStatus = Status.Closed;

  // 标识是否需要从缓存恢复
  private final AtomicBoolean sessionResume = new AtomicBoolean(false);

  private final AtomicLong lastServerAckReceived = new AtomicLong(0);

  PendingMessageCache<PendingMessageCache.Message> pendingMessages;
  AVIMOperationQueue conversationOperationCache;
  private final ConcurrentMap<String, AVConversationHolder> conversationHolderCache =
          new ConcurrentHashMap<String, AVConversationHolder>();

  final AVSessionListener sessionListener;
  private final AVConnectionListener websocketListener;

  public AVConnectionListener getWebSocketListener() {
    return websocketListener;
  }

  public AVSession(String selfId, AVSessionListener sessionListener) {
    this.selfId = selfId;
    this.sessionListener = sessionListener;
    pendingMessages = new PendingMessageCache<PendingMessageCache.Message>(selfId, PendingMessageCache.Message.class);
    conversationOperationCache = new AVIMOperationQueue(selfId);
    this.websocketListener = new AVDefaultConnectionListener(this);
  }

  public boolean setSessionResume(boolean flag) {
    return this.sessionResume.getAndSet(flag);
  }

  public void setSessionStatus(Status curStatus) {
    this.currentStatus = curStatus;
  }

  public boolean isResume() {
    return this.sessionResume.get();
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public void open(final String clientTag, final String sessionToken, boolean isReconnection, final int requestId) {
    this.tag = clientTag;
    updateUserSessionToken(sessionToken);
    try {
      boolean connectionEstablished = AVConnectionManager.getInstance().isConnectionEstablished();
      if (!connectionEstablished) {
        sessionListener.onError(AVSession.this, new IllegalStateException(
                "Connection Lost"), OPERATION_OPEN_SESSION, requestId);
        return;
      }
      if (Status.Opened == currentStatus) {
        sessionListener.onSessionOpen(AVSession.this, requestId);
        return;
      }
      openWithSignature(requestId, isReconnection, true);
    } catch (Exception ex) {
      sessionListener.onError(AVSession.this, ex, OPERATION_OPEN_SESSION, requestId);
    }
  }

  void reopen() {
    String rtmSessionToken = AVSessionCacheHelper.IMSessionTokenCache.getIMSessionToken(getSelfPeerId());
    if (!StringUtil.isEmpty(rtmSessionToken)) {
      openWithSessionToken(rtmSessionToken);
    } else {
      int requestId = WindTalker.getNextIMRequestId();
      openWithSignature(requestId, true, false);
    }
  }

  public void renewRealtimeSesionToken(final int requestId) {
    final SignatureCallback callback = new SignatureCallback() {
      @Override
      public void onSignatureReady(Signature sig, AVException exception) {
        if (null != exception) {
          LOGGER.d("failed to generate signaure. cause:", exception);
        } else {
          SessionControlPacket scp = SessionControlPacket.genSessionCommand(
                  getSelfPeerId(), null,
                  SessionControlPacket.SessionControlOp.RENEW_RTMTOKEN, sig,
                  getLastNotifyTime(), getLastPatchTime(), requestId);
          scp.setTag(tag);
          scp.setSessionToken(realtimeSessionToken);
          AVConnectionManager.getInstance().sendPacket(scp);
        }
      }

      @Override
      public Signature computeSignature() throws SignatureFactory.SignatureException {
        SignatureFactory signatureFactory = AVIMOptions.getGlobalOptions().getSignatureFactory();
        if (null == signatureFactory && !StringUtil.isEmpty(getUserSessionToken())) {
          signatureFactory = new AVUserSignatureFactory(getUserSessionToken());
        }
        if (null != signatureFactory) {
          return signatureFactory.createSignature(getSelfPeerId(), new ArrayList<String>());
        }
        return null;
      }
    };
    new SignatureTask(callback, getSelfPeerId()).start();
  }

  void updateRealtimeSessionToken(String sessionToken, int expireInSec) {
    this.realtimeSessionToken = sessionToken;
    this.realtimeSessionTokenExpired = System.currentTimeMillis() + expireInSec * 1000;

    AVIMClient.getInstance(this.getSelfPeerId()).updateRealtimeSessionToken(sessionToken, this.realtimeSessionTokenExpired/1000);

    if (StringUtil.isEmpty(sessionToken)) {
      AVSessionCacheHelper.IMSessionTokenCache.removeIMSessionToken(getSelfPeerId());
    } else {
      AVSessionCacheHelper.IMSessionTokenCache.addIMSessionToken(getSelfPeerId(), sessionToken,
              realtimeSessionTokenExpired);
    }
  }

  public boolean realtimeSessionTokenExpired() {
    long now = System.currentTimeMillis()/1000;
    return (now + REALTIME_TOKEN_WINDOW_INSECONDS) >= this.realtimeSessionTokenExpired;
  };

  private void openWithSessionToken(String rtmSessionToken) {
//    SessionControlPacket scp = SessionControlPacket.genSessionCommand(
//            this.getSelfPeerId(), null, SessionControlPacket.SessionControlOp.OPEN,
//            null, this.getLastNotifyTime(), this.getLastPatchTime(), null);
//    scp.setSessionToken(rtmSessionToken);
//    scp.setReconnectionRequest(true);
//    scp.setAppId(AVOSCloud.getApplicationId());
    CommandPacket scp = WindTalker.getInstance().assembleSessionOpenPacket(this.getSelfPeerId(), this.tag, rtmSessionToken,
            this.getLastNotifyTime(), this.getLastPatchTime(), true, null);
    AVConnectionManager.getInstance().sendPacket(scp);
  }

  private void openWithSignature(final int requestId, final boolean reconnectionFlag,
                                 final boolean notifyListener) {
    final SignatureCallback callback = new SignatureCallback() {
      @Override
      public void onSignatureReady(Signature sig, AVException exception) {
        if (null != exception) {
          if (notifyListener) {
            sessionListener.onError(AVSession.this, exception,
                    OPERATION_OPEN_SESSION, requestId);
          }
          LOGGER.d("failed to generate signaure. cause:", exception);
        } else {
          conversationOperationCache.offer(AVIMOperationQueue.Operation.getOperation(
                  Conversation.AVIMOperation.CLIENT_OPEN.getCode(), getSelfPeerId(), null, requestId));
          CommandPacket scp = WindTalker.getInstance().assembleSessionOpenPacket(getSelfPeerId(), tag, sig, getLastNotifyTime(),
                  getLastPatchTime(), reconnectionFlag, requestId);
          AVConnectionManager.getInstance().sendPacket(scp);
        }
      }

      @Override
      public Signature computeSignature() throws SignatureFactory.SignatureException {
        SignatureFactory signatureFactory = AVIMOptions.getGlobalOptions().getSignatureFactory();
        if (null == signatureFactory && !StringUtil.isEmpty(getUserSessionToken())) {
          signatureFactory = new AVUserSignatureFactory(getUserSessionToken());
        }
        if (null != signatureFactory) {
          return signatureFactory.createSignature(getSelfPeerId(), new ArrayList<String>());
        }
        return null;
      }
    };
    new SignatureTask(callback, getSelfPeerId()).start();
  }

  public void close() {
    close(CommandPacket.UNSUPPORTED_OPERATION);
  }

  public void cleanUp() {
    updateRealtimeSessionToken("", 0);
    if (pendingMessages != null) {
      pendingMessages.clear();
    }
    if (conversationOperationCache != null) {
      this.conversationOperationCache.clear();
    }
    this.conversationHolderCache.clear();
    MessageReceiptCache.clean(this.getSelfPeerId());
  }

  public void close(int requestId) {
    try {
      // 都关掉了，我们需要去除Session记录
      AVSessionCacheHelper.getTagCacheInstance().removeSession(getSelfPeerId());
      AVSessionCacheHelper.IMSessionTokenCache.removeIMSessionToken(getSelfPeerId());

      // 如果session都已不在，缓存消息静静地等到桑田沧海
      this.cleanUp();

      if (Status.Closed == currentStatus) {
        this.sessionListener.onSessionClose(this, requestId);
        return;
      }
      if (AVConnectionManager.getInstance().isConnectionEstablished()) {
        conversationOperationCache.offer(Operation.getOperation(
                AVIMOperation.CLIENT_DISCONNECT.getCode(), selfId, null, requestId));
        CommandPacket scp = WindTalker.getInstance().assembleSessionPacket(this.selfId, null,
                SessionControlPacket.SessionControlOp.CLOSE, null, requestId);
        AVConnectionManager.getInstance().sendPacket(scp);
      } else {
        // 如果网络已经断开的时候，我们就不要管它了，直接强制关闭吧
        this.sessionListener.onSessionClose(this, requestId);
      }
    } catch (Exception e) {
      sessionListener.onError(this, e,
              OPERATION_CLOSE_SESSION, requestId);
    }
  }
  protected void storeMessage(PendingMessageCache.Message cacheMessage, int requestId) {
    pendingMessages.offer(cacheMessage);
    conversationOperationCache.offer(Operation.getOperation(
            AVIMOperation.CONVERSATION_SEND_MESSAGE.getCode(), getSelfPeerId(), cacheMessage.cid,
            requestId));
  }

  public String getSelfPeerId() {
    return this.selfId;
  }

  protected void setServerAckReceived(long lastAckReceivedTimestamp) {
    lastServerAckReceived.set(lastAckReceivedTimestamp);
  }

  public void queryOnlinePeers(List<String> peerIds, int requestId) {
    SessionControlPacket scp =
            SessionControlPacket.genSessionCommand(this.selfId, peerIds,
                    SessionControlPacket.SessionControlOp.QUERY, null, requestId);
    AVConnectionManager.getInstance().sendPacket(scp);
  }

  public void queryConversations(Map<String, Object> params, int requestId, String identifier) {
    if (Status.Closed == currentStatus) {
      RuntimeException se = new RuntimeException("Connection Lost");
      InternalConfiguration.getOperationTube().onOperationCompleted(getSelfPeerId(), null, requestId,
              AVIMOperation.CONVERSATION_QUERY, se);
      return;
    }

    // TODO: add request suppression.
    Operation op = Operation.getOperation(
            AVIMOperation.CONVERSATION_QUERY.getCode(), selfId, null, requestId);
    op.setIdentifier(identifier);

    conversationOperationCache.offer(op);
    if (!RequestStormSuppression.getInstance().postpone(op)) {
      LOGGER.d("[RequestSuppression] offer operation with requestId=" + requestId + ", selfId=" + selfId);
      ConversationQueryPacket packet = ConversationQueryPacket.getConversationQueryPacket(getSelfPeerId(),
              params, requestId);
      AVConnectionManager.getInstance().sendPacket(packet);
    } else {
      LOGGER.d("[RequestSuppression] other request is running, pending current request(requestId=" + requestId + ", selfId=" + selfId + ")" );
    }
  }

  public AVException checkSessionStatus() {
    if (Status.Closed == currentStatus) {
      return new AVException(AVException.OPERATION_FORBIDDEN,
              "Please call AVIMClient.open() first");
    } else if (Status.Resuming == currentStatus) {
      return new AVException(new RuntimeException("Connecting to server"));
    } else if (!AVConnectionManager.getInstance().isConnectionEstablished()) {
      return new AVException(new RuntimeException("Connection Lost"));
    } else {
      return null;
    }
  }

  public Status getCurrentStatus() {
    return this.currentStatus;
  }

  public AVConversationHolder getConversationHolder(String conversationId, int convType) {
    AVConversationHolder conversation = conversationHolderCache.get(conversationId);
    if (conversation != null) {
      return conversation;
    } else {
      conversation = new AVConversationHolder(conversationId, this, convType);
      AVConversationHolder elderObject =
              conversationHolderCache.putIfAbsent(conversationId, conversation);
      return elderObject == null ? conversation : elderObject;
    }
  }

  public void removeConversation(String conversationId) {
    conversationHolderCache.remove(conversationId);
  }

  public void createConversation(final List<String> members,
                                    final Map<String, Object> attributes,
                                    final boolean isTransient, final boolean isUnique, final boolean isTemp, final int tempTTL,
                                    final boolean isSystem, final int requestId) {
    if (!AVConnectionManager.getInstance().isConnectionEstablished()) {
      RuntimeException se = new RuntimeException("Connection Lost");
      sessionListener.onError(this, se, Conversation.AVIMOperation.CONVERSATION_CREATION.getCode(),
              requestId);
      return;
    }
    SignatureCallback callback = new SignatureCallback() {
      @Override
      public Signature computeSignature() throws SignatureFactory.SignatureException {
        SignatureFactory signatureFactory = AVIMOptions.getGlobalOptions().getSignatureFactory();
        if (signatureFactory != null) {
          return signatureFactory.createConversationSignature(null, AVSession.this.selfId, members, CREATE_CONV);
        }
        return null;
      }

      @Override
      public void onSignatureReady(Signature sig, AVException e) {
        if (e == null) {
          conversationOperationCache.offer(Operation.getOperation(
                  AVIMOperation.CONVERSATION_CREATION.getCode(), getSelfPeerId(), null, requestId));
          AVConnectionManager.getInstance().sendPacket(ConversationControlPacket.genConversationCommand(selfId, null,
                  members, ConversationControlPacket.ConversationControlOp.START, attributes, sig,
                  isTransient, isUnique, isTemp, tempTTL, isSystem, requestId));
        } else {
          InternalConfiguration.getOperationTube().onOperationCompleted(getSelfPeerId(), null, requestId,
                  AVIMOperation.CONVERSATION_CREATION, e);
        }
      }
    };
    new SignatureTask(callback, getSelfPeerId()).start();
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
      if (AVIMOptions.getGlobalOptions().isAlwaysRetrieveAllNotification()) {
        // DO NOT persist last notify timestamp.
      } else {
        AppConfiguration.getDefaultSetting().saveLong(selfId, LAST_NOTIFY_TIME, notifyTime);
      }
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
    updateLastPatchTime(patchTime, false);
  }

  void updateLastPatchTime(long patchTime, boolean force) {
    if (force) {
      lastPatchTime = patchTime;
      AppConfiguration.getDefaultSetting().saveLong(selfId, LAST_PATCH_TIME, patchTime);
    } else {
      long currentTime = getLastPatchTime();
      if (patchTime > currentTime) {
        lastPatchTime = patchTime;
        AppConfiguration.getDefaultSetting().saveLong(selfId, LAST_PATCH_TIME, patchTime);
      }
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
    if (AVIMOptions.getGlobalOptions().isOnlyPushCount() && null != messages && messages.size() > 0) {
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
