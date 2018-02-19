package cn.leancloud;

public abstract class ProgressCallback extends AVCallback<Integer> {
  public abstract void done(Integer percentDone);

  /**
   * Override this function with your desired callback.
   */
  protected final void internalDone0(Integer returnValue, AVException e) {
    done(returnValue);
  }
}
