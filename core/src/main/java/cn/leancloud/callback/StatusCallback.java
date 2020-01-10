package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.AVStatus;

public abstract class StatusCallback extends AVCallback<AVStatus> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   *
   * @param statusObject The objects matching the query, or null if it failed.
   * @param avException The exception raised by the operation, or null if it succeeded.
   */
  public abstract void done(AVStatus statusObject, AVException avException);

  @Override
  protected final void internalDone0(AVStatus returnValue, AVException e) {
    done(returnValue, e);
  }
}