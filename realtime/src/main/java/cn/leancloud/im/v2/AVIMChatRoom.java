package cn.leancloud.im.v2;

public class AVIMChatRoom extends AVIMConversation {
  protected AVIMChatRoom(AVIMClient client, String conversationId){
    super(client, conversationId);
    setTransientForInit(true);
  }
}
