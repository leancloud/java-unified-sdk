package cn.leancloud.im.v2;

public class LCIMMessageManagerHelper {
  public static void processMessage(LCIMMessage message, int convType, LCIMClient client, boolean hasMore,
                                    boolean isTransient) {
    message.setCurrentClient(client.getClientId());
    LCIMMessageManager.processMessage(message, convType, client, hasMore, isTransient);
  }

  public static void processMessageReceipt(LCIMMessage message, LCIMClient client, String from) {
    LCIMMessageManager.processMessageReceipt(message, client, from);
  }

  public static LCIMClientEventHandler getClientEventHandler() {
    return LCIMClient.getClientEventHandler();
  }

  public static LCIMConversationEventHandler getConversationEventHandler() {
    return LCIMMessageManager.getConversationEventHandler();
  }

  public static LCIMMessage parseTypedMessage(LCIMMessage message) {
    return LCIMMessageManager.parseTypedMessage(message);
  }

  public static void removeConversationCache(LCIMConversation conversation) {
    conversation.storage.deleteConversationData(conversation.getConversationId());
  }

  public static String getMessageToken(LCIMMessage msg) {
    return msg.getUniqueToken();
  }
}
