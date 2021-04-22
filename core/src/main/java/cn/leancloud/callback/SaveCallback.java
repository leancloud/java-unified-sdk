package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.LCObject;

public abstract class SaveCallback<T extends LCObject> extends LCCallback<T> {

  /**
   * Override this function with the code you want to run after the save is complete.
   *
   * @param e The exception raised by the save, or null if it succeeded.
   */
  public abstract void done(LCException e);

  protected final void internalDone0(T returnValue, LCException e) {
    done(e);
  }
}
