package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.Conversation.AVIMOperation;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AVConversationHolder {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVConversationHolder.class);

  // 服务器端为了兼容老版本，这里需要使用group的invite
  private static final String GROUP_INVITE = "invite";
  private static final String GROUP_KICK = "kick";
  private static final String BLOCK_MEMBER = "conversation-block-clients";
  private static final String UNBLOCK_MEMBER = "conversation-unblock-clients";

  AVSession session;
  String conversationId;
  int convType;

  public AVConversationHolder(String conversationId, AVSession session, int convType) {
    this.session = session;
    this.conversationId = conversationId;
    this.conversationGene = getConversationGeneString();
    this.convType = convType;
  }

  public void addMembers(final List<String> members, final int requestId) {
    ;
  }

  public void kickMembers(final List<String> members, final int requestId) {
    ;
  }

  public void muteMembers(final List<String> members, final int requestId){
    ;
  }

  public void unmuteMembers(final List<String> members, final int requestId){
    ;
  }

  public void blockMembers(final List<String> members, final int requestId){
    ;
  }

  public void unblockMembers(final List<String> members, final int requestId){
    ;
  }

  public void join(final int requestId) {
    ;
  }

  public void queryMutedMembers(int offset, int limit, int requestId) {
    ;
  }

  public void queryBlockedMembers(int offset, int limit, int requestId) {
    ;
  }

  public void updateInfo(Map<String, Object> attr, int requestId) {
    ;
  }

  public void promoteMember(Map<String, Object> member, int requestId) {
    ;
  }

  public void sendMessage(AVIMMessage message, int requestId, AVIMMessageOption messageOption) {
    ;
  }

  public void patchMessage(AVIMMessage oldMessage, AVIMMessage newMessage, AVIMMessage recallMessage,
                           Conversation.AVIMOperation operation, int requestId) {
    ;
  }
  public void quit(final int requestId) {
    ;
  }

  public void queryHistoryMessages(String msgId, long timestamp, int limit, String toMsgId,
                                   long toTimestamp, int requestId) {
    ;
  }

  public void queryHistoryMessages(String msgId, long timestamp, boolean sclosed,
                                   String toMsgId, long toTimestamp, boolean toclosed,
                                   int direct, int limit, int msgType, int requestId) {
    ;
  }

  public void mute(int requestId) {
    ;
  }

  public void unmute(int requestId) {
    ;
  }

  public void getMemberCount(int requestId) {
    ;
  }

  private void getReceiptTime(int requestId) {
    ;
  }

  private void read(String msgId, long timestamp, int requestId) {
    ;
  }

  private boolean checkSessionStatus(Conversation.AVIMOperation operation, int requestId) {
    return false;
  }
  public void processConversationCommandFromClient(Conversation.AVIMOperation imop, Map<String, Object> params,
                                                   int requestId) {
    ;
  }
  public void processConversationCommandFromServer(Conversation.AVIMOperation imop, String operation, int requestId, Messages.ConvCommand convCommand) {
    ;
  }
  void onResponse4MemberBlock(Conversation.AVIMOperation imop, String operation, int reqeustId, Messages.BlacklistCommand blacklistCommand) {
    ;
  }
  void onResponse4MemberMute(AVIMOperation imop, String operation, int requestId, Messages.ConvCommand convCommand) {
    ;
  }
  public void processMessages(Integer requestKey, List<Messages.LogItem> logItems) {
    ;
  }
  void onConversationCreated(int requestId, Messages.ConvCommand convCommand) {
    ;
  }
  void onJoined(int requestId) {
    ;
  }
  void onInvited(int requestId) {
    ;
  }
  void onKicked(int requestId) {
    ;
  }
  void onQuit(int requestId) {
    ;
  }
  private void onInfoUpdated(int requestId, String updatedAt) {
    ;
  }
  private void onMemberUpdated(int requestId) {
    ;
  }
  private void onMemberChanged(final String operator, Messages.ConvMemberInfo member) {
    ;
  }
  void onMuted(int requestId) {
    ;
  }
  void onUnmuted(int requestId) {
    ;
  }
  void onMemberCount(int count, int requestId) {
    ;
  }
  void onMessageSent(int requestId, String msgId, long timestamp) {
    ;
  }
  void onHistoryMessageQuery(ArrayList<AVIMMessage> messages, int requestId, long deliveredAt, long readAt) {
    ;
  }
  void onTimesReceipt(int requestId, long deliveredAt, long readAt) {
    ;
  }
  void onInvitedToConversation(final String invitedBy, Messages.ConvCommand convCommand) {
    ;
  }
  void onInfoChangedNotify(Messages.ConvCommand convCommand) {
    ;
  }
  void onKickedFromConversation(final String invitedBy) {
    ;
  }
  void onSelfShutupedNotify(final boolean isMuted, final String operator, Messages.ConvCommand convCommand) {
    ;
  }
  void onMemberShutupedNotify(final boolean isMuted, final String operator, Messages.ConvCommand convCommand) {
    ;
  }
  void onSelfBlockedNotify(final boolean isBlocked, final String operator, Messages.ConvCommand convCommand) {
    ;
  }
  void onMemberBlockedNotify(final boolean isBlocked, final String operator, Messages.ConvCommand convCommand) {
    ;
  }
  void onMembersJoined(final List<String> members, final String invitedBy) {
    ;
  }
  void onMembersLeft(final List<String> members, final String removedBy) {
    ;
  }
  void onUnreadMessagesEvent(AVIMMessage message, int unreadCount, boolean mentioned) {
    ;
  }
  void onMessageReceipt(final AVIMMessage message) {
    ;
  }
  void onMessage(final AVIMMessage message, final boolean hasMore, final boolean isTransient) {
    ;
  }
  void onMessageUpdateEvent(final AVIMMessage message, final boolean isRecall) {
    ;
  }
  void onConversationReadAtEvent(final long readAt) {
    ;
  }
  void onConversationDeliveredAtEvent(final long deliveredAt) {
    ;
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
  private AVIMConversation parseConversation(AVIMClient client, Messages.ConvCommand convCommand) {
    return null;
  }
}
