package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.types.LCNull;

public abstract class DeleteCallback extends LCCallback<LCNull> {
  /**
   * Override this function with the code you want to run after the delete is complete.
   *
   * @param e The exception raised by the delete, or null if it succeeded.
   */
  public abstract void done(LCException e);

  protected final void internalDone0(LCNull returnValue, LCException e) {
    done(e);
  }
}