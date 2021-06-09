package cn.leancloud.im.v2.conversation;

import cn.leancloud.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class LCIMConversationMemberInfo {
  public static final String ATTR_OJBECTID = "infoId";
  public static final String ATTR_OJBECTID2 = "objectId";
  public static final String ATTR_CONVID = "conversationId";
  public static final String ATTR_CONVID_SIMPLE = "cid";
  public static final String ATTR_MEMBERID = "peerId";
  public static final String ATTR_CLIENTID = "clientId";
  public static final String ATTR_ROLE = "role";
  private static final String ATTR_CREATEDAT = "createdAt";
  private static final String ATTR_NICKNAME = "nickname";
  private static final String ATTR_INVITER = "inviter";

  private String conversationId = null;
  private String memberId = null;
  private ConversationMemberRole role;
  private boolean isOwner = false;

  private String createdAt = null;
  private String objectId = null;
  private String inviter = null;
  private String nickname = null;

  /**
   * 构造函数
   * @param conversationId 对话 id
   * @param memberId       成员的 client id
   * @param role           角色
   */
  public LCIMConversationMemberInfo(String conversationId, String memberId, ConversationMemberRole role) {
    this(null, conversationId, memberId, role);
  }

  /**
   * 构造函数
   * @param objectId         记录的 objectId
   * @param conversationId   对话 id
   * @param memberId         成员的 client id
   * @param role             角色
   */
  public LCIMConversationMemberInfo(String objectId, String conversationId, String memberId, ConversationMemberRole role) {
    this.objectId = objectId;
    this.conversationId = conversationId;
    this.memberId = memberId;
    this.role = role;
  }

  public String toString() {
    return "convId:" + this.conversationId + ", memberId:" + this.memberId + ", role:" + this.role.toString();
  }

  /**
   * 获取对话 id
   * @return conversation id
   */
  public String getConversationId() {
    return conversationId;
  }

  /**
   * 获取成员的 client id
   * @return member id
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * 获取角色信息
   * @return ConversationMemberRole instance.
   */
  public ConversationMemberRole getRole() {
    return this.role;
  }

  /**
   * 设置角色
   * @param role ConversationMemberRole instance.
   */
  public void setRole(ConversationMemberRole role) {
    this.role = role;
  }

  /**
   * 获取成员加入时间（也就是该记录创建时间）
   * 注意：目前尚未实现
   * @return createdAt timestamp string.
   */
  public String getCreatedAt() {
    return createdAt;
  }

  /**
   * 设置记录创建时间
   * 注意：目前尚未实现
   * @param createdAt createdAt timestamp string.
   */
  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * 获取该条记录的 objectId
   * @return objectId.
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * 获取邀请者名字
   * 注意：目前尚未实现
   * @return inviter id.
   */
  public String getInviter() {
    return inviter;
  }

  /**
   * 设置邀请者名字
   * 注意：目前尚未实现
   * @param inviter inviter id.
   */
  public void setInviter(String inviter) {
    this.inviter = inviter;
  }

  /**
   * 获取成员的昵称
   * 注意：目前尚未实现
   * @return nickname.
   */
  public String getNickname() {
    return nickname;
  }

  /**
   * 设置成员的昵称
   * 注意：目前尚未实现
   * @param nickname nickname.
   */
  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public boolean isOwner() {
    return isOwner;
  }

  /**
   * 返回属性更新的 Map（内部使用）
   * @return updated attribute map.
   */
  public Map<String,String> getUpdateAttrs() {
    HashMap<String, String> attrs = new HashMap<>();
    attrs.put(ATTR_MEMBERID, getMemberId());
    attrs.put(ATTR_ROLE, getRole().getName());
    if (!StringUtil.isEmpty(getObjectId())) {
      attrs.put(ATTR_OJBECTID, getObjectId());
    }
    return attrs;
  }

  /**
   * 根据服务端返回信息创建实例（内部使用）
   * @param data attribute map.
   * @return new ConversationMemberInfo instance.
   */
  public static LCIMConversationMemberInfo createInstance(Map<String, Object> data) {
    if (null == data) {
      return null;
    }
    String conversationId = (String)data.get(ATTR_CONVID);
    if (StringUtil.isEmpty(conversationId)) {
      conversationId = (String) data.get(ATTR_CONVID_SIMPLE);
    }
    String memberId = (String)data.get(ATTR_MEMBERID);
    if (StringUtil.isEmpty(memberId)) {
      memberId = (String) data.get(ATTR_CLIENTID);
    }
    String roleStr = (String)data.get(ATTR_ROLE);
    String objectId = (String)data.get(ATTR_OJBECTID);
    if (StringUtil.isEmpty(objectId)) {
      objectId = (String) data.get(ATTR_OJBECTID2);
    }
    ConversationMemberRole role = ConversationMemberRole.fromString(roleStr);
    return new LCIMConversationMemberInfo(objectId, conversationId, memberId, role);
  }
}
