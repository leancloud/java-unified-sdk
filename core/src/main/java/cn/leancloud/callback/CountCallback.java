package cn.leancloud.callback;

import cn.leancloud.LCException;

public abstract class CountCallback extends LCCallback<Integer> {

  /**
   * Override this function with the code you want to run after the count is complete.
   *
   * @param count The number of objects matching the query, or -1 if it failed.
   * @param e The exception raised by the count, or null if it succeeded.
   */
  public abstract void done(int count, LCException e);

  /**
   * internal done function.
   * @param returnValue return value.
   * @param e exception.
   */
  protected final void internalDone0(Integer returnValue, LCException e) {
    done(returnValue == null ? -1 : returnValue, e);
  }
}
