package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.AVUser;

public abstract class LogInCallback<T extends AVUser> extends AVCallback<T> {
  /**
   * Override this function with the code you want to run after the save is complete.
   *
   * @param user The user that logged in, if the username and password is valid.
   * @param e The exception raised by the login.
   */
  public abstract void done(T user, AVException e);

  protected final void internalDone0(T returnValue, AVException e) {
    done(returnValue, e);
  }
}