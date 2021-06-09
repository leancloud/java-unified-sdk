package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.LCUser;

public abstract class LogInCallback<T extends LCUser> extends LCCallback<T> {
  /**
   * Override this function with the code you want to run after the save is complete.
   *
   * @param user The user that logged in, if the username and password is valid.
   * @param e The exception raised by the login.
   */
  public abstract void done(T user, LCException e);

  protected final void internalDone0(T returnValue, LCException e) {
    done(returnValue, e);
  }
}