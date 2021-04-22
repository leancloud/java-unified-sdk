package cn.leancloud.livequery;

import cn.leancloud.im.WindTalker;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.session.LCConnectionManager;
import cn.leancloud.session.IMOperationQueue;

public class LiveQueryOperationDelegate {
  private static final LiveQueryOperationDelegate instance = new LiveQueryOperationDelegate();

  public static final String LIVEQUERY_DEFAULT_ID = "leancloud_livequery_default_id";

  public static LiveQueryOperationDelegate getInstance() {
    return instance;
  }

  IMOperationQueue operationCache;
  private LiveQueryOperationDelegate() {
    operationCache = new IMOperationQueue(LIVEQUERY_DEFAULT_ID);
  }

  public boolean login(String subscriptionId, int requestId) {
    // FIXME: no timeout timer for login request.
    operationCache.offer(IMOperationQueue.Operation.getOperation(
            Conversation.AVIMOperation.LIVEQUERY_LOGIN.getCode(), LIVEQUERY_DEFAULT_ID, null, requestId));
    LCConnectionManager.getInstance().sendPacket(WindTalker.getInstance().assembleLiveQueryLoginPacket(subscriptionId, requestId));
    return true;
  }

  public void ackOperationReplied(int requestId) {
    operationCache.poll(requestId);
  }
}
