package cn.leancloud.im.v2;

/**
 * 继承此类来处理与自定义消息相关的事件
 */
public class AVIMTypedMessageHandler<T extends AVIMTypedMessage> extends MessageHandler<T> {
  /**
   * 重载此方法来处理接收消息
   *
   * @param message
   * @param conversation
   * @param client
   */
  @Override
  public void onMessage(T message, AVIMConversation conversation, AVIMClient client) {
    ;
  }

  /**
   * 重载此方法来处理消息回执
   *
   * @param message
   * @param conversation
   * @param client
   */
  @Override
  public void onMessageReceipt(T message, AVIMConversation conversation, AVIMClient client) {
    ;
  }
}
