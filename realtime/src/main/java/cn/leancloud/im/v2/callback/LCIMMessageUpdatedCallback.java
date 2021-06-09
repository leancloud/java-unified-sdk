package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMMessage;

public abstract class LCIMMessageUpdatedCallback extends LCCallback<LCIMMessage> {

  public abstract void done(LCIMMessage message, LCException e);

  @Override
  protected void internalDone0(LCIMMessage message, LCException LCException) {
    done(message, LCException);
  }
}