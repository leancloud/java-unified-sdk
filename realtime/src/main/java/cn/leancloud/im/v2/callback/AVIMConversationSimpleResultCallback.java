package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMException;

import java.util.List;

public abstract class AVIMConversationSimpleResultCallback extends AVCallback<List<String>> {
  /**
   * 结果处理函数
   * @param memberIdList  成员的 client id 列表
   * @param e             异常
   */
  public abstract void done(List<String> memberIdList, AVIMException e);

  @Override
  protected final void internalDone0(List<String> returnValue, AVException e) {
    done(returnValue, AVIMException.wrapperAVException(e));
  }
}