package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.LCStatus;

import java.util.List;

public abstract class StatusListCallback extends LCCallback<List<LCStatus>> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   *
   * @param statusObjects The objects matching the query, or null if it failed.
   * @param LCException The exception raised by the operation, or null if it succeeded.
   */
  public abstract void done(List<LCStatus> statusObjects, LCException LCException);

  protected final void internalDone0(List<LCStatus> returnValue, LCException e) {
    done(returnValue, e);
  }
}