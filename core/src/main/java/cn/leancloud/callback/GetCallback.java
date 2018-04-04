package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.callback.AVCallback;

public abstract class GetCallback<T extends AVObject> extends AVCallback<T> {
  public abstract void done(T object, AVException e);

  protected final void internalDone0(T returnValue, AVException e) {
    done(returnValue, e);
  }
}
