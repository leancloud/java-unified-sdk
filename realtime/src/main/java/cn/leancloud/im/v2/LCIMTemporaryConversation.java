package cn.leancloud.im.v2;

public class LCIMTemporaryConversation extends LCIMConversation {
  protected LCIMTemporaryConversation(LCIMClient client, String conversationId) {
    super(client, conversationId);
    setTemporary(true);
  }

  /*
   * 判断当前临时对话是否已经过期
   */
  public boolean isExpired() {
    long now = System.currentTimeMillis() / 1000;
    return now > this.getTemporaryExpiredat() + this.getCreatedAt().getTime()/1000;
  }
}
