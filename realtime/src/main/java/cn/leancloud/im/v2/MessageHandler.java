package cn.leancloud.im.v2;

import cn.leancloud.im.LCIMEventHandler;

public abstract class MessageHandler<T extends LCIMMessage> extends LCIMEventHandler {
  public abstract void onMessage(T message, LCIMConversation conversation, LCIMClient client);

  public abstract void onMessageReceipt(T message, LCIMConversation conversation, LCIMClient client);

  public abstract void onMessageReceiptEx(T message, String operator, LCIMConversation conversation, LCIMClient client);

  @Override
  protected final void processEvent0(final int operation, final Object operator, final Object operand,
                                     Object eventScene) {
    final LCIMConversation conversation = (LCIMConversation) eventScene;
    processMessage(operation, operator, operand, conversation);
  }

  private void processMessage(int operation, final Object operator, final Object operand, LCIMConversation conversation) {
    switch (operation) {
      case Conversation.STATUS_ON_MESSAGE:
        onMessage((T) operand, conversation, conversation.client);
        break;
      case Conversation.STATUS_ON_MESSAGE_RECEIPTED:
        if (null == operator) {
          onMessageReceipt((T) operand, conversation, conversation.client);
        } else {
          onMessageReceiptEx((T) operand, (String) operator, conversation, conversation.client);
        }
        break;
      default:
        break;
    }
  }
}