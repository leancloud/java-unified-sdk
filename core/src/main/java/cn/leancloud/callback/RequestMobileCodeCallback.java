package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.types.LCNull;

public abstract class RequestMobileCodeCallback extends LCCallback<LCNull> {

  public abstract void done(LCException e);

  @Override
  protected final void internalDone0(LCNull t, LCException LCException) {
    this.done(LCException);
  }
}