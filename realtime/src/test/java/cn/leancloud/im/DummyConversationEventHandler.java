package cn.leancloud.im;

import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationEventHandler;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.utils.StringUtil;

import java.util.List;

public class DummyConversationEventHandler extends AVIMConversationEventHandler {
  static final int NOTIFY_INDEX_INVITED = 1;
  static final int NOTIFY_INDEX_KICKED = 2;
  static final int NOTIFY_INDEX_MUTED = 3;
  static final int NOTIFY_INDEX_UNMUTED = 4;
  static final int NOTIFY_INDEX_BLOCKED = 5;
  static final int NOTIFY_INDEX_UNBLOCKED = 6;
  static final int NOTIFY_INDEX_MEMBERLEFT = 7;
  static final int NOTIFY_INDEX_MEMBERJOINED = 8;
  static final int NOTIFY_INDEX_MEMBERMUTED = 9;
  static final int NOTIFY_INDEX_MEMBERUNMUTED = 10;
  static final int NOTIFY_INDEX_MEMBERBLOCKED = 11;
  static final int NOTIFY_INDEX_MEMBERUNBLOCKED = 12;
  static final int NOTIFY_INDEX_MSGUPDATED = 13;
  static final int NOTIFY_INDEX_MSGRECALLED = 14;

  static final int NOTIFY_COUNT = 15;

  static final int FLAG_NOTIFY_INVITED = 0x0001 << (NOTIFY_INDEX_INVITED - 1);
  static final int FLAG_NOTIFY_KICKED = 0x0001 << (NOTIFY_INDEX_KICKED - 1);
  static final int FLAG_NOTIFY_MUTED = 0x0001 << (NOTIFY_INDEX_MUTED - 1);
  static final int FLAG_NOTIFY_UNMUTED = 0x0001 << (NOTIFY_INDEX_UNMUTED - 1);
  static final int FLAG_NOTIFY_BLOCKED = 0x0001 << (NOTIFY_INDEX_BLOCKED - 1);
  static final int FLAG_NOTIFY_UNBLOCKED = 0x0001 << (NOTIFY_INDEX_UNBLOCKED - 1);
  static final int FLAG_NOTIFY_MEMBERLEFT = 0x0001 << (NOTIFY_INDEX_MEMBERLEFT - 1);
  static final int FLAG_NOTIFY_MEMBERJOINED = 0x0001 << (NOTIFY_INDEX_MEMBERJOINED - 1);
  static final int FLAG_NOTIFY_MEMBERMUTED = 0x0001 << (NOTIFY_INDEX_MEMBERMUTED - 1);
  static final int FLAG_NOTIFY_MEMBERUNMUTED = 0x0001 << (NOTIFY_INDEX_MEMBERUNMUTED - 1);
  static final int FLAG_NOTIFY_MEMBERBLOCKED = 0x0001 << (NOTIFY_INDEX_MEMBERBLOCKED - 1);
  static final int FLAG_NOTIFY_MEMBERUNBLOCKED = 0x0001 << (NOTIFY_INDEX_MEMBERUNBLOCKED - 1);
  static final int FLAG_NOTIFY_MSGUPDATED = 0x0001 << (NOTIFY_INDEX_MSGUPDATED - 1);
  static final int FLAG_NOTIFY_MSGRECALLED = 0x0001 << (NOTIFY_INDEX_MSGRECALLED - 1);

  private int notifyConfig = 0;
  private int[] count = new int[NOTIFY_COUNT];

  public DummyConversationEventHandler() {
    this(0);
  }
  public DummyConversationEventHandler(int notifyConfig) {
    this.notifyConfig = notifyConfig;
    resetAllCount();
  }

  public void resetAllCount() {
    for (int i = 0; i< NOTIFY_COUNT; i++) {
      count[i] = 0;
    }
  }

  public int getCount(int config) {
    int result = 0;
    for (int i = 0; i < NOTIFY_COUNT; i++) {
      int flag = (0x0001 << i);
      if ((config & flag) == flag) {
        result += count[i];
      }
    }

    return result;
  }

  public void onMemberLeft(AVIMClient client,
                           AVIMConversation conversation, List<String> members, String kickedBy) {
    LOGGER.d("Notification --- onMemberLeft. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + kickedBy );
    if ((notifyConfig & FLAG_NOTIFY_MEMBERLEFT) == FLAG_NOTIFY_MEMBERLEFT) {
      count[NOTIFY_INDEX_MEMBERLEFT] ++;
    }
  }

  public void onMemberJoined(AVIMClient client,
                                      AVIMConversation conversation, List<String> members, String invitedBy) {
    LOGGER.d("Notification --- onMemberJoined. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + invitedBy );
    if ((notifyConfig & FLAG_NOTIFY_MEMBERJOINED) == FLAG_NOTIFY_MEMBERJOINED) {
      count[NOTIFY_INDEX_MEMBERJOINED] ++;
    }
  }

  public void onKicked(AVIMClient client, AVIMConversation conversation,
                                String kickedBy) {
    LOGGER.d("Notification --- onKicked. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + kickedBy );
    if ((notifyConfig & FLAG_NOTIFY_KICKED) == FLAG_NOTIFY_KICKED) {
      count[NOTIFY_INDEX_KICKED] ++;
    }
  }

  public void onInvited(AVIMClient client, AVIMConversation conversation,
                                 String operator) {
    LOGGER.d("Notification --- onInvited. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + operator );
    if ((notifyConfig & FLAG_NOTIFY_INVITED) == FLAG_NOTIFY_INVITED) {
      count[NOTIFY_INDEX_INVITED] ++;
    }
  }

  public void onMuted(AVIMClient client, AVIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are muted by " + operator );
    if ((notifyConfig & FLAG_NOTIFY_MUTED) == FLAG_NOTIFY_MUTED) {
      count[NOTIFY_INDEX_MUTED] ++;
    }
  }

  public void onUnmuted(AVIMClient client, AVIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are unmuted by " + operator );
    if ((notifyConfig & FLAG_NOTIFY_UNMUTED) == FLAG_NOTIFY_UNMUTED) {
      count[NOTIFY_INDEX_UNMUTED] ++;
    }
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
    if ((notifyConfig & FLAG_NOTIFY_MEMBERMUTED) == FLAG_NOTIFY_MEMBERMUTED) {
      count[NOTIFY_INDEX_MEMBERMUTED] ++;
    }
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
    if ((notifyConfig & FLAG_NOTIFY_MEMBERUNMUTED) == FLAG_NOTIFY_MEMBERUNMUTED) {
      count[NOTIFY_INDEX_MEMBERUNMUTED] ++;
    }
  }

  /**
   * 当前用户被加入黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param operator      操作者 id
   */
  public void onBlocked(AVIMClient client, AVIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are blocked by " + operator );
    if ((notifyConfig & FLAG_NOTIFY_BLOCKED) == FLAG_NOTIFY_BLOCKED) {
      count[NOTIFY_INDEX_BLOCKED] ++;
    }
  }

  /**
   * 当前用户被移出黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param operator      操作者 id
   */
  public void onUnblocked(AVIMClient client, AVIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are unblocked by " + operator );
    if ((notifyConfig & FLAG_NOTIFY_UNBLOCKED) == FLAG_NOTIFY_UNBLOCKED) {
      count[NOTIFY_INDEX_UNBLOCKED] ++;
    }
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
    if ((notifyConfig & FLAG_NOTIFY_MEMBERBLOCKED) == FLAG_NOTIFY_MEMBERBLOCKED) {
      count[NOTIFY_INDEX_MEMBERBLOCKED] ++;
    }
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
    if ((notifyConfig & FLAG_NOTIFY_MEMBERUNBLOCKED) == FLAG_NOTIFY_MEMBERUNBLOCKED) {
      count[NOTIFY_INDEX_MEMBERUNBLOCKED] ++;
    }
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
    if ((notifyConfig & FLAG_NOTIFY_MSGUPDATED) == FLAG_NOTIFY_MSGUPDATED) {
      count[NOTIFY_INDEX_MSGUPDATED] ++;
    }
  }

  /**
   * 实现本地方法来处理消息的撤回事件
   * @param client
   * @param conversation
   * @param message
   */
  public void onMessageRecalled(AVIMClient client, AVIMConversation conversation, AVIMMessage message) {
    LOGGER.d("Notification --- onMessageRecalled. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
    if ((notifyConfig & FLAG_NOTIFY_MSGRECALLED) == FLAG_NOTIFY_MSGRECALLED) {
      count[NOTIFY_INDEX_MSGRECALLED] ++;
    }
  }

}
