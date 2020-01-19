package cn.leancloud.im.v2;

import java.util.List;

public class ConversationSynchronizer {
  public static void mergeMembers(AVIMConversation conversation, List<String> members) {
    if (null == conversation) {
      return;
    }
    conversation.internalMergeMembers(members);
  }

  public static void removeMembers(AVIMConversation conversation, List<String> members) {
    if (null == conversation) {
      return;
    }
    conversation.internalRemoveMembers(members);
  }
}
