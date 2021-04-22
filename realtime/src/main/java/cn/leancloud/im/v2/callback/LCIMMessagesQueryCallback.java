package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.LCIMMessage;

import java.util.List;

public abstract class LCIMMessagesQueryCallback extends LCCallback<List<LCIMMessage>> {

  public abstract void done(List<LCIMMessage> messages, LCIMException e);

  @Override
  protected final void internalDone0(List<LCIMMessage> returnValue, LCException e) {
    done(returnValue, LCIMException.wrapperAVException(e));
  }
}
