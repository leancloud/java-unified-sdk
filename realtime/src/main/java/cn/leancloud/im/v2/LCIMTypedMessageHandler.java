package cn.leancloud.im.v2;

/**
 * 继承此类来处理与自定义消息相关的事件
 */
public class LCIMTypedMessageHandler<T extends LCIMTypedMessage> extends MessageHandler<T> {
  /**
   * 重载此方法来处理接收消息
   *
   * @param message message instance.
   * @param conversation conversation instance.
   * @param client client instance.
   */
  @Override
  public void onMessage(T message, LCIMConversation conversation, LCIMClient client) {
    ;
  }

  /**
   * 重载此方法来处理消息回执
   *
   * @param message message instance.
   * @param conversation conversation instance.
   * @param client client instance.
   */
  @Override
  public void onMessageReceipt(T message, LCIMConversation conversation, LCIMClient client) {
    ;
  }

  @Override
  public void onMessageReceiptEx(T message, String operator, LCIMConversation conversation, LCIMClient client) {
    onMessageReceipt(message, conversation, client);
  }
}
