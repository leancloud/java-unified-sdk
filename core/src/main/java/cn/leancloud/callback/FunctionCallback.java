package cn.leancloud.callback;

import cn.leancloud.LCException;

public abstract class FunctionCallback<T> extends LCCallback<T> {
  /**
   * Override this function with the code you want to run after the cloud function is complete.
   *
   * @param object The object that was returned by the cloud function.
   * @param e The exception raised by the cloud call, or null if it succeeded.
   */
  public abstract void done(T object, LCException e);

  protected final void internalDone0(T returnValue, LCException e) {
    done(returnValue, e);
  }
}