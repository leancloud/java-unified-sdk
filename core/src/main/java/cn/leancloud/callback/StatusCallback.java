package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.LCStatus;

public abstract class StatusCallback extends LCCallback<LCStatus> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   *
   * @param statusObject The objects matching the query, or null if it failed.
   * @param LCException The exception raised by the operation, or null if it succeeded.
   */
  public abstract void done(LCStatus statusObject, LCException LCException);

  @Override
  protected final void internalDone0(LCStatus returnValue, LCException e) {
    done(returnValue, e);
  }
}