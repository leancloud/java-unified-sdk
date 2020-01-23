package cn.leancloud.im.v2;

import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  public static void changeUpdatedTime(AVIMConversation conversation, String updatedAt) {
    if (null != conversation && !StringUtil.isEmpty(updatedAt)) {
      conversation.setUpdatedAt(updatedAt);
    }
  }

  public static void mergeConversationFromJsonObject(AVIMConversation conversation, JSONObject jsonObj) {
    if (null == conversation || null == jsonObj) {
      return;
    }
    // Notice: cannot update deleted attr.
    for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
      String key = entry.getKey();
      conversation.instanceData.put(key, entry.getValue());
    }
    conversation.latestConversationFetch = System.currentTimeMillis();
  }
}
