package cn.leancloud.im;

import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.session.AVSession;

import java.util.List;
import java.util.Map;

public class SimpleCommandCourier implements CommandCourier {
  public void openClient(String clientId, String tag, String userSessionToken,
                  boolean reConnect, AVIMClientCallback callback) {
    int requestId = WindTalker.getNextIMRequestId();
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
