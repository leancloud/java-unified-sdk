package cn.leancloud.im;

import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageHandler;

public class DummyMessageHandler extends AVIMMessageHandler{
  @Override
  public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
    System.out.println("conversation(" + conversation.getConversationId() + ") receiving message: " + message.toJSONString());
  }

  @Override
  public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
    System.out.println("conversation(" + conversation.getConversationId() + ") receiving receipt message: " + message.toJSONString());
  }
}
