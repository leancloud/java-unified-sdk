package cn.leancloud.im.v2;

/**
 * 继承此类来处理与消息相关的事件
 */
public class LCIMMessageHandler extends MessageHandler<LCIMMessage> {
  /**
   * 重载此方法来处理接收消息
   *
   * @param message message instance.
   * @param conversation conversation instance.
   * @param client client instance.
   */
  @Override
  public void onMessage(LCIMMessage message, LCIMConversation conversation, LCIMClient client) {

  }

  /**
   * 重载此方法来处理消息回执
   *
   * @param message message instance.
   * @param conversation conversation instance.
   * @param client client instance.
   */
  @Override
  public void onMessageReceipt(LCIMMessage message, LCIMConversation conversation, LCIMClient client) {
  }

  /**
   * 重载此方法来处理消息回执
   *
   * @param message message instance.
   * @param operator operator client id.
   * @param conversation conversation instance.
   * @param client client instance.
   */
  @Override
  public void onMessageReceiptEx(LCIMMessage message, String operator, LCIMConversation conversation,
                                 LCIMClient client) {
    onMessageReceipt(message, conversation, client);
  }
}