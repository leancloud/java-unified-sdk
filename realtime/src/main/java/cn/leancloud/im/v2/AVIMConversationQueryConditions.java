package cn.leancloud.im.v2;

import cn.leancloud.query.QueryConditions;

import java.util.Map;

final class AVIMConversationQueryConditions extends QueryConditions {
  private boolean isWithLastMessageRefreshed = false;
  private boolean isCompact = false;
  /**
   * 是否携带最后一条消息
   *
   * @return
   */
  public boolean isWithLastMessagesRefreshed() {
    return isWithLastMessageRefreshed;
  }

  /**
   * 设置是否携带最后一条消息
   *
   * @param isWithLastMessageRefreshed
   */
  public void setWithLastMessagesRefreshed(boolean isWithLastMessageRefreshed) {
    this.isWithLastMessageRefreshed = isWithLastMessageRefreshed;
  }

  public void setCompact(boolean isCompact) {
    this.isCompact = isCompact;
  }

  @Override
  public Map<String, String> assembleParameters() {
    Map<String, String> parameters = super.assembleParameters();
    if (isWithLastMessageRefreshed) {
      parameters.put(Conversation.QUERY_PARAM_LAST_MESSAGE, Boolean.toString(isWithLastMessageRefreshed));
    } else if (parameters.containsKey(Conversation.QUERY_PARAM_LAST_MESSAGE)) {
      parameters.remove(Conversation.QUERY_PARAM_LAST_MESSAGE);
    }

    if (isCompact) {
      parameters.put(Conversation.QUERY_PARAM_COMPACT, Boolean.toString(isCompact));
    } else if (parameters.containsKey(Conversation.QUERY_PARAM_COMPACT)) {
      parameters.remove(Conversation.QUERY_PARAM_COMPACT);
    }
    return parameters;
  }

}
