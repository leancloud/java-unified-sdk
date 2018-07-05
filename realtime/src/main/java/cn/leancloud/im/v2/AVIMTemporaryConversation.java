package cn.leancloud.im.v2;

public class AVIMTemporaryConversation extends AVIMConversation {
  protected AVIMTemporaryConversation(AVIMClient client, String conversationId) {
    super(client, conversationId);
    isTemporary = true;
  }
}
