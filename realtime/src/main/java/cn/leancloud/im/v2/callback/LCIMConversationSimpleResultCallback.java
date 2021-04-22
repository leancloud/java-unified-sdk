package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMException;

import java.util.List;

public abstract class LCIMConversationSimpleResultCallback extends LCCallback<List<String>> {
  /**
   * 结果处理函数
   * @param memberIdList  成员的 client id 列表
   * @param e             异常
   */
  public abstract void done(List<String> memberIdList, LCIMException e);

  @Override
  protected final void internalDone0(List<String> returnValue, LCException e) {
    done(returnValue, LCIMException.wrapperAVException(e));
  }
}