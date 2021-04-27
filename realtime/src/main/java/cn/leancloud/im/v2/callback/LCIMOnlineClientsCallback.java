package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMException;

import java.util.List;

public abstract class LCIMOnlineClientsCallback extends LCCallback<List<String>> {
  public abstract void done(List<String> object, LCIMException e);

  @Override
  protected final void internalDone0(List<String> object, LCException error) {
    this.done(object, LCIMException.wrapperException(error));
  }
}