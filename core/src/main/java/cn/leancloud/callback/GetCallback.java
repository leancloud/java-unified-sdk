package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.LCObject;

public abstract class GetCallback<T extends LCObject> extends LCCallback<T> {
  public abstract void done(T object, LCException e);

  protected final void internalDone0(T returnValue, LCException e) {
    done(returnValue, e);
  }
}
