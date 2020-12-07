package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.im.Signature;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.AVInstallation;
import cn.leancloud.utils.StringUtil;

import java.util.Collection;
import java.util.List;

public class SessionControlPacket extends PeerBasedCommandPacket {
  public static final String SESSION_COMMAND = "session";
  public static class SessionControlOp {
    public static final String OPEN = "open";

    public static final String ADD = "add";

    public static final String REMOVE = "remove";

    public static final String CLOSE = "close";

    public static final String QUERY = "query";

    public static final String OPENED = "opened";

    public static final String ADDED = "added";

    public static final String QUERY_RESULT = "query_result";

    public static final String REMOVED = "removed";

    public static final String CLOSED = "closed";

    public static final String RENEW_RTMTOKEN = "refresh";
    public static final String RENEWED_RTMTOKEN = "refreshed";

    public static final String SESSION_TOKEN = "st";

    public static final String SESSION_TOKEN_TTL = "stTtl";
  }

  private static final long PATCH_FLAG = 0x01;                // support to update and recall message.
  private static final long PATCH_FLAG_TEMPORARY_CONV = 0x02; // support temporary conversation.
  private static final long PATCH_FLAG_BIND_INSTALLATION_TO_SESSION = 0x04; // support to bind Installation.
  private static final long PATCH_FLAG_ACK_4_TRANSIENT_MSG = 0x08;          // support to receive ack for transient message.
  private static final long PATCH_FLAG_SUPPORT_CONVMEMBER_INFO = 0x20;      // support partial result for conv operation.
  private static final long PATCH_FLAG_ACK_4_GROUPCHAT = 0x40;              // support to receive ack for group chatting.
  private static final long PATCH_FLAG_SUPPORT_RELIABLE_NOTIFICATION = 0x10;// support reliable notification.

  private String op;

  private Collection<String> sessionPeerIds;

  private String signature;

  private long timestamp;

  private String nonce;

  private boolean reconnectionRequest = false;

  private long lastUnreadNotifyTime = 0;

  private long lastPatchTime = 0;

  private long sessionConfig = 0;

  String tag;
  String sessionToken;
  String deviceId;

  public SessionControlPacket() {
    this.setCmd(SESSION_COMMAND);
  }

  public void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }

  public boolean isReconnectionRequest() {
    return reconnectionRequest;
  }

  public void setReconnectionRequest(boolean reconnectionRequest) {
    this.reconnectionRequest = reconnectionRequest;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setOp(Messages.OpType.valueOf(op));
    builder.setSessionMessage(getSessionCommand());
    return builder;
  }

  private Messages.SessionCommand getSessionCommand() {
    Messages.SessionCommand.Builder builder = Messages.SessionCommand.newBuilder();

    if (sessionPeerIds != null && !sessionPeerIds.isEmpty()) {
      builder.addAllSessionPeerIds(sessionPeerIds);
    }

    if (op.equals(SessionControlOp.OPEN)) {
      builder.setUa(AppConfiguration.getUserAgent());
      if (!StringUtil.isEmpty(tag)) {
        builder.setTag(tag);
      }
    }
    if (op.equals(SessionControlOp.OPEN) || op.equals(SessionControlOp.CLOSE)) {
      if (!StringUtil.isEmpty(deviceId)) {
        builder.setDeviceId(deviceId);
      }
    }

    if (!StringUtil.isEmpty(signature)) {
      builder.setS(signature);
      builder.setT(timestamp);
      builder.setN(nonce);
    }

    if (reconnectionRequest) {
      builder.setR(true);
    }

    if (lastUnreadNotifyTime > 0) {
      builder.setLastUnreadNotifTime(lastUnreadNotifyTime);
    }

    if (lastPatchTime > 0) {
      builder.setLastPatchTime(lastPatchTime);
    }

    if (!StringUtil.isEmpty(sessionToken)) {
      builder.setSt(sessionToken);
    }

    if (0 != sessionConfig) {
      builder.setConfigBitmap(sessionConfig);
    }

    return builder.build();
  }

  public static SessionControlPacket genSessionCommand(String deviceId, String selfId, List<String> peers,
                                                       String op, Signature signature, Integer requestId) {
    return genSessionCommand(deviceId, selfId, peers, op, signature, 0, 0, requestId);
  }

  public static SessionControlPacket genSessionCommand(String deviceId, String selfId, List<String> peers, String op, Signature signature,
                                                       long lastUnreadNotifyTime, long lastPatchTime, Integer requestId) {

    SessionControlPacket scp = new SessionControlPacket();

    if (signature != null) {
      if (op.equals(SessionControlPacket.SessionControlOp.OPEN)
              || op.equals(SessionControlPacket.SessionControlOp.ADD)) {
        scp.signature = signature.getSignature();
        scp.nonce = signature.getNonce();
        scp.timestamp = signature.getTimestamp();
      }
    }

    scp.op = op;
    scp.sessionPeerIds = peers;
    scp.lastUnreadNotifyTime = lastUnreadNotifyTime;
    scp.lastPatchTime = lastPatchTime;
    scp.sessionConfig |= PATCH_FLAG | PATCH_FLAG_TEMPORARY_CONV | PATCH_FLAG_ACK_4_TRANSIENT_MSG;
    scp.sessionConfig |= PATCH_FLAG_SUPPORT_CONVMEMBER_INFO;
    scp.setDeviceId(deviceId);
    if (op.equals(SessionControlOp.RENEW_RTMTOKEN)) {
      scp.setPeerId(selfId);
    } else if (op.equals(SessionControlOp.OPEN)) {
      // selfId is mandotary for session/open
      scp.sessionConfig |= PATCH_FLAG_BIND_INSTALLATION_TO_SESSION;
      scp.setPeerId(selfId);
    } else if (AVIMClient.getClientsCount() > 1) {
      // selfId is necessary only when more than one client logins.
      scp.setPeerId(selfId);
    }
    if (null == requestId) {
      scp.setRequestId(SessionControlPacket.UNSUPPORTED_OPERATION);
    } else {
      scp.setRequestId(requestId);
    }
    return scp;
  }
}
