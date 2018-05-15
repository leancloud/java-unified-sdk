package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.types.AVNull;

public abstract class SendCallback extends AVCallback<AVNull> {
  public abstract void done(AVException e);

  @Override
  protected final void internalDone0(AVNull t, AVException avException) {
    this.done(avException);
  }
}
