package cn.leancloud.im.v2.conversation;

public enum ConversationMemberRole {
  MANAGER("Manager"),  // 管理员
  MEMBER("Member");    // 普通成员
  private String role;
  ConversationMemberRole(String role) {
    this.role = role;
  }

  /**
   * 获取角色名字
   * @return name of role
   */
  public String getName() {
    return this.role;
  }

  /**
   * 从角色名字生成实例
   * @param role 角色名字
   * @return new ConversationMemberRole instance
   */
  public static ConversationMemberRole fromString(String role) {
    for(ConversationMemberRole mr: ConversationMemberRole.values()) {
      if (mr.role.equalsIgnoreCase(role)) {
        return mr;
      }
    }
    return null;
  }
}
