package cn.leancloud.livequery;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;

public abstract class LCLiveQuerySubscribeCallback extends LCCallback<Void> {

  public abstract void done(LCException e);

  @Override
  protected void internalDone0(Void aVoid, LCException LCException) {
    done(LCException);
  }
}
