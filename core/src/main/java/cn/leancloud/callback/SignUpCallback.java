package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.AVUser;

public abstract class SignUpCallback extends AVCallback<AVUser> {

  /**
   * Override this function with the code you want to run after the signUp is complete.
   *
   * @param e The exception raised by the signUp, or null if it succeeded.
   */
  public abstract void done(AVException e);

  protected final void internalDone0(AVUser t, AVException avException) {
    this.done(avException);
  }

}