package cn.leancloud.session;

import cn.leancloud.LCLogger;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMClientEventHandler;
import cn.leancloud.im.v2.LCIMMessageManagerHelper;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.Conversation.AVIMOperation;
import cn.leancloud.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DefaultSessionListener extends LCSessionListener {
  private static final LCLogger LOGGER = LogUtil.getLogger(DefaultSessionListener.class);

  public DefaultSessionListener() {
  }

  @Override
  public void onSessionOpen(LCSession session, int requestId) {
    // 既然已经成功了，就往缓存里面添加一条记录
    SessionCacheHelper.getTagCacheInstance().addSession(session.getSelfPeerId(), session.getTag());
    // 这里需要给AVIMClient那边发一个LocalBoardcastMessage
    if (requestId > CommandPacket.UNSUPPORTED_OPERATION) {
      InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
              AVIMOperation.CLIENT_OPEN, null);
      broadcastSessionStatus(session, Conversation.STATUS_ON_CLIENT_ONLINE);
    } else {
      LOGGER.d("internal session open.");
      onSessionResumed(session);
    }
  }

  public void onSessionPaused(LCSession session) {
    broadcastSessionStatus(session, Conversation.STATUS_ON_CONNECTION_PAUSED);
  }

  @Override
  public void onSessionTokenRenewed(LCSession session, int requestId) {
    if (requestId > CommandPacket.UNSUPPORTED_OPERATION) {
      InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
              AVIMOperation.CLIENT_REFRESH_TOKEN, null);
    }
  }

  @Override
  public void onSessionResumed(LCSession session) {
    broadcastSessionStatus(session, Conversation.STATUS_ON_CONNECTION_RESUMED);
  }

  private void broadcastSessionStatus(LCSession session, int operation) {
    LCIMClientEventHandler handler = LCIMMessageManagerHelper.getClientEventHandler();
    if (handler != null) {
      handler.processEvent(operation, null, null, LCIMClient.getInstance(session.getSelfPeerId()));
    }
  }

  @Override
  public void onSessionClosedFromServer(LCSession session, int code) {
    cleanSession(session);
    LCIMClientEventHandler handler = LCIMMessageManagerHelper.getClientEventHandler();
    if (handler != null) {
      handler.processEvent(Conversation.STATUS_ON_CLIENT_OFFLINE, null, code,
              LCIMClient.getInstance(session.getSelfPeerId()));
    }
  }

  @Override
  public void onError(LCSession session, Throwable e, int sessionOperation,
                      int requestId) {
    LOGGER.e("session error:" + e);
    if (requestId > CommandPacket.UNSUPPORTED_OPERATION) {
      switch (sessionOperation) {
        case LCSession.OPERATION_OPEN_SESSION:
          InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
                  Conversation.AVIMOperation.CLIENT_OPEN, e);
          break;
        case LCSession.OPERATION_CLOSE_SESSION:
          InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
                  Conversation.AVIMOperation.CLIENT_DISCONNECT, e);
          break;
        default:
          break;
      }
      if (sessionOperation == AVIMOperation.CONVERSATION_CREATION.getCode()) {
        InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
                Conversation.AVIMOperation.CONVERSATION_CREATION, e);
      }
    }
  }

  @Override
  public void onSessionClose(LCSession session, int requestId) {
    LCSessionManager.getInstance().removeSession(session.getSelfPeerId());
    if (requestId > CommandPacket.UNSUPPORTED_OPERATION) {
      InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
              AVIMOperation.CLIENT_DISCONNECT, null);
    }
  }

  private void cleanSession(LCSession session) {
    SessionCacheHelper.getTagCacheInstance().removeSession(session.getSelfPeerId());
    session.setSessionStatus(LCSession.Status.Closed);
    // 如果session都已不在，缓存消息静静地等到桑田沧海
    session.cleanUp();
    LCSessionManager.getInstance().removeSession(session.getSelfPeerId());
  }

  @Override
  public void onOnlineQuery(LCSession session, List<String> onlinePeerIds,
                            int requestCode) {
    if (requestCode != CommandPacket.UNSUPPORTED_OPERATION) {
      HashMap<String, Object> bundle = new HashMap<>();
      bundle.put(Conversation.callbackOnlineClients, new ArrayList<String>(
              onlinePeerIds));
      InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), null, requestCode,
              AVIMOperation.CLIENT_ONLINE_QUERY, bundle);
    }
  }
}
