package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.session.AVSession;
import cn.leancloud.utils.LogUtil;

import java.lang.ref.ReferenceQueue;
import java.util.List;
import java.util.Map;

public class SimpleCommandCourier implements CommandCourier {
  private static final AVLogger LOGGER = LogUtil.getLogger(SimpleCommandCourier.class);

  private final boolean needCacheRequestKey;
  public SimpleCommandCourier(boolean needCacheRequestKey) {
    this.needCacheRequestKey = needCacheRequestKey;
  }

  public void openClient(String clientId, String tag, String userSessionToken,
                  boolean reConnect, AVIMClientCallback callback) {
    LOGGER.d("openClient...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    AVSession session = AVConnectionManager.getInstance().getOrCreateSession(clientId);
    session.open(tag, userSessionToken, reConnect, requestId);
  }

  public void queryClientStatus(String clientId, final AVIMClientStatusCallback callback) {}

  public void closeClient(String self, AVIMClientCallback callback) {}

  public void queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback) {}

  public void createConversation(final List<String> members, final String name,
                          final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                          final boolean isTemp, int tempTTL, final AVIMConversationCreatedCallback callback) {}


  public void sendMessage(final AVIMMessage message, final AVIMMessageOption messageOption, final AVIMConversationCallback callback) {}

  public void updateMessage(AVIMMessage oldMessage, AVIMMessage newMessage, AVIMMessageUpdatedCallback callback) {}

  public void recallMessage(AVIMMessage message, AVIMMessageRecalledCallback callback) {}
}
