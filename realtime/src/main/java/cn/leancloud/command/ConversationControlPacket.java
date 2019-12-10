package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.im.Signature;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;

public class ConversationControlPacket extends PeerBasedCommandPacket {
  public static final String CONVERSATION_CMD = "conv";

  public static class ConversationControlOp {
    /**
     * 客户端发出的op
     */
    public static final String START = "start";
    public static final String ADD = "add";
    public static final String REMOVE = "remove";
    public static final String QUERY = "query";
    public static final String UPDATE = "update";
    public static final String MUTE = "mute";
    public static final String UNMUTE = "unmute";
    public static final String COUNT = "count";
    public static final String MAX_READ = "max_read";
    // 权限
    public static final String MEMBER_UPDATE = "member_info_update";
    // 禁言
    public static final String ADD_SHUTUP = "add_shutup";
    public static final String REMOVE_SHUTUP = "remove_shutup";
    public static final String QUERY_SHUTUP = "query_shutup";

    /**
     * 服务器端会响应的op
     */
    public static final String STARTED = "started";
    public static final String JOINED = "joined";
    public static final String MEMBER_JOINED = "members_joined";
    public static final String MEMBER_LEFTED = "members_left";
    public static final String ADDED = "added";
    public static final String REMOVED = "removed";
    public static final String LEFT = "left";
    public static final String QUERY_RESULT = "results";
    public static final String MEMBER_COUNT_QUERY_RESULT = "result";
    public static final String UPDATED = "updated";
    // 权限
    public static final String MEMBER_UPDATED = "member_info_updated";
    public static final String MEMBER_INFO_CHANGED = "member_info_changed";
    // 禁言
    public static final String SHUTUP_ADDED = "shutup_added";
    public static final String SHUTUP_REMOVED = "shutup_removed";
    public static final String SHUTUPED = "shutuped";
    public static final String UNSHUTUPED = "unshutuped";
    public static final String MEMBER_SHUTPED = "members_shutuped";
    public static final String MEMBER_UNSHUTUPED = "members_unshutuped";
    public static final String QUERY_SHUTUP_RESULT = "shutup_result";
    // 黑名单通知
    public static final String BLOCKED = "blocked";
    public static final String UNBLOCKED = "unblocked";
    public static final String MEMBER_BLOCKED_NOTIFY = "members_blocked";
    public static final String MEMBER_UNBLOCKED_NOTIFY = "members_unblocked";
  }

  private List<String> members;
  private String signature;

  private long timestamp;

  private String nonce;

  private String conversationId;

  private String op;

  private Map<String, Object> attributes;

  private boolean isTransient;

  /**
   * 原子创建单聊会话，如果为 true，则先查询是否有符合条件的 conversation，有则返回已存在的，否则创建新的
   * 详见 https://github.com/leancloud/avoscloud-push/issues/293
   */
  private boolean isUnique;

  private boolean isTemporary = false;

  private int tempTTL = 0;

  private Map<String, Object> memberInfo = null;

  private int queryOffset = 0;
  private int queryLimit = 0;

  public ConversationControlPacket() {
    this.setCmd(CONVERSATION_CMD);
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public boolean isTransient() {
    return isTransient;
  }

  public void setTransient(boolean isTransient) {
    this.isTransient = isTransient;
  }

  public boolean isUnique() {
    return isUnique;
  }

  public void setUnique(boolean isUnique) {
    this.isUnique = isUnique;
  }

  public boolean isTemporary() {return isTemporary;}

  public void setTemporary(boolean val) {
    this.isTemporary = val;
  }

  public int getTempTTL() {
    return tempTTL;
  }

  public void setTempTTL(int tempTTL) {
    this.tempTTL = tempTTL;
  }

  public void setMemberInfo(Map<String, Object> memberInfo) {
    this.memberInfo = memberInfo;
  }

  public int getQueryOffset() {
    return queryOffset;
  }

  public void setQueryOffset(int queryOffset) {
    this.queryOffset = queryOffset;
  }

  public int getQueryLimit() {
    return queryLimit;
  }

  public void setQueryLimit(int queryLimit) {
    this.queryLimit = queryLimit;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setOp(Messages.OpType.valueOf(op));
    builder.setConvMessage(getConvCommand());
    return builder;
  }

  private Messages.ConvCommand getConvCommand() {
    Messages.ConvCommand.Builder builder = Messages.ConvCommand.newBuilder();

    if (attributes != null && !attributes.isEmpty()) {
      Messages.JsonObjectMessage.Builder attrBuilder = Messages.JsonObjectMessage.newBuilder();
      attrBuilder.setData(JSON.toJSONString(attributes));
      builder.setAttr(attrBuilder);
    }

    if (null != members && members.size() > 0) {
      builder.addAllM(members);
    }

    if (getSignature() != null) {
      builder.setS(getSignature());
      builder.setT(getTimestamp());
      builder.setN(getNonce());
    }

    if (!StringUtil.isEmpty(conversationId)) {
      builder.setCid(conversationId);
    }
    if (isTransient) {
      builder.setTransient(isTransient);
    }
    if (isUnique) {
      builder.setUnique(isUnique);
    }
    if (isTemporary) {
      builder.setTempConv(isTemporary);
      builder.setTempConvTTL(tempTTL);
    }

    if (null != memberInfo) {
      Messages.ConvMemberInfo.Builder cmiBuilder = Messages.ConvMemberInfo.newBuilder();
      if (memberInfo.containsKey("peerId")) {
        cmiBuilder.setPid((String) memberInfo.get("peerId"));
        builder.setTargetClientId((String) memberInfo.get("peerId"));
      }
      if (memberInfo.containsKey("role")) {
        cmiBuilder.setRole((String) memberInfo.get("role"));
      }
      if (memberInfo.containsKey("infoId")) {
        cmiBuilder.setInfoId((String) memberInfo.get("infoId"));
      }
      builder.setInfo(cmiBuilder.build());
    }
    if (this.queryOffset > 0) {
      builder.setNext(Integer.toString(this.queryOffset));
    }
    if (this.queryLimit > 0) {
      builder.setLimit(this.queryLimit);
    }
    return builder.build();
  }

  public static ConversationControlPacket genConversationCommand(String selfId,
                                                                 String conversationId, List<String> peers, String op, Map<String, Object> attributes,
                                                                 Signature signature, boolean isTransient, boolean isUnique, boolean isTemporary, int tempTTL,
                                                                 boolean isSystem, int requestId) {
    ConversationControlPacket ccp = new ConversationControlPacket();
//    if (AVIMClient.getClientsCount() > 1) {
      // selfId is necessary only when more than 1 clients logined.
      ccp.setPeerId(selfId);
//    }
    ccp.setConversationId(conversationId);
    ccp.setRequestId(requestId);
    ccp.setTransient(isTransient);
    ccp.setUnique(isUnique);
    ccp.setTemporary(isTemporary);
    if (isTemporary) {
      ccp.setTempTTL(tempTTL);
    }

    if (null != peers && peers.size() > 0) {
      ccp.setMembers(peers);
    }
    ccp.setOp(op);

    if (signature != null) {
      if (op.equals(ConversationControlOp.ADD) || op.equals(ConversationControlOp.REMOVE)
              || op.equals(ConversationControlOp.START)) {
        ccp.setSignature(signature.getSignature());
        ccp.setNonce(signature.getNonce());
        ccp.setTimestamp(signature.getTimestamp());
      }
    }
    ccp.setRequestId(requestId);
    ccp.setAttributes(attributes);

    return ccp;
  }

  public static ConversationControlPacket genConversationCommand(String selfId,
                                                                 String conversationId, List<String> peers, String op, Map<String, Object> attributes,
                                                                 Signature signature, boolean isTransient, int requestId) {
    return genConversationCommand(selfId, conversationId, peers, op, attributes, signature, isTransient,
            false, false, 0, false, requestId);
  }

  public static ConversationControlPacket genConversationCommand(String selfId,
                                                                 String conversationId, List<String> peers, String op, Map<String, Object> attributes,
                                                                 Signature signature, int requestId) {
    return genConversationCommand(selfId, conversationId, peers, op, attributes, signature, false,
            requestId);
  }

  public static ConversationControlPacket genConversationMemberCommand(String selfId, String conversationId,
                                                                       String op, Map<String, Object> memberInfo,
                                                                       Signature signature, int requestId) {
    ConversationControlPacket ccp = genConversationCommand(selfId, conversationId, null, op, null, signature,
            false, false, false, 0, false, requestId);
    ccp.setMemberInfo(memberInfo);
    return ccp;
  }
}
