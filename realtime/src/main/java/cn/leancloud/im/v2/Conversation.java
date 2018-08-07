package cn.leancloud.im.v2;

import cn.leancloud.AVObject;

public interface Conversation {
  String AV_CONVERSATION_INTENT_ACTION = "com.avoscloud.im.v2.action";

  String PARAM_CLIENT_TAG = "client.tag";
  String PARAM_CLIENT_USERSESSIONTOKEN = "client.userSession";
  String PARAM_CLIENT_RECONNECTION = "client.reconnect";

  String PARAM_CONVERSATION_MEMBER = "conversation.member";
  String PARAM_CONVERSATION_ATTRIBUTE = "conversation.attributes";
  String PARAM_CONVERSATION_ISTRANSIENT = "conversation.transient";
  String PARAM_CONVERSATION_ISUNIQUE = "conversation.unique";
  String PARAM_CONVERSATION_ISTEMPORARY = "conversation.temp";
  String PARAM_CONVERSATION_TEMPORARY_TTL = "conversation.tempTTL";
  String PARAM_CONVERSATION_ISSYSTEM = "conversation.sys";
  String PARAM_ONLINE_CLIENTS = "client.oneline";
  String PARAM_CONVERSATION_MEMBER_DETAILS = "conversation.memberDetails";

  String PARAM_MESSAGE_QUERY_LIMIT = "limit";
  String PARAM_MESSAGE_QUERY_DIRECT = "direct";
  String PARAM_MESSAGE_QUERY_TIMESTAMP = "ts";
  String PARAM_MESSAGE_QUERY_MSGID = "mid";
  String PARAM_MESSAGE_QUERY_STARTCLOSED = "sinc";
  String PARAM_MESSAGE_QUERY_TO_MSGID = "tmid";
  String PARAM_MESSAGE_QUERY_TO_TIMESTAMP = "tt";
  String PARAM_MESSAGE_QUERY_TOCLOSED = "tinc";
  String PARAM_MESSAGE_QUERY_TYPE = "type";

  String INTENT_KEY_DATA = "conversation.data";
  String INTENT_KEY_MESSAGE_OPTION = "conversation.messageoption";
  String INTENT_KEY_CLIENT_PARCEL = "conversation.client.parcel";
  String INTENT_KEY_CLIENT = "conversation.client";
  String INTENT_KEY_CONVERSATION = "convesration.id";
  String INTENT_KEY_CONV_TYPE = "conversation.type";
  String INTENT_KEY_OPERATION = "conversation.operation";
  String INTENT_KEY_REQUESTID = "conversation.requestId";

  String PARAM_MESSAGE_PATCH_TIME = "message_patch_time";

  int DEFAULT_CONVERSATION_EXPIRE_TIME_IN_MILLS = 3600000;  // 1 hour

  enum AVIMOperation {
    CONVERSATION_CREATION(40000, "com.avoscloud.v2.im.conversation.creation."),
    CONVERSATION_ADD_MEMBER(40001, "com.avoscloud.v2.im.conversation.members."),
    CONVERSATION_RM_MEMBER(40002, "com.avoscloud.v2.im.conversation.members."),
    CONVERSATION_JOIN(40003, "com.avoscloud.v2.im.conversation.join."),
    CONVERSATION_QUIT(40004, "com.avoscloud.v2.im.conversation.quit."),
    CONVERSATION_SEND_MESSAGE(40005, "com.avoscloud.v2.im.conversation.message."),
    CLIENT_OPEN(40006, "com.avoscloud.v2.im.client.initialize."),
    CLIENT_DISCONNECT(40007, "com.avoscloud.v2.im.client.quit."),
    CONVERSATION_QUERY(40008, "com.avoscloud.v2.im.conversation.query."),
    CONVERSATION_UPDATE(40009, "com.avoscloud.v2.im.conversation.update."),
    CONVERSATION_MESSAGE_QUERY(40010, "com.avoscloud.v2.im.conversation.message.query."),
    CONVERSATION_MUTE(40011, "com.avoscloud.v2.im.conversation.mute."),
    CONVERSATION_UNMUTE(40012, "com.avoscloud.v2.im.conversation.unmute"),
    CONVERSATION_MEMBER_COUNT_QUERY(40013, "com.avoscloud.v2.im.conversation.membercount."),
    CLIENT_ONLINE_QUERY(40014, "com.avoscloud.v2.im.client.onlineQuery."),
    CLIENT_STATUS(40015, "com.avoscloud.v2.im.client.status"),
    CONVERSATION_READ(40016, "com.avoscloud.v2.im.conversation.read."),
    CONVERSATION_FETCH_RECEIPT_TIME(40017, "com.avoscloud.v2.im.conversation.fetchReceiptTimestamps."),
    CONVERSATION_UPDATE_MESSAGE(40018, "com.avoscloud.v2.im.conversation.updateMessage."),
    CONVERSATION_RECALL_MESSAGE(40019, "com.avoscloud.v2.im.conversation.recallMessage."),
    CLIENT_REFRESH_TOKEN(40020, "com.avoscloud.v2.im.client.refreshToken"),
    CONVERSATION_PROMOTE_MEMBER(40021, "com.avoscloud.v2.im.conversation.promoteMember"),
    CONVERSATION_MUTE_MEMBER(40022, "com.avoscloud.v2.im.conversation.muteMember"),
    CONVERSATION_UNMUTE_MEMBER(40023, "com.avoscloud.v2.im.conversation.unmuteMember"),
    CONVERSATION_BLOCK_MEMBER(40024, "com.avoscloud.v2.im.conversation.blockMember"),
    CONVERSATION_UNBLOCK_MEMBER(40025, "com.avoscloud.v2.im.conversation.unblockMember"),
    CONVERSATION_MUTED_MEMBER_QUERY(40026, "com.avoscloud.v2.im.conversation.mutedMemberQuery"),
    CONVERSATION_BLOCKED_MEMBER_QUERY(40027, "com.avoscloud.v2.im.conversation.blockedMemberQuery"),
    CONVERSATION_UNKNOWN(49999, "com.avoscloud.v2.im.conversation.unknown");

    private final String header;
    private final int code;

    AVIMOperation(int operationCode, String operationHeader) {
      this.code = operationCode;
      this.header = operationHeader;
    }

    public int getCode() {
      return code;
    }

    public String getOperation() {
      return header;
    }

    public static AVIMOperation getAVIMOperation(int code) {
      switch (code) {
        case 40000:
          return CONVERSATION_CREATION;
        case 40001:
          return CONVERSATION_ADD_MEMBER;
        case 40002:
          return CONVERSATION_RM_MEMBER;
        case 40003:
          return CONVERSATION_JOIN;
        case 40004:
          return CONVERSATION_QUIT;
        case 40005:
          return CONVERSATION_SEND_MESSAGE;
        case 40006:
          return CLIENT_OPEN;
        case 40007:
          return CLIENT_DISCONNECT;
        case 40008:
          return CONVERSATION_QUERY;
        case 40009:
          return CONVERSATION_UPDATE;
        case 40010:
          return CONVERSATION_MESSAGE_QUERY;
        case 40011:
          return CONVERSATION_MUTE;
        case 40012:
          return CONVERSATION_UNMUTE;
        case 40013:
          return CONVERSATION_MEMBER_COUNT_QUERY;
        case 40014:
          return CLIENT_ONLINE_QUERY;
        case 40015:
          return CLIENT_STATUS;
        case 40016:
          return CONVERSATION_READ;
        case 40017:
          return CONVERSATION_FETCH_RECEIPT_TIME;
        case 40018:
          return CONVERSATION_UPDATE_MESSAGE;
        case 40019:
          return CONVERSATION_RECALL_MESSAGE;
        case 40020:
          return CLIENT_REFRESH_TOKEN;
        case 40021:
          return CONVERSATION_PROMOTE_MEMBER;
        case 40022:
          return CONVERSATION_MUTE_MEMBER;
        case 40023:
          return CONVERSATION_UNMUTE_MEMBER;
        case 40024:
          return CONVERSATION_BLOCK_MEMBER;
        case 40025:
          return CONVERSATION_UNBLOCK_MEMBER;
        case 40026:
          return CONVERSATION_MUTED_MEMBER_QUERY;
        case 40027:
          return CONVERSATION_BLOCKED_MEMBER_QUERY;
        default:
          return CONVERSATION_UNKNOWN;
      }
    }
  }

  int STATUS_ON_MESSAGE = 50000;
  int STATUS_ON_MESSAGE_RECEIPTED = 50001;
  int STATUS_ON_MEMBERS_LEFT = 50004;
  int STATUS_ON_MEMBERS_JOINED = 50005;
  int STATUS_ON_CONNECTION_PAUSED = 50006;
  int STATUS_ON_CONNECTION_RESUMED = 50007;
  int STATUS_ON_JOINED = 50008;
  int STATUS_ON_KICKED_FROM_CONVERSATION = 50009;
  int STATUS_ON_CLIENT_OFFLINE = 50010;
  int STATUS_ON_UNREAD_EVENT = 50012;
  int STATUS_ON_MESSAGE_READ = 50013;
  int STATUS_ON_MESSAGE_DELIVERED = 50014;
  int STATUS_ON_MESSAGE_UPDATED = 50015;
  int STATUS_ON_MESSAGE_RECALLED = 50016;
  int STATUS_ON_MEMBER_INFO_CHANGED = 50017;
  // mute member
  int STATUS_ON_MUTED = 50018;
  int STATUS_ON_UNMUTED = 50019;
  int STATUS_ON_MEMBER_MUTED = 50020;
  int STATUS_ON_MEMBER_UNMUTED = 50021;
  // block member
  int STATUS_ON_BLOCKED = 50022;
  int STATUS_ON_UNBLOCKED = 50023;
  int STATUS_ON_MEMBER_BLOCKED = 50024;
  int STATUS_ON_MEMBER_UNBLOCKED = 50025;
  // info changed
  int STATUS_ON_INFO_CHANGED = 50026;

  int CONV_TYPE_UNKNOWN = 0;
  int CONV_TYPE_NORMAL = 1;
  int CONV_TYPE_TRANSIENT = 2;
  int CONV_TYPE_SYSTEM = 3;
  int CONV_TYPE_TEMPORARY = 4;

  String callbackExceptionKey = "callbackException";
  String callbackData = "callbackData";
  String callbackClientKey = "callbackclient";
  String callbackConversationKey = "callbackconversation";
  String callbackMessageTimeStamp = "callbackMessageTimeStamp";
  String callbackMessageId = "callbackMessageId";
  String callbackHistoryMessages = "callbackHistoryMessages";
  String callbackMemberCount = "callbackMemberCount";
  String callbackOnlineClients = "callbackOnlineClient";
  String callbackCreatedAt = "callbackCreatedAt";
  String callbackUpdatedAt = "callbackUpdatedAt";
  String callbackClientStatus = "callbackClientStatus";
  String callbackDeliveredAt = "callbackDeliveredAt";
  String callbackReadAt = "callbackReadAt";

  //  String callbackTemporary = "callbackTemporary";
  String callbackTemporaryTTL = "callbackTemporaryTTL";
  //  String callbackTransient = "callbackTransient";
//  String callbackSystem = "callbackSystem";
  String callbackConvType = "callbackConvType";
  String callbackConvMemberMuted_SUCC = "callbackConvMemberMutedSUCC";
  String callbackConvMemberMuted_FAIL = "callbackConvMemberMutedFAIL";

  String QUERY_PARAM_OFFSET = "skip";
  String QUERY_PARAM_LIMIT = "limit";
  String QUERY_PARAM_SORT = "sort";
  String QUERY_PARAM_WHERE = "where";
  String QUERY_PARAM_LAST_MESSAGE = "last_message";
  String QUERY_PARAM_COMPACT = "compact";
  String QUERY_PARAM_TEMPCONV = "temp_id";

  String NAME = "name";
  String ATTRIBUTE = "attr";
  String MEMBERS = "m";
  String MUTE = "mu";
  String TRANSIENT = "tr";
  String LAST_MESSAGE_AT = "lm";
  String SYSTEM = "sys";
  String CREATOR = "c";
  String TEMPORARY = "temp";
  String TEMPORARYTTL = "ttl";

  String TEMPCONV_ID_PREFIX = "_tmp:";

  String[] CONVERSATION_COLUMNS = {MEMBERS, NAME, CREATOR,
          LAST_MESSAGE_AT, AVObject.KEY_OBJECT_ID, MUTE, AVObject.KEY_UPDATED_AT,
          AVObject.KEY_CREATED_AT, ATTRIBUTE, TRANSIENT, SYSTEM, TEMPORARY, TEMPORARYTTL};
}
