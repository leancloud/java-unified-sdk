package cn.leancloud.im.v2;

public class AVIMMessageManagerHelper {
  public static void processMessage(AVIMMessage message, int convType, AVIMClient client, boolean hasMore,
                                    boolean isTransient) {
    message.setCurrentClient(client.getClientId());
    AVIMMessageManager.processMessage(message, convType, client, hasMore, isTransient);
  }

  public static void processMessageReceipt(AVIMMessage message, AVIMClient client) {
    AVIMMessageManager.processMessageReceipt(message, client);
  }

  public static AVIMClientEventHandler getClientEventHandler() {
    return AVIMClient.getClientEventHandler();
  }

  public static AVIMConversationEventHandler getConversationEventHandler() {
    return AVIMMessageManager.getConversationEventHandler();
  }

  public static AVIMMessage parseTypedMessage(AVIMMessage message) {
    return AVIMMessageManager.parseTypedMessage(message);
  }

  public static void removeConversationCache(AVIMConversation conversation) {
    conversation.storage.deleteConversationData(conversation.getConversationId());
  }

  public static String getMessageToken(AVIMMessage msg) {
    return msg.getUniqueToken();
  }
}
