package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.conversation.AVIMConversationMemberInfo;

import java.util.List;

/**
 * 对话成员信息查询结果回调类
 */
public abstract class AVIMConversationMemberQueryCallback extends AVCallback<List<AVIMConversationMemberInfo>> {
  /**
   * 结果处理函数
   * @param memberInfoList   结果列表
   * @param e                异常实例，正常情况下为 null。
   */
  public abstract void done(List<AVIMConversationMemberInfo> memberInfoList, AVIMException e);

  @Override
  protected final void internalDone0(List<AVIMConversationMemberInfo> returnValue, AVException e) {
    done(returnValue, AVIMException.wrapperAVException(e));
  }
}