package cn.leancloud.session;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.command.*;
import cn.leancloud.command.ConversationControlPacket.ConversationControlOp;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.WindTalker;
import cn.leancloud.im.v2.*;
import cn.leancloud.livequery.AVLiveQuery;
import cn.leancloud.livequery.LiveQueryOperationDelegate;
import cn.leancloud.push.AVInstallation;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.session.PendingMessageCache.Message;
import cn.leancloud.session.AVIMOperationQueue.Operation;
import cn.leancloud.im.v2.Conversation.AVIMOperation;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AVDefaultConnectionListener implements AVConnectionListener {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVDefaultConnectionListener.class);
  private static final String SESSION_MESSASGE_DEPOT = "com.avos.push.session.message.";
  private static final int CODE_SESSION_SIGNATURE_FAILURE = 4102;
  private static final int CODE_SESSION_TOKEN_FAILURE = 4112;
  AVSession session;
  private final StaleMessageDepot depot;

  public AVDefaultConnectionListener(AVSession session) {
    this.session = session;
    depot = new StaleMessageDepot(SESSION_MESSASGE_DEPOT + session.getSelfPeerId());
  }

  @Override
  public void onWebSocketOpen() {
    LOGGER.d("web socket opened, send session open.");
    if (AVSession.Status.Closed == session.getCurrentStatus()) {
      session.reopen();
    }
  }

  @Override
  public void onWebSocketClose() {
    if (AVSession.Status.Closed != session.getCurrentStatus()) {
      try {
        session.sessionListener.onSessionPaused(session);
        // 这里给所有的消息发送失败消息
        if (session.pendingMessages != null && !session.pendingMessages.isEmpty()) {
          while (!session.pendingMessages.isEmpty()) {
            Message m = session.pendingMessages.poll();
            if (!StringUtil.isEmpty(m.cid)) {
              AVConversationHolder conversation = session.getConversationHolder(m.cid, Conversation.CONV_TYPE_NORMAL);
              InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), conversation.conversationId,
                      Integer.parseInt(m.id), Conversation.AVIMOperation.CONVERSATION_SEND_MESSAGE,
                      new RuntimeException("Connection Lost"));
            }
          }
        }
        if (session.conversationOperationCache != null
                && !session.conversationOperationCache.isEmpty()) {
          for (Map.Entry<Integer, Operation> entry : session.conversationOperationCache.cache.entrySet()) {
            int requestId = entry.getKey();
            Operation op = session.conversationOperationCache.poll(requestId);
            InternalConfiguration.getOperationTube().onOperationCompleted(op.sessionId, op.conversationId, requestId,
                    Conversation.AVIMOperation.getAVIMOperation(op.operation), new IllegalStateException("Connection Lost"));
          }
        }
      } catch (Exception e) {
        session.sessionListener.onError(session, e);
      }
    }
  }

  @Override
  public void onError(Integer requestKey, Messages.ErrorCommand errorCommand) {
    if (null != requestKey && requestKey != CommandPacket.UNSUPPORTED_OPERATION) {
      Operation op = session.conversationOperationCache.poll(requestKey);
      if (null != op && op.operation == AVIMOperation.CLIENT_OPEN.getCode()) {
        session.setSessionStatue(AVSession.Status.Closed);
      }
      int code = errorCommand.getCode();
      int appCode = (errorCommand.hasAppCode() ? errorCommand.getAppCode() : 0);
      String reason = errorCommand.getReason();
      AVIMOperation operation = (null != op)? AVIMOperation.getAVIMOperation(op.operation): null;
      InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), null, requestKey,
              operation, new AVIMException(code, appCode, reason));
    }

    // 如果遇到signature failure的异常,清除缓存
    if (null == requestKey) {
      int code = errorCommand.getCode();
      // 如果遇到signature failure的异常,清除缓存
      if (CODE_SESSION_SIGNATURE_FAILURE == code) {
        AVSessionCacheHelper.getTagCacheInstance().removeSession(session.getSelfPeerId());
      } else if (CODE_SESSION_TOKEN_FAILURE == code) {
        // 如果遇到session token 失效或者过期的情况，先是清理缓存，然后再重新触发一次自动登录
        session.updateRealtimeSessionToken("", 0);
        this.onWebSocketOpen();
      }
    }
  }

  public void onMessageArriving(String peerId, Integer requestKey, Messages.GenericCommand command) {
    LOGGER.d("new message arriving. peerId=" + peerId + ", requestId=" + requestKey + ", command=" + command.getCmd().getNumber());
    if (null == command) {
      return;
    }
    if (command.getCmd().getNumber() == Messages.CommandType.loggedin_VALUE) {
      if (LiveQueryLoginPacket.SERVICE_LIVE_QUERY == command.getService()) {
        // FIXME: unused code
        processLoggedinCommand(requestKey);
      } else {
        LOGGER.w("ignore loggedin command bcz invalid service.");
      }
    } else {
      switch (command.getCmd().getNumber()) {
        case Messages.CommandType.data_VALUE:
          if (!command.hasService()) {
            processDataCommand(command.getDataMessage());
          } else {
            final int service = command.getService();
            if (LiveQueryLoginPacket.SERVICE_PUSH == service) {
              processDataCommand(command.getDataMessage());
            } else if (LiveQueryLoginPacket.SERVICE_LIVE_QUERY == service) {
              // FIXME: unused code
              processLiveQueryData(command.getDataMessage());
            }
          }
          break;
        case Messages.CommandType.direct_VALUE:
          processDirectCommand(peerId, command.getDirectMessage());
          break;
        case Messages.CommandType.session_VALUE:
          processSessionCommand(peerId, command.getOp().name(), requestKey,
                  command.getSessionMessage());
          break;
        case Messages.CommandType.ack_VALUE:
          processAckCommand(peerId, requestKey, command.getAckMessage());
          break;
        case Messages.CommandType.rcp_VALUE:
          processRcpCommand(peerId, command.getRcpMessage());
          break;
        case Messages.CommandType.conv_VALUE:
          processConvCommand(peerId, command.getOp().name(), requestKey,
                  command.getConvMessage());
          break;
        case Messages.CommandType.error_VALUE:
          processErrorCommand(peerId, requestKey, command.getErrorMessage());
          break;
        case Messages.CommandType.logs_VALUE:
          processLogsCommand(peerId, requestKey, command.getLogsMessage());
          break;
        case Messages.CommandType.unread_VALUE:
          processUnreadCommand(peerId, command.getUnreadMessage());
          break;
        case Messages.CommandType.blacklist_VALUE:
          processBlacklistCommand(peerId, command.getOp().name(), requestKey, command.getBlacklistMessage());
          break;
        case Messages.CommandType.patch_VALUE:
          if(command.getOp().equals(Messages.OpType.modify)) {
            // modify 为服务器端主动推送的 patch 消息
            processPatchCommand(peerId, true, requestKey, command.getPatchMessage());
          } else if (command.getOp().equals(Messages.OpType.modified)) {
            // modified 代表的是服务器端对于客户端请求的相应
            processPatchCommand(peerId, false, requestKey, command.getPatchMessage());
          }
          break;
        default:
          LOGGER.w("unknown command. Cmd:" + command.getCmd().getNumber());
          break;
      }
    }
  }

  // FIXME: unused code
  private void processLoggedinCommand(Integer requestKey) {
    LiveQueryOperationDelegate.getInstance().ackOperationReplied(requestKey);
    if (null != requestKey) {
      InternalConfiguration.getOperationTube().onLiveQueryCompleted(requestKey, null);
    }
  }

  private void processDataCommand(Messages.DataCommand dataCommand) {
    List<String> messageIds = dataCommand.getIdsList();
    List<Messages.JsonObjectMessage> messages = dataCommand.getMsgList();
    for (int i = 0; i < messages.size() && i < messageIds.size(); i++) {
      if (null != messages.get(i)) {
        if (depot.putStableMessage(messageIds.get(i))) {
          InternalConfiguration.getOperationTube().onPushMessage(messages.get(i).getData(),
                  messageIds.get(i));
        } else {
          LOGGER.d("ignore duplicated push message.");
        }
      }
    }
    WindTalker windTalker = WindTalker.getInstance();
    CommandPacket packet = windTalker.assemblePushAckPacket(AVInstallation.getCurrentInstallation().getInstallationId(), messageIds);
    AVConnectionManager.getInstance().sendPacket(packet);
  }

  // FIXME: unused code
  private void processLiveQueryData(Messages.DataCommand dataCommand) {
    List<String> messageIds = dataCommand.getIdsList();
    List<Messages.JsonObjectMessage> messages = dataCommand.getMsgList();

    ArrayList<String> dataList = new ArrayList<>();
    for (int i = 0; i < messages.size() && i < messageIds.size(); i++) {
      Messages.JsonObjectMessage message = messages.get(i);
      if (null != message) {
        dataList.add(message.getData());
      }
    };
    AVLiveQuery.processData(dataList);
  }

  private void processDirectCommand(String peerId, Messages.DirectCommand directCommand) {
    final String msg = directCommand.getMsg();
    final ByteString binaryMsg = directCommand.getBinaryMsg();
    final String fromPeerId = directCommand.getFromPeerId();
    final String conversationId = directCommand.getCid();
    final Long timestamp = directCommand.getTimestamp();
    final String messageId = directCommand.getId();
    int convType = directCommand.hasConvType()? directCommand.getConvType() : Conversation.CONV_TYPE_NORMAL;
    final boolean isTransient = directCommand.hasTransient() && directCommand.getTransient();
    final boolean hasMore = directCommand.hasHasMore() && directCommand.getHasMore();
    final long patchTimestamp = directCommand.getPatchTimestamp();
    final boolean mentionAll = directCommand.hasMentionAll()? directCommand.getMentionAll() : false;
    final List<String> mentionList = directCommand.getMentionPidsList();

    try {
      if (!isTransient) {
        if (!StringUtil.isEmpty(conversationId)) {
          AVConnectionManager.getInstance().sendPacket(ConversationAckPacket.getConversationAckPacket(
                  session.getSelfPeerId(), conversationId, messageId));
        } else {
          AVConnectionManager.getInstance().sendPacket(genSessionAckPacket(messageId));
        }
      }

      if (depot.putStableMessage(messageId) && !StringUtil.isEmpty(conversationId)) {
        AVConversationHolder conversation = session.getConversationHolder(conversationId, convType);
        AVIMMessage message = null;
        if (StringUtil.isEmpty(msg) && null != binaryMsg) {
          message = new AVIMBinaryMessage(conversationId, fromPeerId, timestamp, -1);
          ((AVIMBinaryMessage)message).setBytes(binaryMsg.toByteArray());
        } else {
          message = new AVIMMessage(conversationId, fromPeerId, timestamp, -1);
          message.setContent(msg);
        }
        message.setMessageId(messageId);
        message.setUpdateAt(patchTimestamp);
        message.setMentionAll(mentionAll);
        message.setMentionList(mentionList);
        conversation.onMessage(message, hasMore, isTransient);
      }
    } catch (Exception e) {
      session.sessionListener.onError(session, e);
    }
  }
  private void processSessionCommand(String peerId, String op, Integer requestKey,
                                     Messages.SessionCommand command) {
    LOGGER.d("processSessionCommand. op=" + op + ",requestKey=" + requestKey);

    int requestId = (null != requestKey ? requestKey : CommandPacket.UNSUPPORTED_OPERATION);

    if (op.equals(SessionControlPacket.SessionControlOp.OPENED)) {
      try {
        AVSession.Status prevStatus = session.getCurrentStatus();
        session.setSessionStatue(AVSession.Status.Opened);


        if (AVSession.Status.Closed == prevStatus) {
          if (requestId != CommandPacket.UNSUPPORTED_OPERATION) {
            session.conversationOperationCache.poll(requestId);
          }
          session.sessionListener.onSessionOpen(session, requestId);
        } else {
          LOGGER.d("session resumed");
          session.sessionListener.onSessionResumed(session);
        }
        if (command.hasSt() && command.hasStTtl()) {
          session.updateRealtimeSessionToken(command.getSt(), Integer.valueOf(command.getStTtl()));
        }
      } catch (Exception e) {
        session.sessionListener.onError(session, e);
      }
    } else if (op.equals(SessionControlPacket.SessionControlOp.RENEWED_RTMTOKEN)) {
      if (command.hasSt() && command.hasStTtl()) {
        session.updateRealtimeSessionToken(command.getSt(), Integer.valueOf(command.getStTtl()));
      }
      session.sessionListener.onSessionTokenRenewed(session, requestId);
    }else if (op.equals(SessionControlPacket.SessionControlOp.QUERY_RESULT)) {
      final List<String> sessionPeerIds = command.getOnlineSessionPeerIdsList();
      session.sessionListener.onOnlineQuery(session, sessionPeerIds,
              requestId);

    } else if (op.equals(SessionControlPacket.SessionControlOp.CLOSED)) {
      if (command.hasCode()) {
        session.sessionListener.onSessionClosedFromServer(session,
                command.getCode());
      } else {
        if (requestId != CommandPacket.UNSUPPORTED_OPERATION) {
          session.conversationOperationCache.poll(requestId);
        }
        session.sessionListener.onSessionClose(session, requestId);
      }
    } else {
      LOGGER.w("unknown operation: " + op);
    }
  }
  private void processAckCommand(String peerId, Integer requestKey, Messages.AckCommand ackCommand) {
    session.setServerAckReceived(System.currentTimeMillis() / 1000);
    long timestamp = ackCommand.getT();
    final Message m = session.pendingMessages.poll(String.valueOf(requestKey));
    if (ackCommand.hasCode()) {
      // 这里是发送消息异常时的ack
      this.onAckError(requestKey, ackCommand, m);
    } else {
      if (null != m && !StringUtil.isEmpty(m.cid)) {
        AVConversationHolder conversation = session.getConversationHolder(m.cid, Conversation.CONV_TYPE_NORMAL);
        session.conversationOperationCache.poll(requestKey);
        String msgId = ackCommand.getUid();
        conversation.onMessageSent(requestKey, msgId, timestamp);
        if (m.requestReceipt) {
          m.timestamp = timestamp;
          m.id = msgId;
          MessageReceiptCache.add(session.getSelfPeerId(), msgId, m);
        }
      }
    }
  }
  private void processRcpCommand(String peerId, Messages.RcpCommand rcpCommand) {
    try {
      if (rcpCommand.hasT()) {
        final Long timestamp = rcpCommand.getT();
        final String conversationId = rcpCommand.getCid();
        int convType = Conversation.CONV_TYPE_NORMAL; // RcpCommand doesn't include convType, so we use default value.
        // Notice: it becomes a problem only when server send RcpCommand to a new device for the logined user.

        if (!StringUtil.isEmpty(conversationId)) {
          processConversationDeliveredAt(conversationId, convType, timestamp);
          processMessageReceipt(rcpCommand.getId(), conversationId, convType, timestamp);
        }
      }
    } catch (Exception e) {
      session.sessionListener.onError(session, e);
    }
  }
  private void processConvCommand(String peerId, String operation, Integer requestKey,
                                  Messages.ConvCommand convCommand) {
    if (ConversationControlPacket.ConversationControlOp.QUERY_RESULT.equals(operation)) {
      Operation op = session.conversationOperationCache.poll(requestKey);
      LOGGER.d("poll operation with requestId=" + requestKey + ", result=" + op);
      if (null != op && op.operation == AVIMOperation.CONVERSATION_QUERY.getCode()) {
        String result = convCommand.getResults().getData();
        Map<String, Object> bundle = new HashMap<>();
        bundle.put(Conversation.callbackData, result);
        InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), null, requestKey,
                AVIMOperation.CONVERSATION_QUERY, bundle);
      } else {
        LOGGER.w("not found requestKey: " + requestKey + ", op=" + op);
      }
    } else if (ConversationControlPacket.ConversationControlOp.QUERY_SHUTUP_RESULT.equals(operation)) {
      Operation op = session.conversationOperationCache.poll(requestKey);
      if (null != op && op.operation == AVIMOperation.CONVERSATION_MUTED_MEMBER_QUERY.getCode()) {
        List<String> result = convCommand.getMList(); // result stored in m field.
        String[] resultMembers = new String[null == result? 0 : result.size()];
        if (null != result) {
          result.toArray(resultMembers);
        }
        Map<String, Object> bundle = new HashMap<>();
        bundle.put(Conversation.callbackData, resultMembers);
        InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), null, requestKey,
                AVIMOperation.CONVERSATION_MUTED_MEMBER_QUERY, bundle);
      } else {
        LOGGER.w("not found requestKey: " + requestKey);
      }
    } else {
      String conversationId = null;
      int requestId = (null != requestKey ? requestKey : CommandPacket.UNSUPPORTED_OPERATION);
      AVIMOperation originOperation = null;
      if ((operation.equals(ConversationControlPacket.ConversationControlOp.ADDED)
              || operation.equals(ConversationControlOp.REMOVED)
              || operation.equals(ConversationControlOp.UPDATED)
              || operation.equals(ConversationControlOp.MEMBER_COUNT_QUERY_RESULT)
              || operation.equals(ConversationControlOp.SHUTUP_ADDED)
              || operation.equals(ConversationControlOp.SHUTUP_REMOVED)
              || operation.equals(ConversationControlOp.MEMBER_UPDATED))
              && requestId != CommandPacket.UNSUPPORTED_OPERATION) {

        Operation op = session.conversationOperationCache.poll(requestId);
        if (null != op) {
          originOperation = AVIMOperation.getAVIMOperation(op.operation);
          conversationId = op.conversationId;
        } else {
          conversationId = convCommand.getCid();
        }
      } else {
        if (operation.equals(ConversationControlOp.STARTED)) {
          session.conversationOperationCache.poll(requestId);
        }
        conversationId = convCommand.getCid();
      }
      int convType = Conversation.CONV_TYPE_NORMAL;
      if (convCommand.hasTempConv() && convCommand.getTempConv()) {
        convType = Conversation.CONV_TYPE_TEMPORARY;
      } else if (convCommand.hasTransient() && convCommand.getTransient()) {
        convType = Conversation.CONV_TYPE_TRANSIENT;
      }
      if (!StringUtil.isEmpty(conversationId)) {
        AVConversationHolder conversation = session.getConversationHolder(conversationId, convType);
        conversation.processConversationCommandFromServer(originOperation, operation, requestId, convCommand);
      }
    }
  }
  private void processErrorCommand(String peerId, Integer requestKey,
                                   Messages.ErrorCommand errorCommand) {
    if (null != requestKey && requestKey != CommandPacket.UNSUPPORTED_OPERATION) {
      Operation op = session.conversationOperationCache.poll(requestKey);
      if (null != op && op.operation == AVIMOperation.CLIENT_OPEN.getCode()) {
        session.setSessionStatue(AVSession.Status.Closed);
      }
      int code = errorCommand.getCode();
      int appCode = (errorCommand.hasAppCode() ? errorCommand.getAppCode() : 0);
      String reason = errorCommand.getReason();
      AVIMOperation operation = (null != op)? AVIMOperation.getAVIMOperation(op.operation): null;
      InternalConfiguration.getOperationTube().onOperationCompleted(peerId, null, requestKey,
              operation, new AVIMException(code, appCode, reason));
    }

    // 如果遇到signature failure的异常,清除缓存
    if (null == requestKey) {
      int code = errorCommand.getCode();
      // 如果遇到signature failure的异常,清除缓存
      if (CODE_SESSION_SIGNATURE_FAILURE == code) {
        AVSessionCacheHelper.getTagCacheInstance().removeSession(session.getSelfPeerId());
      } else if (CODE_SESSION_TOKEN_FAILURE == code) {
        // 如果遇到session token 失效或者过期的情况，先是清理缓存，然后再重新触发一次自动登录
        session.updateRealtimeSessionToken("", 0);
        this.onWebSocketOpen();
      }
    }
  }

  private void processLogsCommand(String peerId, Integer requestKey,
                                  Messages.LogsCommand logsCommand) {
    if (null != requestKey && requestKey != CommandPacket.UNSUPPORTED_OPERATION) {
      Operation op = session.conversationOperationCache.poll(requestKey);
      int convType = Conversation.CONV_TYPE_NORMAL;
      if (logsCommand.getLogsCount() > 0) {
        Messages.LogItem item = logsCommand.getLogs(0);
        if (null != item && item.hasConvType()) {
          convType = item.getConvType();
        }
      }
      AVConversationHolder conversation = session.getConversationHolder(op.conversationId, convType);
      conversation.processMessages(requestKey, logsCommand.getLogsList());
    }
  }

  private void processUnreadCommand(String peerId, Messages.UnreadCommand unreadCommand) {
    session.updateLastNotifyTime(unreadCommand.getNotifTime());
    if (unreadCommand.getConvsCount() > 0) {

      List<Messages.UnreadTuple> unreadTupleList = unreadCommand.getConvsList();
      if (null != unreadTupleList) {
        for (Messages.UnreadTuple unreadTuple : unreadTupleList) {
          String msgId = unreadTuple.getMid();
          String msgContent = unreadTuple.getData();
          long ts = unreadTuple.getPatchTimestamp();
          long updateTS = unreadTuple.getPatchTimestamp();
          String conversationId = unreadTuple.getCid();
          boolean mentioned = unreadTuple.getMentioned();
          ByteString binaryMsg = unreadTuple.getBinaryMsg();
          String from = unreadTuple.getFrom();
          int convType = unreadTuple.hasConvType()? unreadTuple.getConvType() : Conversation.CONV_TYPE_NORMAL;

          AVIMMessage message = null;
          if (StringUtil.isEmpty(msgContent) && null != binaryMsg) {
            message = new AVIMBinaryMessage(conversationId, from, ts, -1);
            ((AVIMBinaryMessage)message).setBytes(binaryMsg.toByteArray());
          } else {
            message = new AVIMMessage(conversationId, from, ts, -1);
            message.setContent(msgContent);
          }
          message.setMessageId(msgId);
          message.setUpdateAt(updateTS);

          AVConversationHolder conversation = session.getConversationHolder(conversationId, convType);
          conversation.onUnreadMessagesEvent(message, unreadTuple.getUnread(), mentioned);
        }
      }
    }
  }
  private void processBlacklistCommand(String peerId, String operation, Integer requestKey,
                                       Messages.BlacklistCommand blacklistCommand) {
    if (BlacklistCommandPacket.BlacklistCommandOp.QUERY_RESULT.equals(operation)) {
      Operation op = session.conversationOperationCache.poll(requestKey);
      if (null == op || op.operation != Conversation.AVIMOperation.CONVERSATION_BLOCKED_MEMBER_QUERY.getCode()) {
        LOGGER.w("not found requestKey: " + requestKey);
      } else {
        List<String> result = blacklistCommand.getBlockedPidsList();
        String[] resultArray = new String[null == result ? 0: result.size()];
        if (null != result) {
          result.toArray(resultArray);
        }
        String cid = blacklistCommand.getSrcCid();
        Map<String, Object> bundle = new HashMap<>();
        bundle.put(Conversation.callbackData, resultArray);
        InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), cid, requestKey,
                AVIMOperation.CONVERSATION_BLOCKED_MEMBER_QUERY, bundle);
      }
    } else if (BlacklistCommandPacket.BlacklistCommandOp.BLOCKED.equals(operation)
            || BlacklistCommandPacket.BlacklistCommandOp.UNBLOCKED.equals(operation)){
      // response for block/unblock reqeust.
      String conversationId = blacklistCommand.getSrcCid();
      AVConversationHolder internalConversation = session.getConversationHolder(conversationId, Conversation.CONV_TYPE_NORMAL);
      Operation op = session.conversationOperationCache.poll(requestKey);
      if (null == op || null == internalConversation) {
        // warning.
      } else {
        Conversation.AVIMOperation originOperation = Conversation.AVIMOperation.getAVIMOperation(op.operation);
        internalConversation.onResponse4MemberBlock(originOperation, operation, requestKey, blacklistCommand);
      }
    }
  }
  private void processPatchCommand(String peerId, boolean isModify, Integer requestKey, Messages.PatchCommand patchCommand) {
    updateLocalPatchTime(isModify, patchCommand);
    if (isModify) {
      if (patchCommand.getPatchesCount() > 0) {
        for (Messages.PatchItem patchItem : patchCommand.getPatchesList()) {
          AVIMMessage message = AVIMTypedMessage.getMessage(patchItem.getCid(), patchItem.getMid(), patchItem.getData(),
                  patchItem.getFrom(), patchItem.getTimestamp(), 0, 0);
          message.setUpdateAt(patchItem.getPatchTimestamp());
          AVConversationHolder conversation = session.getConversationHolder(patchItem.getCid(), Conversation.CONV_TYPE_NORMAL);
          conversation.onMessageUpdateEvent(message, patchItem.getRecall());
        }
      }
    } else {
      Operation op = session.conversationOperationCache.poll(requestKey);
      AVIMOperation operation = AVIMOperation.getAVIMOperation(op.operation);
      Map<String, Object> bundle = new HashMap<>();
      bundle.put(Conversation.PARAM_MESSAGE_PATCH_TIME, patchCommand.getLastPatchTime());
      InternalConfiguration.getOperationTube().onOperationCompletedEx(session.getSelfPeerId(), null, requestKey,
              operation, bundle);
    }
  }

  private void onAckError(Integer requestKey, Messages.AckCommand command, Message m) {
    Operation op = session.conversationOperationCache.poll(requestKey);
    if (op.operation == Conversation.AVIMOperation.CLIENT_OPEN.getCode()) {
      session.setSessionStatue(AVSession.Status.Closed);
    }
    Conversation.AVIMOperation operation = Conversation.AVIMOperation.getAVIMOperation(op.operation);
    int code = command.getCode();
    int appCode = (command.hasAppCode() ? command.getAppCode() : 0);
    String reason = command.getReason();
    AVException error = new AVIMException(code, appCode, reason);
    InternalConfiguration.getOperationTube().onOperationCompleted(session.getSelfPeerId(), op.conversationId,
            requestKey, operation, error);
  }

  /**
   * 处理 v2 版本中 conversation 的 deliveredAt 事件
   * @param conversationId
   * @param timestamp
   */
  private void processConversationDeliveredAt(String conversationId, int convType, long timestamp) {
    AVConversationHolder conversation = session.getConversationHolder(conversationId, convType);
    conversation.onConversationDeliveredAtEvent(timestamp);
  }

  /**
   * 处理 v2 版本中 message 的 rcp 消息
   * @param msgId
   * @param conversationId
   * @param timestamp
   */
  private void processMessageReceipt(String msgId, String conversationId, int convType, long timestamp) {
    Object messageCache =
            MessageReceiptCache.get(session.getSelfPeerId(), msgId);
    if (messageCache == null) {
      return;
    }
    Message m = (Message) messageCache;
    AVIMMessage msg =
            new AVIMMessage(conversationId, session.getSelfPeerId(), m.timestamp, timestamp);
    msg.setMessageId(m.id);
    msg.setContent(m.msg);
    msg.setMessageStatus(AVIMMessage.AVIMMessageStatus.AVIMMessageStatusReceipt);
    AVConversationHolder conversation = session.getConversationHolder(conversationId, convType);
    conversation.onMessageReceipt(msg);
  }

  public void onReadCmdReceipt(Messages.RcpCommand rcpCommand) {
    if (rcpCommand.hasRead() && rcpCommand.hasCid()) {
      final Long timestamp = rcpCommand.getT();
      String conversationId = rcpCommand.getCid();
      AVConversationHolder conversation = session.getConversationHolder(conversationId, Conversation.CONV_TYPE_NORMAL);
      conversation.onConversationReadAtEvent(timestamp);
    }
  }

  private SessionAckPacket genSessionAckPacket(String messageId) {
    SessionAckPacket sap = new SessionAckPacket();
    sap.setPeerId(session.getSelfPeerId());
    if (!StringUtil.isEmpty(messageId)) {
      sap.setMessageId(messageId);
    }

    return sap;
  }

  private void updateLocalPatchTime(boolean isModify, Messages.PatchCommand patchCommand) {
    if (isModify) {
      long lastPatchTime = 0;
      for (Messages.PatchItem item : patchCommand.getPatchesList()) {
        if (item.getPatchTimestamp() > lastPatchTime) {
          lastPatchTime = item.getPatchTimestamp();
        }
      }
      session.updateLastPatchTime(lastPatchTime);
    } else {
      session.updateLastPatchTime(patchCommand.getLastPatchTime());
    }
  }
}
