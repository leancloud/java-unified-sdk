package cn.leancloud.im;

import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMMessage;
import cn.leancloud.im.v2.LCIMMessageHandler;

public class DummyMessageHandler extends LCIMMessageHandler {
  @Override
  public void onMessage(LCIMMessage message, LCIMConversation conversation, LCIMClient client) {
    System.out.println("conversation(" + conversation.getConversationId() + ") receiving message: " + message.toJSONString());
  }

  @Override
  public void onMessageReceipt(LCIMMessage message, LCIMConversation conversation, LCIMClient client) {
    System.out.println("conversation(" + conversation.getConversationId() + ") receiving receipt message: " + message.toJSONString());
  }
}
