package cn.leancloud.callback;

import cn.leancloud.AVException;

public abstract class CountCallback extends AVCallback<Integer> {

  /**
   * Override this function with the code you want to run after the count is complete.
   *
   * @param count The number of objects matching the query, or -1 if it failed.
   * @param e The exception raised by the count, or null if it succeeded.
   */
  public abstract void done(int count, AVException e);

  /**
   * internal done function.
   * @param returnValue return value.
   * @param e exception.
   */
  protected final void internalDone0(Integer returnValue, AVException e) {
    done(returnValue == null ? -1 : returnValue, e);
  }
}
