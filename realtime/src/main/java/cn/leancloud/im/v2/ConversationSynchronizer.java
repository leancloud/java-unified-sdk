package cn.leancloud.im.v2;

import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSONObject;

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

  public static void mergeConversationFromJsonObject(AVIMConversation conversation, JSONObject postObj, JSONObject allAttrs) {
    if (null == conversation || (null == postObj && null == allAttrs)) {
      return;
    }
    if (null == postObj) {
      // all are deleted attributes.
      for (Map.Entry<String, Object> entry: allAttrs.entrySet()) {
        conversation.instanceData.remove(entry.getKey());
      }
    } else {
      for (Map.Entry<String, Object> entry : postObj.entrySet()) {
        String key = entry.getKey();
        conversation.instanceData.put(key, entry.getValue());
      }
      if (null != allAttrs) {
        for (Map.Entry<String, Object> entry : allAttrs.entrySet()) {
          String key = entry.getKey();
          if (!postObj.containsKey(key)) {
            conversation.instanceData.remove(key);
          }
        }
      }
    }

    conversation.latestConversationFetch = System.currentTimeMillis();
  }
}
