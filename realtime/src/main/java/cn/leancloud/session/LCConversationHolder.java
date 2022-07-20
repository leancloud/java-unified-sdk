package cn.leancloud.session;

import cn.leancloud.LCException;
import cn.leancloud.LCLogger;
import cn.leancloud.Messages;
import cn.leancloud.codec.Base64Decoder;
import cn.leancloud.command.*;
import cn.leancloud.command.ConversationControlPacket.ConversationControlOp;
import cn.leancloud.im.*;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.Conversation.LCIMOperation;
import cn.leancloud.im.v2.callback.LCIMCommonJsonCallback;
import cn.leancloud.session.IMOperationQueue.Operation;
import cn.leancloud.im.v2.callback.LCIMOperationFailure;
import cn.leancloud.im.v2.conversation.LCIMConversationMemberInfo;
import cn.leancloud.im.v2.conversation.ConversationMemberRole;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.im.SignatureFactory.SignatureException;

import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;

import java.util.*;

public class LCConversationHolder {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCConversationHolder.class);

  // 服务器端为了兼容老版本，这里需要使用group的invite
  private static final String GROUP_INVITE = "invite";
  private static final String GROUP_KICK = "kick";
  private static final String BLOCK_MEMBER = "conversation-block-clients";
  private static final String UNBLOCK_MEMBER = "conversation-unblock-clients";

  LCSession session;
  String conversationId;
  int convType;

  public LCConversationHolder(String conversationId, LCSession session, int convType) {
    this.session = session;
    this.conversationId = conversationId;
    this.conversationGene = getConversationGeneString();
    this.convType = convType;
  }

  public void addMembers(final List<String> members, final int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_ADD_MEMBER, requestId)) {
      return;
    }
    SignatureCallback callback = new SignatureCallback() {

      @Override
      public void onSignatureReady(Signature sig, LCException e) {
        if (e == null) {
          session.conversationOperationCache.offer(Operation.getOperation(
                  LCIMOperation.CONVERSATION_ADD_MEMBER.getCode(), session.getSelfPeerId(),
                  conversationId, requestId));
          session.sendPacket(ConversationControlPacket.genConversationCommand(
                  session.getSelfPeerId(), conversationId, members,
                  ConversationControlPacket.ConversationControlOp.ADD, null, sig, requestId));
        } else {
          InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId,
                  requestId, LCIMOperation.CONVERSATION_ADD_MEMBER, e);
        }
      }

      @Override
      public Signature computeSignature() throws SignatureException {
        final SignatureFactory signatureFactory = LCIMOptions.getGlobalOptions().getSignatureFactory();
        if (null != signatureFactory) {
          // 服务器端为了兼容老版本，这里需要使用group的invite
          return signatureFactory.createConversationSignature(conversationId,
                  session.getSelfPeerId(), members, GROUP_INVITE);
        }
        return null;
      }
    };
    new SignatureTask(callback, session.getSelfPeerId()).start();
  }

  public void kickMembers(final List<String> members, final int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_RM_MEMBER, requestId)) {
      return;
    }
    SignatureCallback callback = new SignatureCallback() {

      @Override
      public void onSignatureReady(Signature sig, LCException e) {
        if (e == null) {
          session.conversationOperationCache.offer(Operation.getOperation(
                  LCIMOperation.CONVERSATION_RM_MEMBER.getCode(), session.getSelfPeerId(),
                  conversationId, requestId));
          session.sendPacket(ConversationControlPacket.genConversationCommand(
                  session.getSelfPeerId(), conversationId, members,
                  ConversationControlOp.REMOVE, null, sig, requestId));
        } else {
          InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId,
                  requestId, LCIMOperation.CONVERSATION_RM_MEMBER, e);
        }
      }

      @Override
      public Signature computeSignature() throws SignatureException {
        // 服务器端为兼容老版本，签名使用kick的action
        final SignatureFactory signatureFactory = LCIMOptions.getGlobalOptions().getSignatureFactory();
        if (signatureFactory != null) {
          return signatureFactory.createConversationSignature(conversationId,
                  session.getSelfPeerId(), members, GROUP_KICK);
        }
        return null;
      }
    };
    new SignatureTask(callback, session.getSelfPeerId()).start();
  }

  public void muteMembers(final List<String> members, final int requestId){
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_MUTE_MEMBER, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_MUTE_MEMBER.getCode(), session.getSelfPeerId(),
            conversationId, requestId));
    session.sendPacket(ConversationControlPacket.genConversationCommand(
            session.getSelfPeerId(), conversationId, members,
            ConversationControlOp.ADD_SHUTUP, null, null, requestId));
  }

  public void unmuteMembers(final List<String> members, final int requestId){
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_UNMUTE_MEMBER, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_UNMUTE_MEMBER.getCode(), session.getSelfPeerId(),
            conversationId, requestId));
    session.sendPacket(ConversationControlPacket.genConversationCommand(
            session.getSelfPeerId(), conversationId, members,
            ConversationControlOp.REMOVE_SHUTUP, null, null, requestId));
  }

  public void blockMembers(final List<String> members, final int requestId){
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_BLOCK_MEMBER, requestId)) {
      return;
    }
    SignatureCallback callback = new SignatureCallback() {

      @Override
      public void onSignatureReady(Signature sig, LCException e) {
        if (e == null) {
          session.conversationOperationCache.offer(Operation.getOperation(
                  LCIMOperation.CONVERSATION_BLOCK_MEMBER.getCode(), session.getSelfPeerId(),
                  conversationId, requestId));
          session.sendPacket(BlacklistCommandPacket.genBlacklistCommandPacket(
                  session.getSelfPeerId(), conversationId,
                  BlacklistCommandPacket.BlacklistCommandOp.BLOCK, members, sig, requestId));
        } else {
          InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId,
                  requestId, LCIMOperation.CONVERSATION_BLOCK_MEMBER, e);
        }
      }

      @Override
      public Signature computeSignature() throws SignatureException {
        final SignatureFactory signatureFactory = LCIMOptions.getGlobalOptions().getSignatureFactory();
        if (signatureFactory != null) {
          return signatureFactory.createBlacklistSignature(session.getSelfPeerId(), conversationId, members, BLOCK_MEMBER);
        }
        return null;
      }
    };
    new SignatureTask(callback, session.getSelfPeerId()).start();
  }

  public void unblockMembers(final List<String> members, final int requestId){
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_UNBLOCK_MEMBER, requestId)) {
      return;
    }

    SignatureCallback callback = new SignatureCallback() {

      @Override
      public void onSignatureReady(Signature sig, LCException e) {
        if (e == null) {
          session.conversationOperationCache.offer(Operation.getOperation(
                  LCIMOperation.CONVERSATION_UNBLOCK_MEMBER.getCode(), session.getSelfPeerId(),
                  conversationId, requestId));
          session.sendPacket(BlacklistCommandPacket.genBlacklistCommandPacket(
                  session.getSelfPeerId(), conversationId,
                  BlacklistCommandPacket.BlacklistCommandOp.UNBLOCK, members, sig, requestId));
        } else {
          InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId,
                  requestId, LCIMOperation.CONVERSATION_UNBLOCK_MEMBER, e);
        }
      }

      @Override
      public Signature computeSignature() throws SignatureException {
        final SignatureFactory signatureFactory = LCIMOptions.getGlobalOptions().getSignatureFactory();
        if (signatureFactory != null) {
          return signatureFactory.createBlacklistSignature(session.getSelfPeerId(), conversationId, members, UNBLOCK_MEMBER);
        }
        return null;
      }
    };
    new SignatureTask(callback, session.getSelfPeerId()).start();
  }

  public void join(final int requestId) {

    SignatureCallback callback = new SignatureCallback() {

      @Override
      public void onSignatureReady(Signature sig, LCException e) {
        if (e == null) {
          session.conversationOperationCache.offer(Operation.getOperation(
                  LCIMOperation.CONVERSATION_JOIN.getCode(), session.getSelfPeerId(), conversationId,
                  requestId));
          session.sendPacket(ConversationControlPacket.genConversationCommand(
                  session.getSelfPeerId(), conversationId, Arrays.asList(session.getSelfPeerId()),
                  ConversationControlOp.ADD, null, sig, requestId));
        } else {
          InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId,
                  requestId, LCIMOperation.CONVERSATION_JOIN, e);
        }
      }

      @Override
      public Signature computeSignature() throws SignatureException {
        final SignatureFactory signatureFactory = LCIMOptions.getGlobalOptions().getSignatureFactory();
        if (null != signatureFactory) {
          // 服务器端为了兼容老版本，这里需要使用group的invite
          return signatureFactory.createConversationSignature(conversationId,
                  session.getSelfPeerId(), Arrays.asList(session.getSelfPeerId()),
                  GROUP_INVITE);
        }
        return null;
      }
    };
    new SignatureTask(callback, session.getSelfPeerId()).start();
  }

  public void queryMutedMembers(int offset, int limit, int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_MUTED_MEMBER_QUERY, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_MUTED_MEMBER_QUERY.getCode(), session.getSelfPeerId(), conversationId,
            requestId));
    ConversationControlPacket packet = ConversationControlPacket.genConversationCommand(session.getSelfPeerId(),
            conversationId, null, ConversationControlOp.QUERY_SHUTUP, null, null, requestId);
    packet.setQueryOffset(offset);
    packet.setQueryLimit(limit);
    session.sendPacket(packet);
  }

  public void queryBlockedMembers(int offset, int limit, int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_BLOCKED_MEMBER_QUERY, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_BLOCKED_MEMBER_QUERY.getCode(), session.getSelfPeerId(), conversationId,
            requestId));
    BlacklistCommandPacket packet = BlacklistCommandPacket.genBlacklistCommandPacket(session.getSelfPeerId(),
            conversationId, BlacklistCommandPacket.BlacklistCommandOp.QUERY, offset, limit, requestId);
    session.sendPacket(packet);
  }

  public void updateInfo(Map<String, Object> attr, int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_UPDATE, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_UPDATE.getCode(), session.getSelfPeerId(), conversationId,
            requestId));
    session.sendPacket(ConversationControlPacket.genConversationCommand(session.getSelfPeerId(),
            conversationId, null, ConversationControlOp.UPDATE, attr, null, requestId));

  }

  public void promoteMember(Map<String, Object> member, int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_PROMOTE_MEMBER, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_PROMOTE_MEMBER.getCode(), session.getSelfPeerId(), conversationId,
            requestId));
    ConversationControlPacket ccp = ConversationControlPacket.genConversationMemberCommand(session.getSelfPeerId(),
            conversationId, ConversationControlOp.MEMBER_UPDATE, member, null, requestId);
    session.sendPacket(ccp);
  }

  public void sendMessage(LCIMMessage message, int requestId, LCIMMessageOption messageOption) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_SEND_MESSAGE, requestId)) {
      return;
    }
    byte[] binaryMessage = null;
    if (message instanceof LCIMBinaryMessage) {
      binaryMessage = ((LCIMBinaryMessage) message).getBytes();
    }

    session.storeMessage((PendingMessageCache.Message.getMessage(message.getContent(),
            String.valueOf(requestId), messageOption.isReceipt(), conversationId)), requestId);

    session.sendPacket(ConversationDirectMessagePacket.getConversationMessagePacket(
                    session.getSelfPeerId(),
                    conversationId,
                    message.getContent(), binaryMessage, message.isMentionAll(), message.getMentionList(),
                    LCIMMessageManagerHelper.getMessageToken(message),
                    messageOption,
                    requestId));
  }

  public void patchMessage(LCIMMessage oldMessage, LCIMMessage newMessage, LCIMMessage recallMessage,
                           LCIMOperation operation, int requestId) {
    if (!checkSessionStatus(operation, requestId)) {
      return;
    }

    session.conversationOperationCache.offer(Operation.getOperation(
            operation.getCode(), session.getSelfPeerId(), conversationId, requestId));

    if (operation.equals(LCIMOperation.CONVERSATION_RECALL_MESSAGE)) {
      String messageId = recallMessage.getMessageId();
      long timeStamp = recallMessage.getTimestamp();

      session.sendPacket(
              MessagePatchModifyPacket.getMessagePatchPacketForRecall(session.getSelfPeerId(), conversationId, messageId,
                      timeStamp, requestId));
    } else if (operation.equals(LCIMOperation.CONVERSATION_UPDATE_MESSAGE)){
      String messageId = oldMessage.getMessageId();
      long timeStamp = oldMessage.getTimestamp();
      String data = newMessage.getContent();
      boolean mentionAll = newMessage.isMentionAll();
      List<String> mentionList = newMessage.getMentionList();
      byte[] binaryData = null;
      if (newMessage instanceof LCIMBinaryMessage) {
        binaryData = ((LCIMBinaryMessage) newMessage).getBytes();
      }

      session.sendPacket(MessagePatchModifyPacket.getMessagePatchPacketForUpdate(session.getSelfPeerId(),
              conversationId, messageId, data, binaryData, mentionAll, mentionList, timeStamp, requestId));
    }
  }

  public void quit(final int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_QUIT, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_QUIT.getCode(), session.getSelfPeerId(), conversationId,
            requestId));
    session.sendPacket(ConversationControlPacket.genConversationCommand(
            session.getSelfPeerId(), conversationId, Arrays.asList(session.getSelfPeerId()),
            ConversationControlOp.REMOVE, null, null, requestId));
  }

  public void queryHistoryMessages(String msgId, long timestamp, int limit, String toMsgId,
                                   long toTimestamp, int requestId) {
    queryHistoryMessages(msgId, timestamp, false, toMsgId, toTimestamp, false,
            LCIMMessageQueryDirection.DirectionFromNewToOld.getCode(), limit, 0, requestId);
  }

  public void queryHistoryMessages(String msgId, long timestamp, boolean sclosed,
                                   String toMsgId, long toTimestamp, boolean toclosed,
                                   int direct, int limit, int msgType, int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_MESSAGE_QUERY, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_MESSAGE_QUERY.getCode(), session.getSelfPeerId(),
            conversationId, requestId));
    session.sendPacket(ConversationMessageQueryPacket.getConversationMessageQueryPacket(
            session.getSelfPeerId(), conversationId, msgId, timestamp, sclosed, toMsgId, toTimestamp, toclosed,
            direct, limit, msgType, requestId));
  }

  public void mute(int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_MUTE, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_MUTE.getCode(), session.getSelfPeerId(), conversationId,
            requestId));
    session.sendPacket(ConversationControlPacket.genConversationCommand(session.getSelfPeerId(),
            conversationId, null, ConversationControlOp.MUTE, null, null, requestId));

  }

  public void unmute(int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_UNMUTE, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_UNMUTE.getCode(), session.getSelfPeerId(), conversationId,
            requestId));
    session.sendPacket(ConversationControlPacket.genConversationCommand(session.getSelfPeerId(),
            conversationId, null, ConversationControlOp.UNMUTE, null, null, requestId));
  }

  public void getMemberCount(int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_MEMBER_COUNT_QUERY, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_MEMBER_COUNT_QUERY.getCode(), session.getSelfPeerId(),
            conversationId,
            requestId));
    session.sendPacket(ConversationControlPacket.genConversationCommand(session.getSelfPeerId(),
            conversationId, null, ConversationControlOp.COUNT, null, null, requestId));

  }

  public void getReceiptTime(int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_FETCH_RECEIPT_TIME, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_FETCH_RECEIPT_TIME.getCode(), session.getSelfPeerId(), conversationId,
            requestId));
    session.sendPacket(ConversationControlPacket.genConversationCommand(session.getSelfPeerId(),
            conversationId, null, ConversationControlOp.MAX_READ, null, null, requestId));

  }

  private void read(String msgId, long timestamp, int requestId) {
    if (!checkSessionStatus(LCIMOperation.CONVERSATION_READ, requestId)) {
      return;
    }
    session.conversationOperationCache.offer(Operation.getOperation(
            LCIMOperation.CONVERSATION_READ.getCode(), session.getSelfPeerId(), conversationId, requestId));

    UnreadMessagesClearPacket packet =
            UnreadMessagesClearPacket.getUnreadClearPacket(session.getSelfPeerId(), conversationId, msgId, timestamp, requestId);
    session.sendPacket(packet);

    // 因为没有返回值，所以在发送 command 后直接置 unreadCount 为 0 并发送事件
    onUnreadMessagesEvent(null, 0, false);

  }

  private boolean checkSessionStatus(LCIMOperation operation, int requestId) {
    if (session.getCurrentStatus() == LCSession.Status.Closed) {
      RuntimeException se = new RuntimeException("Connection Lost");
      InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId,
              requestId, operation, se);
      return false;
    } else {
      return true;
    }
  }

  public void processConversationCommandFromClient(LCIMOperation imop, Map<String, Object> params,
                                                   int requestId) {
    List<String> members = null != params ? ((List<String>) params.get(Conversation.PARAM_CONVERSATION_MEMBER)) : null;
    switch (imop) {
      case CONVERSATION_JOIN:
        join(requestId);
        break;
      case CONVERSATION_ADD_MEMBER:
        addMembers(members, requestId);
        break;
      case CONVERSATION_RM_MEMBER:
        kickMembers(members, requestId);
        break;
      case CONVERSATION_QUIT:
        quit(requestId);
        break;
      case CONVERSATION_UPDATE:
        Map<String, Object> attr =
                (Map<String, Object>) params.get(Conversation.PARAM_CONVERSATION_ATTRIBUTE);
        this.updateInfo(attr, requestId);
        break;
      case CONVERSATION_MUTE:
        mute(requestId);
        break;
      case CONVERSATION_UNMUTE:
        unmute(requestId);
        break;
      case CONVERSATION_MEMBER_COUNT_QUERY:
        getMemberCount(requestId);
        break;
      case CONVERSATION_FETCH_RECEIPT_TIME:
        getReceiptTime(requestId);
        break;
      case CONVERSATION_READ:
        String messageId = "";
        if (null != params && params.containsKey(Conversation.PARAM_MESSAGE_QUERY_MSGID)) {
          messageId = (String)params.get(Conversation.PARAM_MESSAGE_QUERY_MSGID);
        }
        long messageTS = 0;
        if (null != params && params.containsKey(Conversation.PARAM_MESSAGE_QUERY_TIMESTAMP)) {
          messageTS = ((Number) params.get(Conversation.PARAM_MESSAGE_QUERY_TIMESTAMP)).longValue();
        }
        read(messageId, messageTS, requestId);
        break;
      case CONVERSATION_PROMOTE_MEMBER:
        Map<String, Object> memberInfo = null != params? (Map<String, Object>) params.get(Conversation.PARAM_CONVERSATION_MEMBER_DETAILS) : null;
        if (null != memberInfo) {
          promoteMember(memberInfo, requestId);
        }
        break;
      case CONVERSATION_MUTE_MEMBER:
        muteMembers(members, requestId);
        break;
      case CONVERSATION_UNMUTE_MEMBER:
        unmuteMembers(members, requestId);
        break;
      case CONVERSATION_BLOCK_MEMBER:
        blockMembers(members, requestId);
        break;
      case CONVERSATION_UNBLOCK_MEMBER:
        unblockMembers(members, requestId);
        break;
      case CONVERSATION_MUTED_MEMBER_QUERY:
        int offset = (Integer) params.get(Conversation.QUERY_PARAM_OFFSET);
        int sizeLimit = (Integer) params.get(Conversation.QUERY_PARAM_LIMIT);
        queryMutedMembers(offset, sizeLimit, requestId);
        break;
      case CONVERSATION_BLOCKED_MEMBER_QUERY:
        int blockedOffset = (Integer) params.get(Conversation.QUERY_PARAM_OFFSET);
        int blockedSizeLimit = (Integer) params.get(Conversation.QUERY_PARAM_LIMIT);
        queryBlockedMembers(blockedOffset, blockedSizeLimit, requestId);
        break;
      case CONVERSATION_MESSAGE_QUERY:
        // timestamp = 0时，原来的 (Long) 会发生强制转型错误(Integer cannot cast to Long)
        String msgId = (String) params.get(Conversation.PARAM_MESSAGE_QUERY_MSGID);
        long ts = ((Number) params.get(Conversation.PARAM_MESSAGE_QUERY_TIMESTAMP)).longValue();
        boolean sclosed = (Boolean) params.get(Conversation.PARAM_MESSAGE_QUERY_STARTCLOSED);
        String toMsgId = (String) params.get(Conversation.PARAM_MESSAGE_QUERY_TO_MSGID);
        long tts = ((Number) params.get(Conversation.PARAM_MESSAGE_QUERY_TO_TIMESTAMP)).longValue();
        boolean tclosed = (Boolean) params.get(Conversation.PARAM_MESSAGE_QUERY_TOCLOSED);
        int direct = (Integer) params.get(Conversation.PARAM_MESSAGE_QUERY_DIRECT);
        int limit = (Integer) params.get(Conversation.PARAM_MESSAGE_QUERY_LIMIT);
        int msgType = (Integer) params.get(Conversation.PARAM_MESSAGE_QUERY_TYPE);
        queryHistoryMessages(msgId, ts, sclosed, toMsgId, tts, tclosed, direct, limit, msgType, requestId);
        break;
      default:
        break;
    }
  }

  public void processConversationCommandFromServer(LCIMOperation imop, String operation, int requestId, Messages.ConvCommand convCommand) {
    if (ConversationControlOp.STARTED.equals(operation)) {
      // need convCommand to instantiate conversation object.
      onConversationCreated(requestId, convCommand);
    } else if (ConversationControlOp.JOINED.equals(operation)) {
      String invitedBy = convCommand.getInitBy();
      onInvitedToConversation(invitedBy, convCommand);
    } else if (ConversationControlOp.REMOVED.equals(operation)) {
      if (requestId != CommandPacket.UNSUPPORTED_OPERATION) {
        if (null == imop) {
          LOGGER.e("IllegalState. operation is null, excepted is QUIT / KICK, originalOp=" + operation);
        } else if (imop.getCode() == LCIMOperation.CONVERSATION_QUIT.getCode()) {
          onQuit(requestId);
        } else if (imop.getCode() == LCIMOperation.CONVERSATION_RM_MEMBER.getCode()) {
          onKicked(requestId, convCommand);
        }
      }
    } else if (ConversationControlOp.ADDED.equals(operation)) {
      // 这里我们回过头去看发送的命令是什么，如果是join，则是自己把自己加入到某个conversation。否则是邀请成功
      if (requestId != CommandPacket.UNSUPPORTED_OPERATION) {
        if (null == imop) {
          LOGGER.e("IllegalState. operation is null, excepted is JOIN / INVITE, originalOp=" + operation);
        } else if (imop.getCode() == LCIMOperation.CONVERSATION_JOIN.getCode()) {
          onJoined(requestId);
        } else if (imop.getCode() == LCIMOperation.CONVERSATION_ADD_MEMBER.getCode()) {
          onInvited(requestId, convCommand);
        }
      }
    } else if (ConversationControlOp.LEFT.equals(operation)) {
      String invitedBy = convCommand.getInitBy();
      if (invitedBy != null) {
        onKickedFromConversation(invitedBy);
      }
    } else if (ConversationControlOp.UPDATED.equals(operation)) {
      if (null == imop) {
        // LogUtil.log.e("IllegalState. operation is null, excepted is MUTE / UNMUTE / UPDATE, originalOp=" + operation);
        // conv/updated notification
        onInfoChangedNotify(convCommand);
      } else {
        mergeServerData(convCommand);
        if (LCIMOperation.CONVERSATION_MUTE.getCode() == imop.getCode()) {
          onMuted(requestId);
        } else if (LCIMOperation.CONVERSATION_UNMUTE.getCode() == imop.getCode()) {
          onUnmuted(requestId);
        } else if (LCIMOperation.CONVERSATION_UPDATE.getCode() == imop.getCode()) {
          onInfoUpdated(requestId, convCommand.getUdate());
        }
      }
    } else if (ConversationControlOp.MEMBER_COUNT_QUERY_RESULT.equals(operation)) {
      int memberCount = convCommand.getCount();
      onMemberCount(memberCount, requestId);
    } else if (ConversationControlOp.MAX_READ.equals(operation)) {
      long receiptTime = convCommand.getMaxAckTimestamp();
      long readTime = convCommand.getMaxReadTimestamp();
      onTimesReceipt(requestId,  receiptTime, readTime);
    } else if (ConversationControlOp.MEMBER_UPDATED.equals(operation)) {
      onMemberUpdated(requestId);
    } else if (ConversationControlOp.SHUTUP_ADDED.equals(operation)
            || ConversationControlOp.SHUTUP_REMOVED.equals(operation)) {
      // 禁言/取消禁言的 response
      if (null == imop) {
        LOGGER.e("IllegalState. operation is null, excepted is member_shutupped / member_unshutuped, originalOp=" + operation);
      } else {
        onResponse4MemberMute(imop, operation, requestId, convCommand);
      }
    }
    // 下面都是被动
    else if (ConversationControlOp.MEMBER_JOINED.equals(operation)) {
      String invitedBy = convCommand.getInitBy();
      List<String> joinedMembers = convCommand.getMList();
      onMembersJoined(joinedMembers, invitedBy);
    } else if (ConversationControlOp.MEMBER_LEFTED.equals(operation)) {
      String removedBy = convCommand.getInitBy();
      List<String> leftMembers = convCommand.getMList();
      onMembersLeft(leftMembers, removedBy);
    } else if (ConversationControlOp.MEMBER_INFO_CHANGED.equals(operation)) {
      String changedBy = convCommand.getInitBy();
      Messages.ConvMemberInfo member = convCommand.getInfo();
      onMemberChanged(changedBy, member);
    } else if (ConversationControlOp.SHUTUPED.equals(operation)
            || ConversationControlOp.UNSHUTUPED.equals(operation)) {
      String operator = convCommand.getInitBy();
      // always notify application, it helps for multi-login.
      onSelfShutupedNotify(ConversationControlOp.SHUTUPED.equals(operation), operator, convCommand);
//      if (null != operator && operator.equals(session.getSelfPeerId())) {
//        return;
//      } else {
//        onSelfShutupedNotify(ConversationControlOp.SHUTUPED.equals(operation), operator, convCommand);
//      }
    } else if (ConversationControlOp.MEMBER_SHUTPED.equals(operation)
            || ConversationControlOp.MEMBER_UNSHUTUPED.equals(operation)) {
      String operator = convCommand.getInitBy();
      // always notify application, it helps for multi-login.
      onMemberShutupedNotify(ConversationControlOp.MEMBER_SHUTPED.equals(operation), operator, convCommand);
//      if (null != operator && operator.equals(session.getSelfPeerId())) {
//        return;
//      } else {
//        onMemberShutupedNotify(ConversationControlOp.MEMBER_SHUTPED.equals(operation), operator, convCommand);
//      }
    } else if (ConversationControlOp.BLOCKED.equals(operation)
            || ConversationControlOp.UNBLOCKED.equals(operation)) {
      String operator = convCommand.getInitBy();
      // always notify application, it helps for multi-login.
      onSelfBlockedNotify(ConversationControlOp.BLOCKED.equals(operation), operator, convCommand);
//      if (null != operator && operator.equals(session.getSelfPeerId())) {
//        return;
//      } else {
//        onSelfBlockedNotify(ConversationControlOp.BLOCKED.equals(operation), operator, convCommand);
//      }
    } else if (ConversationControlOp.MEMBER_BLOCKED_NOTIFY.equals(operation)
            || ConversationControlOp.MEMBER_UNBLOCKED_NOTIFY.equals(operation)) {
      String operator = convCommand.getInitBy();
      // always notify application, it helps for multi-login.
      onMemberBlockedNotify(ConversationControlOp.MEMBER_BLOCKED_NOTIFY.equals(operation), operator, convCommand);
//      if (null != operator && operator.equals(session.getSelfPeerId())) {
//        return;
//      } else {
//        onMemberBlockedNotify(ConversationControlOp.MEMBER_BLOCKED_NOTIFY.equals(operation), operator, convCommand);
//      }
    }
  }

  void onResponse4MemberBlock(LCIMOperation imop, String operation, int reqeustId, Messages.BlacklistCommand blacklistCommand) {
    if (null == blacklistCommand) {
      return;
    }
    List<String> allowedList = blacklistCommand.getAllowedPidsList();
    List<Messages.ErrorCommand> errorCommandList = blacklistCommand.getFailedPidsList();
    HashMap<String, Object> bundle = genPartiallyResult(allowedList, errorCommandList);
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), blacklistCommand.getSrcCid(),
            reqeustId, imop, bundle);
  }

  void onResponse4MemberMute(LCIMOperation imop, String operation, int requestId, Messages.ConvCommand convCommand) {
    if (null == convCommand) {
      return;
    }
    List<String> allowedList = convCommand.getAllowedPidsList();
    List<Messages.ErrorCommand> errorCommandList = convCommand.getFailedPidsList();
    HashMap<String, Object> bundle = genPartiallyResult(allowedList, errorCommandList);
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), conversationId, requestId,
            imop, bundle);
  }

  private HashMap<String, Object> genPartiallyResult(List<String> allowedList, List<Messages.ErrorCommand> errorCommandList) {
    String[] allowedMembers = new String[null == allowedList? 0 : allowedList.size()];
    if (null != allowedList) {
      allowedList.toArray(allowedMembers);
    }
    int errorCommandSize = (null == errorCommandList)? 0 : errorCommandList.size();
    ArrayList<LCIMOperationFailure> failedList = new ArrayList<>(errorCommandSize);
    if (null != errorCommandList) {
      for (Messages.ErrorCommand cmd: errorCommandList) {
        LCIMOperationFailure failure = new LCIMOperationFailure();
        failure.setCode(cmd.getCode());
        failure.setMemberIds(cmd.getPidsList());
        failure.setReason(cmd.getReason());
        failedList.add(failure);
      }
    }
    HashMap<String, Object> bundle = new HashMap<>();
    bundle.put(Conversation.callbackConvMemberPartial_SUCC, allowedMembers);
    bundle.put(Conversation.callbackConvMemberPartial_FAIL, failedList);
    return bundle;
  }

  public void processMessages(Integer requestKey, List<Messages.LogItem> logItems) {
    ArrayList<LCIMMessage> messageList = new ArrayList<LCIMMessage>();

    //这里记录的是对方 ack 及 read 的时间，而非自己 
    long lastDeliveredAt = -1;
    long lastReadAt = -1;
    for (Messages.LogItem item : logItems) {
      long ackAt = item.hasAckAt() ? -1 : item.getAckAt();
      long readAt = item.hasReadAt() ?-1 : item.getReadAt();
      if (lastDeliveredAt < ackAt) {
        lastDeliveredAt = ackAt;
      }
      if (lastReadAt < readAt) {
        lastReadAt = readAt;
      }

      String from = item.getFrom();
      String data = item.getData();
      long timestamp = item.getTimestamp();
      String msgId = item.getMsgId();
      long updatedTime = item.hasPatchTimestamp()? item.getPatchTimestamp() : 0l;
      boolean mentionAll = item.hasMentionAll() && item.getMentionAll();
      List<String> mentionList = item.getMentionPidsList();
      boolean isBinaryMsg = item.hasBin() && item.getBin();

      LCIMMessage message = null;
      if (isBinaryMsg && null != data) {
        message = new LCIMBinaryMessage(this.conversationId, from, timestamp, ackAt, readAt);
        ((LCIMBinaryMessage)message).setBytes(Base64Decoder.decodeToBytes(data));
      } else {
        message = new LCIMMessage(this.conversationId, from, timestamp, ackAt, readAt);
        message.setContent(data);
      }
      message.setMessageId(msgId);
      message.setMentionAll(mentionAll);
      message.setMentionList(mentionList);
      if (updatedTime > 0l) {
        message.setUpdateAt(updatedTime);
      }

      message = LCIMMessageManagerHelper.parseTypedMessage(message);
      messageList.add(message);
    }
    onHistoryMessageQuery(messageList, requestKey, lastDeliveredAt, lastReadAt);
  }

  void onConversationCreated(int requestId, Messages.ConvCommand convCommand) {
    String createdAt = convCommand.getCdate();
    String cid = convCommand.getCid();
    int tempTTL = convCommand.hasTempConvTTL()? convCommand.getTempConvTTL(): 0;
    String uniqueId = convCommand.hasUniqueId() ? convCommand.getUniqueId() : null;

    // they are not necessary for create-callback(isTemp, isTransient), except for tempTTL.
    HashMap<String, Object> bundle = new HashMap<>();
    bundle.put(Conversation.callbackCreatedAt, createdAt);
    bundle.put(Conversation.callbackConversationKey, cid);
    bundle.put(Conversation.callbackTemporaryTTL, tempTTL);
    if (!StringUtil.isEmpty(uniqueId)) {
      bundle.put(Conversation.callbackUniqueId, uniqueId);
    }
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_CREATION, bundle);
  }

  void onJoined(int requestId) {
    LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
    final LCIMConversation conversation = client.getConversation(this.conversationId);
    ConversationSynchronizer.mergeMembers(conversation, Arrays.asList(session.getSelfPeerId()));

    InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_JOIN, null);
  }
  void onInvited(int requestId, Messages.ConvCommand convCommand) {
    List<String> allowedList = convCommand.getAllowedPidsList();
    List<Messages.ErrorCommand> errorCommandList = convCommand.getFailedPidsList();

    LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
    final LCIMConversation conversation = client.getConversation(this.conversationId);
    ConversationSynchronizer.mergeMembers(conversation, allowedList);

    HashMap<String, Object> bundle = genPartiallyResult(allowedList, errorCommandList);
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_ADD_MEMBER, bundle);
  }

  void onKicked(int requestId, Messages.ConvCommand convCommand) {
    List<String> allowedList = convCommand.getAllowedPidsList();
    List<Messages.ErrorCommand> errorCommandList = convCommand.getFailedPidsList();

    LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
    final LCIMConversation conversation = client.getConversation(this.conversationId);
    ConversationSynchronizer.removeMembers(conversation, allowedList);

    HashMap<String, Object> bundle = genPartiallyResult(allowedList, errorCommandList);
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_RM_MEMBER, bundle);
  }
  void onQuit(int requestId) {
    InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_QUIT, null);
  }
  private void onInfoUpdated(int requestId, String updatedAt) {
    HashMap<String, Object> bundle = new HashMap<>();
    bundle.put(Conversation.callbackUpdatedAt, updatedAt);
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_UPDATE, bundle);
  }
  private void onMemberUpdated(int requestId) {
    InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_PROMOTE_MEMBER, null);
  }
  private void onMemberChanged(final String operator, Messages.ConvMemberInfo member) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (handler != null) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = client.getConversation(this.conversationId);
      final String objectId = member.getInfoId();
      final String roleStr = member.getRole();
      final String peerId = member.getPid();
      final LCIMConversationMemberInfo memberInfo = new LCIMConversationMemberInfo(objectId, conversationId,
              peerId, ConversationMemberRole.fromString(roleStr));
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          handler.processEvent(Conversation.STATUS_ON_MEMBER_INFO_CHANGED, operator, memberInfo, conversation);
        }
      });
    }
  }
  void onMuted(int requestId) {
    InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_MUTE, null);
  }
  void onUnmuted(int requestId) {
    InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_UNMUTE, null);
  }
  void onMemberCount(int count, int requestId) {
    HashMap<String, Object> bundle = new HashMap<>();
    bundle.put(Conversation.callbackMemberCount, count);
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_MEMBER_COUNT_QUERY, bundle);
  }
  void onMessageSent(int requestId, String msgId, long timestamp) {
    HashMap<String, Object> bundle = new HashMap<>();
    bundle.put(Conversation.callbackMessageTimeStamp, timestamp);
    bundle.put(Conversation.callbackMessageId, msgId);
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_SEND_MESSAGE, bundle);
  }
  void onHistoryMessageQuery(ArrayList<LCIMMessage> messages, int requestId, long deliveredAt, long readAt) {
    HashMap<String, Object> bundle = new HashMap<>();
    bundle.put(Conversation.callbackHistoryMessages, messages);
    bundle.put(Conversation.callbackDeliveredAt, deliveredAt);
    bundle.put(Conversation.callbackReadAt, readAt);
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_MESSAGE_QUERY, bundle);
    session.sendUnreadMessagesAck(messages, conversationId);
  }

  void onTimesReceipt(int requestId, long deliveredAt, long readAt) {
    HashMap<String, Object> bundle = new HashMap<>();
    bundle.put(Conversation.callbackReadAt, readAt);
    bundle.put(Conversation.callbackDeliveredAt, deliveredAt);
    InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), conversationId, requestId,
            LCIMOperation.CONVERSATION_FETCH_RECEIPT_TIME, bundle);
  }
  void onInvitedToConversation(final String invitedBy, Messages.ConvCommand convCommand) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (handler != null) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = parseConversation(client, convCommand);
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          handler.processEvent(Conversation.STATUS_ON_JOINED, invitedBy, null, conversation);
        }
      });
    }
  }

  private void mergeServerData(Messages.ConvCommand convCommand) {
    LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
    final LCIMConversation conversation = client.getConversation(this.conversationId);
    Messages.JsonObjectMessage attrMsg = convCommand.getAttrModified();
    Messages.JsonObjectMessage allAttrs = convCommand.getAttr();
    JSONObject operand = null;
    JSONObject allAttrObject = null;
    if (null != allAttrs && !StringUtil.isEmpty(allAttrs.getData())) {
      allAttrObject = JSON.parseObject(allAttrs.getData());
    }
    if (null != attrMsg && !StringUtil.isEmpty(attrMsg.getData())) {
      operand = JSON.parseObject(attrMsg.getData());
    }
    ConversationSynchronizer.mergeConversationFromJsonObject(conversation, operand, allAttrObject);
    ConversationSynchronizer.changeUpdatedTime(conversation, convCommand.getUdate());
  }

  void onInfoChangedNotify(Messages.ConvCommand convCommand) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (null != handler) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = parseConversation(client, convCommand);
      final String operator = convCommand.getInitBy();
      // change from attr to attrModified.
      Messages.JsonObjectMessage attrModified = convCommand.getAttrModified();
      Messages.JsonObjectMessage allAttrs = convCommand.getAttr();
      String updatedAt = convCommand.getUdate();
      ConversationSynchronizer.changeUpdatedTime(conversation, updatedAt);
      JSONObject operand = null;
      JSONObject allAttrMap = null;
      if (null != allAttrs && !StringUtil.isEmpty(allAttrs.getData())) {
        allAttrMap = JSON.parseObject(allAttrs.getData());
      }
      if (null != attrModified && !StringUtil.isEmpty(attrModified.getData())) {
        operand = JSON.parseObject(attrModified.getData());
      }
      if (null == operand && null == allAttrMap) {
        // attached data is empty
        conversation.setMustFetch();
      } else {
        ConversationSynchronizer.mergeConversationFromJsonObject(conversation, operand, allAttrMap);
      }
      // Notice: SDK doesn't refresh conversation data automatically.
      handler.processEvent(Conversation.STATUS_ON_INFO_CHANGED, operator, operand, conversation);
    }
  }
  void onKickedFromConversation(final String invitedBy) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
    final LCIMConversation conversation = client.getConversation(this.conversationId);
    if (handler != null) {
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          handler.processEvent(Conversation.STATUS_ON_KICKED_FROM_CONVERSATION, invitedBy, null,
                  conversation);
        }
      });
    }
    session.removeConversation(conversationId);
    LCIMMessageManagerHelper.removeConversationCache(conversation);
  }
  void onSelfShutupedNotify(final boolean isMuted, final String operator, Messages.ConvCommand convCommand) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (handler != null) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = parseConversation(client, convCommand);
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          if (isMuted) {
            handler.processEvent(Conversation.STATUS_ON_MUTED, operator, null, conversation);
          } else {
            handler.processEvent(Conversation.STATUS_ON_UNMUTED, operator, null, conversation);
          }
        }
      });
    }
  }
  void onMemberShutupedNotify(final boolean isMuted, final String operator, Messages.ConvCommand convCommand) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    List<String> members = convCommand.getMList();
    if (handler != null && null != members) {
      final List<String> copyMembers = new ArrayList<>(members);
      copyMembers.remove(session.getSelfPeerId());
      if (copyMembers.size() < 1) {
        // ignore self member_shutuped notify, bcz server sends both shutuped and member_shutuped notification.
        LOGGER.d("Notification --- ignore shutuped/unshutuped notify bcz duplicated.");
      } else {
        LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
        final LCIMConversation conversation = parseConversation(client, convCommand);
        refreshConversationThenNotify(conversation, new SimpleCallback() {
          @Override
          public void done() {
            if (isMuted) {
              handler.processEvent(Conversation.STATUS_ON_MEMBER_MUTED, operator, copyMembers, conversation);
            } else {
              handler.processEvent(Conversation.STATUS_ON_MEMBER_UNMUTED, operator, copyMembers, conversation);
            }
          }
        });
      }
    }
  }
  void onSelfBlockedNotify(final boolean isBlocked, final String operator, Messages.ConvCommand convCommand) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (null == handler) {
      return;
    }
    LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
    final LCIMConversation conversation = parseConversation(client, convCommand);
    refreshConversationThenNotify(conversation, new SimpleCallback() {
      @Override
      public void done() {
        if (isBlocked) {
          handler.processEvent(Conversation.STATUS_ON_BLOCKED, operator, null, conversation);
        } else {
          handler.processEvent(Conversation.STATUS_ON_UNBLOCKED, operator, null, conversation);
        }
      }
    });
  }
  void onMemberBlockedNotify(final boolean isBlocked, final String operator, Messages.ConvCommand convCommand) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    final List<String> members = convCommand.getMList();
    if (handler != null && null != members) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = parseConversation(client, convCommand);
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          if (isBlocked) {
            handler.processEvent(Conversation.STATUS_ON_MEMBER_BLOCKED, operator, members, conversation);
          } else {
            handler.processEvent(Conversation.STATUS_ON_MEMBER_UNBLOCKED, operator, members, conversation);
          }
        }
      });
    }
  }
  void onMembersJoined(final List<String> members, final String invitedBy) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (handler != null) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = client.getConversation(this.conversationId);
      ConversationSynchronizer.mergeMembers(conversation, members);
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          handler.processEvent(Conversation.STATUS_ON_MEMBERS_JOINED, invitedBy, members, conversation);
        }
      });
    }
  }
  void onMembersLeft(final List<String> members, final String removedBy) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (handler != null) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = client.getConversation(this.conversationId);
      ConversationSynchronizer.removeMembers(conversation, members);
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          handler.processEvent(Conversation.STATUS_ON_MEMBERS_LEFT, removedBy, members, conversation);
        }
      });
    }
  }
  void onUnreadMessagesEvent(LCIMMessage message, int unreadCount, boolean mentioned) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (handler != null) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = client.getConversation(this.conversationId);
      if (conversation.getUnreadMessagesCount() != unreadCount) {
        final AbstractMap.SimpleEntry<Integer, Boolean> unreadInfo = new AbstractMap.SimpleEntry<>(unreadCount, mentioned);
        if (null != message) {
          message.setMessageIOType(LCIMMessage.MessageIOType.TypeIn);
          message.setMessageStatus(LCIMMessage.MessageStatus.StatusSent);
          message = LCIMMessageManagerHelper.parseTypedMessage(message);
        }
        final LCIMMessage msgCopy = message;

        refreshConversationThenNotify(conversation, new SimpleCallback() {
          @Override
          public void done() {
            handler.processEvent(Conversation.STATUS_ON_UNREAD_EVENT, msgCopy, unreadInfo, conversation);
          }
        });
      }
    }
  }
  void onMessageReceipt(final LCIMMessage message, final String from) {
    refreshConversationThenNotify(message, new SimpleCallback() {
      @Override
      public void done() {
        LCIMMessageManagerHelper.processMessageReceipt(message, LCIMClient.getInstance(session.getSelfPeerId()), from);
      }
    });
  }
  void onMessage(final LCIMMessage message, final boolean hasMore, final boolean isTransient) {
    message.setMessageIOType(LCIMMessage.MessageIOType.TypeIn);
    message.setMessageStatus(LCIMMessage.MessageStatus.StatusSent);

    final LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
    refreshConversationThenNotify(message, new SimpleCallback() {
      @Override
      public void done() {
        LCIMMessageManagerHelper.processMessage(message, convType, client, hasMore, isTransient);
      }
    });
  }
  void onMessageUpdateEvent(final LCIMMessage message, final boolean isRecall, long patchCode, String patchReason) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (handler != null) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = client.getConversation(this.conversationId);
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          if (isRecall) {
            handler.processEvent(Conversation.STATUS_ON_MESSAGE_RECALLED, message, null, conversation);
          } else {
            handler.processEvent(Conversation.STATUS_ON_MESSAGE_UPDATED, message, null, conversation);
          }
        }
      });
    }
  }
  void onConversationReadAtEvent(final long readAt) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (handler != null) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = client.getConversation(this.conversationId);
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          handler.processEvent(Conversation.STATUS_ON_MESSAGE_READ, readAt, null, conversation);
        }
      });
    }
  }
  void onConversationDeliveredAtEvent(final long deliveredAt) {
    final LCIMConversationEventHandler handler = LCIMMessageManagerHelper.getConversationEventHandler();
    if (handler != null) {
      LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
      final LCIMConversation conversation = client.getConversation(this.conversationId);
      refreshConversationThenNotify(conversation, new SimpleCallback() {
        @Override
        public void done() {
          handler.processEvent(Conversation.STATUS_ON_MESSAGE_DELIVERED, deliveredAt, null, conversation);
        }
      });
    }
  }
  private String conversationGene = null;

  private String getConversationGeneString() {
    if (StringUtil.isEmpty(conversationGene)) {
      HashMap<String, String> conversationGeneMap = new HashMap<String, String>();
      conversationGeneMap.put(Conversation.INTENT_KEY_CLIENT, session.getSelfPeerId());
      conversationGeneMap.put(Conversation.INTENT_KEY_CONVERSATION, this.conversationId);
      conversationGene = JSON.toJSONString(conversationGeneMap);
    }
    return conversationGene;
  }
  private LCIMConversation parseConversation(LCIMClient client, Messages.ConvCommand convCommand) {
    if (null == client || null == convCommand) {
      return null;
    }
    boolean isTemp = convCommand.hasTempConv() && convCommand.getTempConv();
    boolean isTransient = convCommand.hasTransient() && convCommand.getTransient();
    int tempTTL = convCommand.hasTempConvTTL()?convCommand.getTempConvTTL() : 0;

    LCIMConversation conversation = client.getConversation(this.conversationId, isTransient, isTemp);
    conversation.setTemporaryExpiredat(System.currentTimeMillis()/1000 + tempTTL);
    return conversation;
  }

  private void refreshConversationThenNotify(final LCIMMessage message, final SimpleCallback callback) {
    if (null == message || null == callback) {
      return;
    }
    final LCIMClient client = LCIMClient.getInstance(session.getSelfPeerId());
    final LCIMConversation conversation = client.getConversation(message.getConversationId(), convType);
    refreshConversationThenNotify(conversation, callback);
  }

  private void refreshConversationThenNotify(final LCIMConversation conversation, final SimpleCallback callback) {
    if (null == conversation) {
      return;
    }
    if (!conversation.isShouldFetch()) {
      callback.done();
    } else {
      LOGGER.d("try to query conversation info for id=" + conversation.getConversationId());

      Map<String, Object> fetchParams = conversation.getFetchRequestParams();
      InternalConfiguration.getOperationTube().queryConversationsInternally(session.getConnectionManager(),
              session.getSelfPeerId(), JSON.toJSONString(fetchParams),
              new LCIMCommonJsonCallback() {
                @Override
                public void done(Map<String, Object> result, LCIMException e) {
                  if (null != result) {
                    conversation.processQueryResult((String)result.get(Conversation.callbackData));
                    if (null != callback) {
                      callback.done();
                    }
                  }
                }
              });
    }
  }

  private static abstract class SimpleCallback {
    public abstract void done();
  }
}
