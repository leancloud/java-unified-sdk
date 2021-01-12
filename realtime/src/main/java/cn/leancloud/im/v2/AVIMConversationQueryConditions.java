package cn.leancloud.im.v2;

import cn.leancloud.query.QueryConditions;
import cn.leancloud.utils.StringUtil;

import java.util.List;
import java.util.Map;

final class AVIMConversationQueryConditions extends QueryConditions {
  private static final int Flag_Compact = 0x01;
  private static final int Flag_WithLastMessage = 0x02;
  private boolean isWithLastMessageRefreshed = false;
  private boolean isCompact = false;

  private List<String> tempConvIds = null;

  /**
   * 是否携带最后一条消息
   *
   * @return
   */
  public boolean isWithLastMessagesRefreshed() {
    return isWithLastMessageRefreshed;
  }

  public static boolean isWithLastMessagesRefreshed(int flag) {
    return (flag & Flag_WithLastMessage) == Flag_WithLastMessage;
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

  public void setTempConversationIds(List<String> ids) {
    this.tempConvIds = ids;
  }

  @Override
  public Map<String, String> assembleParameters() {
    Map<String, String> parameters = super.assembleParameters();
    if (null != this.tempConvIds && !this.tempConvIds.isEmpty()) {
      parameters.put(Conversation.QUERY_PARAM_TEMPCONV, StringUtil.join(",", this.tempConvIds));
    }
    int flag = 0;
    if (isWithLastMessageRefreshed) {
      flag += Flag_WithLastMessage;
    }
    if (isCompact) {
      flag += Flag_Compact;
    }
    return assembleParameters(parameters, flag);
  }

  public static Map<String, String> assembleParameters(Map<String, String> param, int flag) {
    if (null == param) {
      return null;
    }
    if ((flag & Flag_Compact) == Flag_Compact) {
      // 不返回成员列表
      param.put(Conversation.QUERY_PARAM_COMPACT, Boolean.toString(true));
    } else {
      param.remove(Conversation.QUERY_PARAM_COMPACT);
    }
    if ((flag & Flag_WithLastMessage) == Flag_WithLastMessage) {
      // 返回对话最近一条消息
      param.put(Conversation.QUERY_PARAM_LAST_MESSAGE, Boolean.toString(true));
    } else {
      param.remove(Conversation.QUERY_PARAM_LAST_MESSAGE);
    }
    return param;
  }

}
