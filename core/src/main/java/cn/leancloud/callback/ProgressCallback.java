package cn.leancloud.callback;

import cn.leancloud.AVException;

public abstract class ProgressCallback extends AVCallback<Integer> {
  public abstract void done(Integer percentDone);

  /**
   * Override this function with your desired callback.
   * @param returnValue actual progress value.
   * @param e exception.
   */
  protected final void internalDone0(Integer returnValue, AVException e) {
    done(returnValue);
  }
}
