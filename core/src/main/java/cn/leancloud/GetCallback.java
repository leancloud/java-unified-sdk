package cn.leancloud;

import cn.leancloud.core.AVObject;

public abstract class GetCallback<T extends AVObject> extends AVCallback<T> {
  public abstract void done(T object, AVException e);

  protected final void internalDone0(T returnValue, AVException e) {
    done(returnValue, e);
  }
}
