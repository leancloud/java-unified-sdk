package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.types.AVNull;

public abstract class DeleteCallback extends AVCallback<AVNull> {
  /**
   * Override this function with the code you want to run after the delete is complete.
   *
   * @param e The exception raised by the delete, or null if it succeeded.
   */
  public abstract void done(AVException e);

  protected final void internalDone0(AVNull returnValue, AVException e) {
    done(e);
  }
}