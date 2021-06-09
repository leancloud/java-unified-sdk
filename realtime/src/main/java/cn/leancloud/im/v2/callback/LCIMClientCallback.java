package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMException;

public abstract class LCIMClientCallback extends LCCallback<LCIMClient> {
  public abstract void done(LCIMClient client, LCIMException e);

  @Override
  protected void internalDone0(LCIMClient client, LCException LCException) {
    done(client, LCIMException.wrapperException(LCException));
  }
}
