package cn.leancloud.im;

import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationEventHandler;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.utils.StringUtil;

import java.util.List;

public class DummyConversationEventHandler extends AVIMConversationEventHandler {
  public void onMemberLeft(AVIMClient client,
                           AVIMConversation conversation, List<String> members, String kickedBy) {
    LOGGER.d("Notification --- onMemberLeft. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + kickedBy );
  }

  public void onMemberJoined(AVIMClient client,
                                      AVIMConversation conversation, List<String> members, String invitedBy) {
    LOGGER.d("Notification --- onMemberJoined. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + invitedBy );
  }

  public void onKicked(AVIMClient client, AVIMConversation conversation,
                                String kickedBy) {
    LOGGER.d("Notification --- onKicked. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + kickedBy );
  }

  public void onInvited(AVIMClient client, AVIMConversation conversation,
                                 String operator) {
    LOGGER.d("Notification --- onInvited. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + operator );
  }

  public void onMuted(AVIMClient client, AVIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are muted by " + operator );
  }

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
   * @param client
   * @param conversation
   */
  public void onUnreadMessagesCountUpdated(AVIMClient client, AVIMConversation conversation) {
    LOGGER.d("Notification --- onUnreadMessagesCountUpdated. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
  }

  /**
   * 实现本地方法来处理对方已经接收消息的通知
   */
  public void onLastDeliveredAtUpdated(AVIMClient client, AVIMConversation conversation) {
    LOGGER.d("Notification --- onLastDeliveredAtUpdated. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
  }

  /**
   * 实现本地方法来处理对方已经阅读消息的通知
   */
  public void onLastReadAtUpdated(AVIMClient client, AVIMConversation conversation) {
    LOGGER.d("Notification --- onLastReadAtUpdated. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
  }

  /**
   * 实现本地方法来处理消息的更新事件
   * @param client
   * @param conversation
   * @param message
   */
  public void onMessageUpdated(AVIMClient client, AVIMConversation conversation, AVIMMessage message) {
    LOGGER.d("Notification --- onMessageUpdated. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
  }

  /**
   * 实现本地方法来处理消息的撤回事件
   * @param client
   * @param conversation
   * @param message
   */
  public void onMessageRecalled(AVIMClient client, AVIMConversation conversation, AVIMMessage message) {
    LOGGER.d("Notification --- onMessageRecalled. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
  }

}
