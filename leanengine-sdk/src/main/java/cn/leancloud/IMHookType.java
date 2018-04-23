package cn.leancloud;

public enum IMHookType {
  /**
   * 消息达到服务器，群组成员已解析完成之后，发送给收件人之前
   */
  messageReceived(true),
  /**
   * 消息发送完成，存在离线的收件人
   */
  receiversOffline(true),
  /**
   * 消息发送完成
   */
  messageSent(false),
  /**
   * 创建对话，在签名校验（如果开启）之后，实际创建之前
   */
  conversationStart(true),
  /**
   * 创建对话完成
   */
  conversationStarted(false),
  /**
   * 向对话添加成员，在签名校验（如果开启）之后，实际加入之前，包括主动加入和被其他用户加入两种情况
   */
  conversationAdd(true),
  /**
   * 从对话中踢出成员，在签名校验（如果开启）之后，实际踢出之前，用户自己退出对话不会调用。
   */
  conversationRemove(true),
  /**
   * 修改对话属性、设置或取消对话消息提醒，在实际修改之前调用
   */
  conversationUpdate(true);
  boolean isResponseNeed;

  private IMHookType(boolean response) {
    this.isResponseNeed = response;
  }

  @Override
  public String toString() {
    return "_" + this.name();
  }

  public static IMHookType parse(String functionName) {
    if (functionName != null && functionName.startsWith("_")) {
      String hookName = functionName.substring(1);
      if ("messageReceived".equals(hookName)) {
        return messageReceived;
      } else if ("receiversOffline".equals(hookName)) {
        return receiversOffline;
      } else if ("messageSent".equals(hookName)) {
        return messageSent;
      } else if ("conversationStart".equals(hookName)) {
        return conversationStart;
      } else if ("conversationStarted".equals(hookName)) {
        return conversationStarted;
      } else if ("conversationAdd".equals(hookName)) {
        return conversationAdd;
      } else if ("conversationRemove".equals(hookName)) {
        return conversationRemove;
      } else if ("conversationUpdate".equals(hookName)) {
        return conversationUpdate;
      }
    }
    return null;
  }
}
