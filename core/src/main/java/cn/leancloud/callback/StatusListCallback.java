package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.AVStatus;

import java.util.List;

public abstract class StatusListCallback extends AVCallback<List<AVStatus>> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   *
   * @param statusObjects The objects matching the query, or null if it failed.
   * @param avException The exception raised by the find, or null if it succeeded.
   */
  public abstract void done(List<AVStatus> statusObjects, AVException avException);

  protected final void internalDone0(List<AVStatus> returnValue, AVException e) {
    done(returnValue, e);
  }
}