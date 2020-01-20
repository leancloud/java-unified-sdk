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
    HashMap<String, Object> attributes = new HashMap<String, Object>();
    if (jsonObj.containsKey(Conversation.NAME)) {
      attributes.put(Conversation.NAME, jsonObj.getString(Conversation.NAME));
    }
    if (jsonObj.containsKey(Conversation.ATTRIBUTE)) {
      JSONObject moreAttributes = jsonObj.getJSONObject(Conversation.ATTRIBUTE);
      if (moreAttributes != null) {
        Map<String, Object> moreAttributesMap = JSON.toJavaObject(moreAttributes, Map.class);
        attributes.putAll(moreAttributesMap);
      }
    }
    conversation.attributes.putAll(attributes);
    for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
      String key = entry.getKey();
      if (!Arrays.asList(Conversation.CONVERSATION_COLUMNS).contains(key)) {
        conversation.instanceData.put(key, entry.getValue());
      }
    }
    conversation.latestConversationFetch = System.currentTimeMillis();
  }
}
