package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;

import java.util.List;

public abstract class FindCallback<T extends AVObject> extends AVCallback<java.util.List<T>> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   *
   * @param avObjects The objects matching the query, or null if it failed.
   * @param avException The exception raised by the find, or null if it succeeded.
   */
  public abstract void done(List<T> avObjects, AVException avException);

  protected final void internalDone0(List<T> returnValue, AVException e) {
    done(returnValue, e);
  }
}
