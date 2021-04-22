package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.messages.LCIMRecalledMessage;

public abstract class LCIMMessageRecalledCallback extends LCCallback<LCIMRecalledMessage> {

  public abstract void done(LCIMRecalledMessage recalledMessage, LCException e);

  @Override
  protected void internalDone0(LCIMRecalledMessage LCIMRecalledMessage, LCException LCException) {
    done(LCIMRecalledMessage, LCException);
  }
}
