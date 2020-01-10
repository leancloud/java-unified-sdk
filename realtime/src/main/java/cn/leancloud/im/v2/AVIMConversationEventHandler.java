package cn.leancloud.im.v2;

import cn.leancloud.AVLogger;
import cn.leancloud.im.AVIMEventHandler;
import cn.leancloud.im.v2.conversation.AVIMConversationMemberInfo;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public abstract class AVIMConversationEventHandler extends AVIMEventHandler {
  protected static final AVLogger LOGGER = LogUtil.getLogger(AVIMConversationEventHandler.class);

  /**
   * 实现本方法以处理聊天对话中的参与者离开事件
   *
   * @param client client instance
   * @param conversation conversation instance.
   * @param members 离开的参与者
   * @param kickedBy 离开事件的发动者，有可能是离开的参与者本身
   * @since 3.0
   */

  public abstract void onMemberLeft(AVIMClient client,
                                    AVIMConversation conversation, List<String> members, String kickedBy);

  /**
   * 实现本方法以处理聊天对话中的参与者加入事件
   *
   * @param client client instance
   * @param conversation conversation instance.
   * @param members 加入的参与者
   * @param invitedBy 加入事件的邀请人，有可能是加入的参与者本身
   * @since 3.0
   */

  public abstract void onMemberJoined(AVIMClient client,
                                      AVIMConversation conversation, List<String> members, String invitedBy);

  /**
   * 实现本方法来处理当前用户被踢出某个聊天对话事件
   *
   * @param client client instance
   * @param conversation conversation instance.
   * @param kickedBy 踢出你的人
   * @since 3.0
   */

  public abstract void onKicked(AVIMClient client, AVIMConversation conversation,
                                String kickedBy);

  /**
   * 实现本方法来处理当前用户被邀请到某个聊天对话事件
   *
   * @param client client instance
   * @param conversation 被邀请的聊天对话
   * @param operator 邀请你的人
   * @since 3.0
   */
  public abstract void onInvited(AVIMClient client, AVIMConversation conversation,
                                 String operator);

  /**
   * 当前用户被禁言通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param operator      操作者 id
   */
  public void onMuted(AVIMClient client, AVIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are muted by " + operator );
  }

  /**
   * 当前用户被解除禁言通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param operator      操作者 id
   */
  public void onUnmuted(AVIMClient client, AVIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are unmuted by " + operator );
  }

  /**
   * 聊天室成员被禁言通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param members       成员列表
   * @param operator      操作者 id
   */
  public void onMemberMuted(AVIMClient client, AVIMConversation conversation, List<String> members, String operator){
    LOGGER.d("Notification --- " + operator + " muted members: " + StringUtil.join(", ", members));
  }

  /**
   * 聊天室成员被解除禁言通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param members       成员列表
   * @param operator      操作者 id
   */
  public void onMemberUnmuted(AVIMClient client, AVIMConversation conversation, List<String> members, String operator){
    LOGGER.d("Notification --- " + operator + " unmuted members: " + StringUtil.join(", ", members));
  }

  /**
   * 当前用户被加入黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param operator      操作者 id
   */
  public void onBlocked(AVIMClient client, AVIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are blocked by " + operator );
  }

  /**
   * 当前用户被移出黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param operator      操作者 id
   */
  public void onUnblocked(AVIMClient client, AVIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are unblocked by " + operator );
  }

  /**
   * 聊天室成员被加入黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param members       成员列表
   * @param operator      操作者 id
   */
  public void onMemberBlocked(AVIMClient client, AVIMConversation conversation, List<String> members, String operator){
    LOGGER.d("Notification --- " + operator + " blocked members: " + StringUtil.join(", ", members));
  }

  /**
   * 聊天室成员被移出黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param members       成员列表
   * @param operator      操作者 id
   */
  public void onMemberUnblocked(AVIMClient client, AVIMConversation conversation, List<String> members, String operator){
    LOGGER.d("Notification --- " + operator + " unblocked members: " + StringUtil.join(", ", members));
  }

  /**
   * 实现本地方法来处理未读消息数量的通知
   * @param client client instance
   * @param conversation current conversation.
   */
  public void onUnreadMessagesCountUpdated(AVIMClient client, AVIMConversation conversation) {}

  /**
   * 实现本地方法来处理对方已经接收消息的通知
   * @param client client instance
   * @param conversation current conversation.
   */
  public void onLastDeliveredAtUpdated(AVIMClient client, AVIMConversation conversation) {}

  /**
   * 实现本地方法来处理对方已经阅读消息的通知
   * @param client client instance
   * @param conversation current conversation.
   */
  public void onLastReadAtUpdated(AVIMClient client, AVIMConversation conversation) {}

  /**
   * 实现本地方法来处理消息的更新事件
   * @param client client instance
   * @param conversation current conversation.
   * @param message messgae instance.
   */
  public void onMessageUpdated(AVIMClient client, AVIMConversation conversation, AVIMMessage message) {}

  /**
   * 实现本地方法来处理消息的撤回事件
   * @param client client instance
   * @param conversation current conversation.
   * @param message message instance.
   */
  public void onMessageRecalled(AVIMClient client, AVIMConversation conversation, AVIMMessage message) {}

  /**
   * 对话成员信息变更通知。
   * 常见的有：某成员权限发生变化（如，被设为管理员等）。
   * @param client             通知关联的 AVIMClient
   * @param conversation       通知关联的对话
   * @param memberInfo         变更后的成员信息
   * @param updatedProperties  发生变更的属性列表（当前固定为 "role"）
   * @param operator           操作者 id
   */
  public void onMemberInfoUpdated(AVIMClient client, AVIMConversation conversation,
                                  AVIMConversationMemberInfo memberInfo, List<String> updatedProperties, String operator) {
    LOGGER.d("Notification --- " + operator + " updated memberInfo: " + memberInfo.toString());
  }

  /**
   * 对话自身属性变更通知
   *
   * @param client client instance
   * @param conversation current conversation.
   * @param attr optional attributes.
   * @param operator operator client id.
   */
  public void onInfoChanged(AVIMClient client, AVIMConversation conversation, JSONObject attr,
                            String operator) {
    LOGGER.d("Notification --- " + operator + " by member: " + operator + ", changedTo: " + attr.toJSONString());
  }

  @Override
  protected final void processEvent0(final int operation, final Object operator, final Object operand,
                                     Object eventScene) {
    final AVIMConversation conversation = (AVIMConversation) eventScene;
    processConversationEvent(operation, operator, operand, conversation);
  }

  private void processConversationEvent(int operation, Object operator, Object operand, AVIMConversation conversation) {
    switch (operation) {
      case Conversation.STATUS_ON_MEMBERS_LEFT:
        onMemberLeft(conversation.client, conversation, (List<String>) operand, (String) operator);
        break;
      case Conversation.STATUS_ON_MEMBERS_JOINED:
        onMemberJoined(conversation.client, conversation, (List<String>) operand, (String) operator);
        break;
      case Conversation.STATUS_ON_JOINED:
        onInvited(conversation.client, conversation, (String) operator);
        break;
      case Conversation.STATUS_ON_KICKED_FROM_CONVERSATION:
        onKicked(conversation.client, conversation, (String) operator);
        break;
      case Conversation.STATUS_ON_UNREAD_EVENT:
        AbstractMap.SimpleEntry<Integer, Boolean> unreadInfo = (AbstractMap.SimpleEntry<Integer, Boolean>)operand;
        conversation.updateUnreadCountAndMessage((AVIMMessage)operator, unreadInfo.getKey(), unreadInfo.getValue());
        onUnreadMessagesCountUpdated(conversation.client, conversation);
        break;
      case Conversation.STATUS_ON_MESSAGE_READ:
        conversation.setLastReadAt((long)operator, true);
        onLastReadAtUpdated(conversation.client, conversation);
        break;
      case Conversation.STATUS_ON_MESSAGE_DELIVERED:
        conversation.setLastDeliveredAt((long)operator, true);
        onLastDeliveredAtUpdated(conversation.client, conversation);
        break;
      case Conversation.STATUS_ON_MESSAGE_UPDATED:
        AVIMMessage message = (AVIMMessage)operator;
        conversation.updateLocalMessage(message);
        onMessageUpdated(conversation.client, conversation, message);
        break;
      case Conversation.STATUS_ON_MESSAGE_RECALLED:
        AVIMMessage recalledMessage = (AVIMMessage)operator;
        conversation.updateLocalMessage(recalledMessage);
        onMessageRecalled(conversation.client, conversation, recalledMessage);
        break;
      case Conversation.STATUS_ON_MEMBER_INFO_CHANGED:
        List<String> attr = new ArrayList<>();
        attr.add(AVIMConversationMemberInfo.ATTR_ROLE);
        onMemberInfoUpdated(conversation.client, conversation, (AVIMConversationMemberInfo) operand,
                attr, (String) operator);
        break;
      case Conversation.STATUS_ON_MUTED:
        onMuted(conversation.client, conversation, (String) operator);
        break;
      case Conversation.STATUS_ON_UNMUTED:
        onUnmuted(conversation.client, conversation, (String) operator);
        break;
      case Conversation.STATUS_ON_BLOCKED:
        onBlocked(conversation.client, conversation, (String) operator);
        break;
      case Conversation.STATUS_ON_UNBLOCKED:
        onUnblocked(conversation.client, conversation, (String) operator);
        break;
      case Conversation.STATUS_ON_MEMBER_MUTED:
        onMemberMuted(conversation.client, conversation, (List<String>) operand, (String) operator);
        break;
      case Conversation.STATUS_ON_MEMBER_UNMUTED:
        onMemberUnmuted(conversation.client, conversation, (List<String>) operand, (String) operator);
        break;
      case Conversation.STATUS_ON_MEMBER_BLOCKED:
        onMemberBlocked(conversation.client, conversation, (List<String>) operand, (String) operator);
        break;
      case Conversation.STATUS_ON_MEMBER_UNBLOCKED:
        onMemberUnblocked(conversation.client, conversation, (List<String>) operand, (String) operator);
        break;
      case Conversation.STATUS_ON_INFO_CHANGED:
        onInfoChanged(conversation.client, conversation, (JSONObject)operand, (String) operator);
      default:
        break;
    }
  }
}