package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.callback.AVCallback;

public abstract class SaveCallback<T extends AVObject> extends AVCallback<T> {

  /**
   * Override this function with the code you want to run after the save is complete.
   *
   * @param e The exception raised by the save, or null if it succeeded.
   */
  public abstract void done(AVException e);

  protected final void internalDone0(T returnValue, AVException e) {
    done(e);
  }
}
