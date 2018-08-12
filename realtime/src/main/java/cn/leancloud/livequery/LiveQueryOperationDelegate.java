package cn.leancloud.livequery;

import cn.leancloud.im.WindTalker;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.session.AVIMOperationQueue;

public class LiveQueryOperationDelegate {
  private static final LiveQueryOperationDelegate instance = new LiveQueryOperationDelegate();

  public static final String LIVEQUERY_DEFAULT_ID = "livequery_default_id";
  public static LiveQueryOperationDelegate getInstance() {
    return instance;
  }

  AVIMOperationQueue operationCache;
  private LiveQueryOperationDelegate() {
    operationCache = new AVIMOperationQueue(LIVEQUERY_DEFAULT_ID);
  }

  public boolean login(String subscriptionId, int requestId) {
    // FIXME: no timeout timer for login request.
    operationCache.offer(AVIMOperationQueue.Operation.getOperation(
            Conversation.AVIMOperation.LIVEQUERY_LOGIN.getCode(), LIVEQUERY_DEFAULT_ID, null, requestId));
    AVConnectionManager.getInstance().sendPacket(WindTalker.getInstance().assembleLiveQueryLoginPacket(subscriptionId, requestId));
    return true;
  }

  public void ackOperationReplied(int requestId) {
    operationCache.poll(requestId);
  }
}
