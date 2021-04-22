package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.LCUser;

public abstract class SignUpCallback extends LCCallback<LCUser> {

  /**
   * Override this function with the code you want to run after the signUp is complete.
   *
   * @param e The exception raised by the signUp, or null if it succeeded.
   */
  public abstract void done(LCException e);

  protected final void internalDone0(LCUser t, LCException LCException) {
    this.done(LCException);
  }

}