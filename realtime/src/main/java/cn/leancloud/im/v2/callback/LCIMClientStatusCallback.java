package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMClient;

public abstract class LCIMClientStatusCallback extends LCCallback<LCIMClient.LCIMClientStatus> {
  public abstract void done(LCIMClient.LCIMClientStatus client);

  @Override
  protected void internalDone0(LCIMClient.LCIMClientStatus status, LCException LCException) {
    done(status);
  }
}
