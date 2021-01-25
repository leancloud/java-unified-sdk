package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMException;

public abstract class AVIMConversationIterableResultCallback extends AVCallback<AVIMConversationIterableResult> {
  /**
   * 结果处理函数
   * @param iterableResult  可迭代的结果
   * @param e             异常
   */
  public abstract void done(AVIMConversationIterableResult iterableResult, AVIMException e);

  @Override
  protected final void internalDone0(AVIMConversationIterableResult iterableResult, AVException e) {
    done(iterableResult, AVIMException.wrapperAVException(e));
  }
}
