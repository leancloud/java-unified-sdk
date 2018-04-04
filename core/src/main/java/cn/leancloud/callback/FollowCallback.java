package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;

public abstract class FollowCallback<T extends AVObject> extends AVCallback<T> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   *
   * @param object The object that was retrieved, or null if it did not succeed.
   * @param e The exception raised by the save, or null if it succeeded.
   */
  public abstract void done(T object, AVException e);

  protected final void internalDone0(T returnValue, AVException e) {
    done(returnValue, e);
  }
}
