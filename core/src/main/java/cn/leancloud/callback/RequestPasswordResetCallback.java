package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.types.LCNull;

public abstract class RequestPasswordResetCallback extends LCCallback<LCNull> {
  /**
   * Override this function with the code you want to run after the request is complete.
   *
   * @param e The exception raised by the save, or null if no account is associated with the email
   *          address.
   */
  public abstract void done(LCException e);

  @Override
  protected final void internalDone0(LCNull t, LCException LCException) {
    this.done(LCException);
  }


}
