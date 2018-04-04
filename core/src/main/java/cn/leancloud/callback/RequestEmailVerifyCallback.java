package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.types.AVNull;

public abstract class RequestEmailVerifyCallback extends AVCallback<AVNull> {
  /**
   * Override this function with the code you want to run after the request is complete.
   *
   * @param e The exception raised by the save, or null if no account is associated with the email
   *          address.
   */
  public abstract void done(AVException e);

  @Override
  protected void internalDone0(AVNull t, AVException avException) {
    this.done(avException);
  }
}