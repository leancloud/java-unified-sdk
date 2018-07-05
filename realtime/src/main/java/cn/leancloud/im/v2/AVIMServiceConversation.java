package cn.leancloud.im.v2;

public class AVIMServiceConversation extends AVIMConversation {
  protected AVIMServiceConversation(AVIMClient client, String conversationId) {
    super(client, conversationId);
    isSystem = true;
  }
}
