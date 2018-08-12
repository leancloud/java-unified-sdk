package cn.leancloud.livequery;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;

public abstract class AVLiveQuerySubscribeCallback extends AVCallback<Void> {

  public abstract void done(AVException e);

  @Override
  protected void internalDone0(Void aVoid, AVException avException) {
    done(avException);
  }
}
