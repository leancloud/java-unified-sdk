package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMException;

/**
 * 查询在线用户数目的回调抽象类
 */
public abstract class AVIMConversationMemberCountCallback extends AVCallback<Integer> {
  public abstract void done(Integer memberCount, AVIMException e);

  @Override
  protected final void internalDone0(Integer returnValue, AVException e) {
    done(returnValue, AVIMException.wrapperAVException(e));
  }
}
