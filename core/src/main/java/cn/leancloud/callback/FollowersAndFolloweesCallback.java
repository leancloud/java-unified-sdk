package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.LCObject;

import java.util.List;
import java.util.Map;

public abstract class FollowersAndFolloweesCallback<T extends LCObject>
        extends LCCallback<Map<String, List<T>>> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   *
   * @param avObjects The objects matching the query, or null if it failed.
   * @param LCException The exception raised by the find, or null if it succeeded.
   */
  public abstract void done(Map<String, List<T>> avObjects, LCException LCException);

  protected final void internalDone0(Map<String, List<T>> returnValue, LCException e) {
    done(returnValue, e);
  }
}