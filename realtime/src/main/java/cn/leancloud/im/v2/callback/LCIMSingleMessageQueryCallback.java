package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.LCIMMessage;

/**
 * 针对某些明确知道只有一个消息返回的消息查询接口的回调
 *
 * 比如getLastMessage
 */
public abstract class LCIMSingleMessageQueryCallback extends LCCallback<LCIMMessage> {


  public abstract void done(LCIMMessage msg, LCIMException e);

  @Override
  protected final void internalDone0(LCIMMessage returnValue, LCException e) {
    done(returnValue, LCIMException.wrapperAVException(e));
  }
}
