package cn.leancloud.session;

import cn.leancloud.AVLogger;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMClientEventHandler;
import cn.leancloud.im.v2.AVIMMessageManagerHelper;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.Conversation.AVIMOperation;
import cn.leancloud.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AVDefaultSessionListener extends AVSessionListener {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVDefaultSessionListener.class);

  public AVDefaultSessionListener() {
  }

  @Override
  public void onSessionOpen(AVSession session, int requestId) {
    // 既然已经成功了，就往缓存里面添加一条记录
    AVSessionCacheHelper.getTagCacheInstance().addSession(session.getSelfPeerId(), session.getTag());
    // 这里需要给AVIMClient那边发一个LocalBoardcastMessage
    if (requestId > CommandPacket.UNSUPPORTED_OPERATION) {
      InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
              AVIMOperation.CLIENT_OPEN, null);
    } else {
      LOGGER.d("internal session open.");
      onSessionResumed(session);
    }
  }

  public void onSessionPaused(AVSession session) {
    AVIMClientEventHandler handler = AVIMMessageManagerHelper.getClientEventHandler();
    if (handler != null) {
      handler.processEvent(Conversation.STATUS_ON_CONNECTION_PAUSED, null, null,
              AVIMClient.getInstance(session.getSelfPeerId()));
    }
  }

  @Override
  public void onSessionTokenRenewed(AVSession session, int requestId) {
    if (requestId > CommandPacket.UNSUPPORTED_OPERATION) {
      InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
              AVIMOperation.CLIENT_REFRESH_TOKEN, null);
    }
  }

  @Override
  public void onSessionResumed(AVSession session) {
    AVIMClientEventHandler handler = AVIMMessageManagerHelper.getClientEventHandler();
    if (handler != null) {
      handler.processEvent(Conversation.STATUS_ON_CONNECTION_RESUMED, null, null,
              AVIMClient.getInstance(session.getSelfPeerId()));
    }
  }

  @Override
  public void onSessionClosedFromServer(AVSession session, int code) {
    cleanSession(session);
    AVIMClientEventHandler handler = AVIMMessageManagerHelper.getClientEventHandler();
    if (handler != null) {
      handler.processEvent(Conversation.STATUS_ON_CLIENT_OFFLINE, null, code,
              AVIMClient.getInstance(session.getSelfPeerId()));
    }
  }

  @Override
  public void onError(AVSession session, Throwable e, int sessionOperation,
                      int requestId) {
    LOGGER.e("session error:" + e);
    if (requestId > CommandPacket.UNSUPPORTED_OPERATION) {
      switch (sessionOperation) {
        case AVSession.OPERATION_OPEN_SESSION:
          InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
                  Conversation.AVIMOperation.CLIENT_OPEN, e);
          break;
        case AVSession.OPERATION_CLOSE_SESSION:
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
  public void onSessionClose(AVSession session, int requestId) {
    AVSessionManager.getInstance().removeSession(session.getSelfPeerId());
    if (requestId > CommandPacket.UNSUPPORTED_OPERATION) {
      InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestId,
              AVIMOperation.CLIENT_DISCONNECT, null);
    }
  }

  private void cleanSession(AVSession session) {
    AVSessionCacheHelper.getTagCacheInstance().removeSession(session.getSelfPeerId());
    session.setSessionStatus(AVSession.Status.Closed);
    // 如果session都已不在，缓存消息静静地等到桑田沧海
    session.cleanUp();
    AVSessionManager.getInstance().removeSession(session.getSelfPeerId());
  }

  @Override
  public void onOnlineQuery(AVSession session, List<String> onlinePeerIds,
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
