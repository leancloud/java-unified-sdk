package cn.leancloud.im;

import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.callback.*;

import java.util.List;
import java.util.Map;

public interface CommandCourier {
  void openClient(String clientId, String tag, String userSessionToken,
                  boolean reConnect, AVIMClientCallback callback);
  void queryClientStatus(String clientId, final AVIMClientStatusCallback callback);
  void closeClient(String self, AVIMClientCallback callback);
  void queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback);

  void createConversation(final List<String> members, final String name,
                          final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                          final boolean isTemp, int tempTTL, final AVIMConversationCreatedCallback callback);

  void sendMessage(final AVIMMessage message, final AVIMMessageOption messageOption, final AVIMConversationCallback callback);
  void updateMessage(AVIMMessage oldMessage, AVIMMessage newMessage, AVIMMessageUpdatedCallback callback);
  void recallMessage(AVIMMessage message, AVIMMessageRecalledCallback callback);
}
