package cn.leancloud.im;

import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMConversationEventHandler;
import cn.leancloud.im.v2.LCIMMessage;
import cn.leancloud.utils.StringUtil;

import java.util.List;

public class DummyConversationEventHandler extends LCIMConversationEventHandler {
  static final int NOTIFY_INDEX_INVITED = 0;
  static final int NOTIFY_INDEX_KICKED = 1;
  static final int NOTIFY_INDEX_MUTED = 2;
  static final int NOTIFY_INDEX_UNMUTED = 3;
  static final int NOTIFY_INDEX_BLOCKED = 4;
  static final int NOTIFY_INDEX_UNBLOCKED = 5;
  static final int NOTIFY_INDEX_MEMBERLEFT = 6;
  static final int NOTIFY_INDEX_MEMBERJOINED = 7;
  static final int NOTIFY_INDEX_MEMBERMUTED = 8;
  static final int NOTIFY_INDEX_MEMBERUNMUTED = 9;
  static final int NOTIFY_INDEX_MEMBERBLOCKED = 10;
  static final int NOTIFY_INDEX_MEMBERUNBLOCKED = 11;
  static final int NOTIFY_INDEX_MSGUPDATED = 12;
  static final int NOTIFY_INDEX_MSGRECALLED = 13;
  static final int NOTIFY_INDEX_END = 14;

  static final int NOTIFY_COUNT = 14;

  static final int FLAG_NOTIFY_INVITED = 0x0001 << (NOTIFY_INDEX_INVITED);
  static final int FLAG_NOTIFY_KICKED = 0x0001 << (NOTIFY_INDEX_KICKED);
  static final int FLAG_NOTIFY_MUTED = 0x0001 << (NOTIFY_INDEX_MUTED);
  static final int FLAG_NOTIFY_UNMUTED = 0x0001 << (NOTIFY_INDEX_UNMUTED);
  static final int FLAG_NOTIFY_BLOCKED = 0x0001 << (NOTIFY_INDEX_BLOCKED);
  static final int FLAG_NOTIFY_UNBLOCKED = 0x0001 << (NOTIFY_INDEX_UNBLOCKED);
  static final int FLAG_NOTIFY_MEMBERLEFT = 0x0001 << (NOTIFY_INDEX_MEMBERLEFT);
  static final int FLAG_NOTIFY_MEMBERJOINED = 0x0001 << (NOTIFY_INDEX_MEMBERJOINED);
  static final int FLAG_NOTIFY_MEMBERMUTED = 0x0001 << (NOTIFY_INDEX_MEMBERMUTED);
  static final int FLAG_NOTIFY_MEMBERUNMUTED = 0x0001 << (NOTIFY_INDEX_MEMBERUNMUTED);
  static final int FLAG_NOTIFY_MEMBERBLOCKED = 0x0001 << (NOTIFY_INDEX_MEMBERBLOCKED);
  static final int FLAG_NOTIFY_MEMBERUNBLOCKED = 0x0001 << (NOTIFY_INDEX_MEMBERUNBLOCKED);
  static final int FLAG_NOTIFY_MSGUPDATED = 0x0001 << (NOTIFY_INDEX_MSGUPDATED);
  static final int FLAG_NOTIFY_MSGRECALLED = 0x0001 << (NOTIFY_INDEX_MSGRECALLED);

  private int notifyConfig = 0;
  private int[] count = new int[NOTIFY_COUNT];

  private DummyConversationEventHandler() {
    this(0x0000FFFF);
  }
  public DummyConversationEventHandler(int notifyConfig) {
    this.notifyConfig = notifyConfig;
    resetAllCount();
  }

  public void resetAllCount() {
    System.out.println("reset all counters.");
    for (int i = 0; i< NOTIFY_COUNT; i++) {
      count[i] = 0;
    }
  }

  public int getCount(int config) {
    int result = 0;
    for (int i = 0; i < NOTIFY_COUNT; i++) {
      int flag = (0x0001 << i);
      if (count[i] <= 0) {
        System.out.println("count is 0 for index: " + i);
        continue;
      }
      if ((config & flag) == flag) {
        result += count[i];
      } else {
        System.out.println("skip count for index: " + i);
      }
    }

    return result;
  }

  public void onMemberLeft(LCIMClient client,
                           LCIMConversation conversation, List<String> members, String kickedBy) {
    LOGGER.d("Notification --- onMemberLeft. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + kickedBy );
    if ((notifyConfig & FLAG_NOTIFY_MEMBERLEFT) == FLAG_NOTIFY_MEMBERLEFT) {
      count[NOTIFY_INDEX_MEMBERLEFT] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_MEMBERLEFT, count[NOTIFY_INDEX_MEMBERLEFT]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  public void onMemberJoined(LCIMClient client,
                             LCIMConversation conversation, List<String> members, String invitedBy) {
    LOGGER.d("Notification --- onMemberJoined. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + invitedBy );
    if ((notifyConfig & FLAG_NOTIFY_MEMBERJOINED) == FLAG_NOTIFY_MEMBERJOINED) {
      count[NOTIFY_INDEX_MEMBERJOINED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_MEMBERJOINED, count[NOTIFY_INDEX_MEMBERJOINED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  public void onKicked(LCIMClient client, LCIMConversation conversation,
                       String kickedBy) {
    LOGGER.d("Notification --- onKicked. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + kickedBy );
    if ((notifyConfig & FLAG_NOTIFY_KICKED) == FLAG_NOTIFY_KICKED) {
      count[NOTIFY_INDEX_KICKED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_KICKED, count[NOTIFY_INDEX_KICKED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  public void onInvited(LCIMClient client, LCIMConversation conversation,
                        String operator) {
    LOGGER.d("Notification --- onInvited. client=" + client.getClientId() + ", convId=" + conversation.getConversationId() + ", by=" + operator );
    if ((notifyConfig & FLAG_NOTIFY_INVITED) == FLAG_NOTIFY_INVITED) {
      count[NOTIFY_INDEX_INVITED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_INVITED, count[NOTIFY_INDEX_INVITED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  public void onMuted(LCIMClient client, LCIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are muted by " + operator );
    if ((notifyConfig & FLAG_NOTIFY_MUTED) == FLAG_NOTIFY_MUTED) {
      count[NOTIFY_INDEX_MUTED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_MUTED, count[NOTIFY_INDEX_MUTED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  public void onUnmuted(LCIMClient client, LCIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are unmuted by " + operator );
    if ((notifyConfig & FLAG_NOTIFY_UNMUTED) == FLAG_NOTIFY_UNMUTED) {
      count[NOTIFY_INDEX_UNMUTED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_UNMUTED, count[NOTIFY_INDEX_UNMUTED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  /**
   * 聊天室成员被禁言通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param members       成员列表
   * @param operator      操作者 id
   */
  public void onMemberMuted(LCIMClient client, LCIMConversation conversation, List<String> members, String operator){
    LOGGER.d("Notification --- " + operator + " muted members: " + StringUtil.join(", ", members));
    if ((notifyConfig & FLAG_NOTIFY_MEMBERMUTED) == FLAG_NOTIFY_MEMBERMUTED) {
      count[NOTIFY_INDEX_MEMBERMUTED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_MEMBERMUTED, count[NOTIFY_INDEX_MEMBERMUTED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  /**
   * 聊天室成员被解除禁言通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param members       成员列表
   * @param operator      操作者 id
   */
  public void onMemberUnmuted(LCIMClient client, LCIMConversation conversation, List<String> members, String operator){
    LOGGER.d("Notification --- " + operator + " unmuted members: " + StringUtil.join(", ", members));
    if ((notifyConfig & FLAG_NOTIFY_MEMBERUNMUTED) == FLAG_NOTIFY_MEMBERUNMUTED) {
      count[NOTIFY_INDEX_MEMBERUNMUTED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_MEMBERUNMUTED, count[NOTIFY_INDEX_MEMBERUNMUTED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  /**
   * 当前用户被加入黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param operator      操作者 id
   */
  public void onBlocked(LCIMClient client, LCIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are blocked by " + operator );
    if ((notifyConfig & FLAG_NOTIFY_BLOCKED) == FLAG_NOTIFY_BLOCKED) {
      count[NOTIFY_INDEX_BLOCKED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_BLOCKED, count[NOTIFY_INDEX_BLOCKED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  /**
   * 当前用户被移出黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param operator      操作者 id
   */
  public void onUnblocked(LCIMClient client, LCIMConversation conversation, String operator) {
    LOGGER.d("Notification --- " + " you are unblocked by " + operator );
    if ((notifyConfig & FLAG_NOTIFY_UNBLOCKED) == FLAG_NOTIFY_UNBLOCKED) {
      count[NOTIFY_INDEX_UNBLOCKED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_UNBLOCKED, count[NOTIFY_INDEX_UNBLOCKED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  /**
   * 聊天室成员被加入黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param members       成员列表
   * @param operator      操作者 id
   */
  public void onMemberBlocked(LCIMClient client, LCIMConversation conversation, List<String> members, String operator){
    LOGGER.d("Notification --- " + operator + " blocked members: " + StringUtil.join(", ", members));
    if ((notifyConfig & FLAG_NOTIFY_MEMBERBLOCKED) == FLAG_NOTIFY_MEMBERBLOCKED) {
      count[NOTIFY_INDEX_MEMBERBLOCKED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_MEMBERBLOCKED, count[NOTIFY_INDEX_MEMBERBLOCKED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  /**
   * 聊天室成员被移出黑名单通知处理函数
   * @param client        聊天客户端
   * @param conversation  对话
   * @param members       成员列表
   * @param operator      操作者 id
   */
  public void onMemberUnblocked(LCIMClient client, LCIMConversation conversation, List<String> members, String operator){
    LOGGER.d("Notification --- " + operator + " unblocked members: " + StringUtil.join(", ", members));
    if ((notifyConfig & FLAG_NOTIFY_MEMBERUNBLOCKED) == FLAG_NOTIFY_MEMBERUNBLOCKED) {
      count[NOTIFY_INDEX_MEMBERUNBLOCKED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_MEMBERUNBLOCKED, count[NOTIFY_INDEX_MEMBERUNBLOCKED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  /**
   * 实现本地方法来处理未读消息数量的通知
   * @param client
   * @param conversation
   */
  public void onUnreadMessagesCountUpdated(LCIMClient client, LCIMConversation conversation) {
    LOGGER.d("Notification --- onUnreadMessagesCountUpdated. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
  }

  /**
   * 实现本地方法来处理对方已经接收消息的通知
   */
  public void onLastDeliveredAtUpdated(LCIMClient client, LCIMConversation conversation) {
    LOGGER.d("Notification --- onLastDeliveredAtUpdated. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
  }

  /**
   * 实现本地方法来处理对方已经阅读消息的通知
   */
  public void onLastReadAtUpdated(LCIMClient client, LCIMConversation conversation) {
    LOGGER.d("Notification --- onLastReadAtUpdated. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
  }

  /**
   * 实现本地方法来处理消息的更新事件
   * @param client
   * @param conversation
   * @param message
   */
  public void onMessageUpdated(LCIMClient client, LCIMConversation conversation, LCIMMessage message) {
    LOGGER.d("Notification --- onMessageUpdated. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
    if ((notifyConfig & FLAG_NOTIFY_MSGUPDATED) == FLAG_NOTIFY_MSGUPDATED) {
      count[NOTIFY_INDEX_MSGUPDATED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_MSGUPDATED, count[NOTIFY_INDEX_MSGUPDATED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

  /**
   * 实现本地方法来处理消息的撤回事件
   * @param client
   * @param conversation
   * @param message
   */
  public void onMessageRecalled(LCIMClient client, LCIMConversation conversation, LCIMMessage message) {
    LOGGER.d("Notification --- onMessageRecalled. client=" + client.getClientId() + ", convId=" + conversation.getConversationId());
    if ((notifyConfig & FLAG_NOTIFY_MSGRECALLED) == FLAG_NOTIFY_MSGRECALLED) {
      count[NOTIFY_INDEX_MSGRECALLED] += 1;
      System.out.println(String.format("index: %d, counter: %d", NOTIFY_INDEX_MSGRECALLED, count[NOTIFY_INDEX_MSGRECALLED]));
    } else {
      System.out.println("!!! Skip Notification !!!");
    }
  }

}
