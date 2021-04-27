package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.conversation.LCIMConversationMemberInfo;

import java.util.List;

/**
 * 对话成员信息查询结果回调类
 */
public abstract class LCIMConversationMemberQueryCallback extends LCCallback<List<LCIMConversationMemberInfo>> {
  /**
   * 结果处理函数
   * @param memberInfoList   结果列表
   * @param e                异常实例，正常情况下为 null。
   */
  public abstract void done(List<LCIMConversationMemberInfo> memberInfoList, LCIMException e);

  @Override
  protected final void internalDone0(List<LCIMConversationMemberInfo> returnValue, LCException e) {
    done(returnValue, LCIMException.wrapperException(e));
  }
}