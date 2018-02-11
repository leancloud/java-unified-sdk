package cn.leancloud;

public abstract class SaveCallback extends AVCallback<Void> {

  /**
   * Override this function with the code you want to run after the save is complete.
   *
   * @param e The exception raised by the save, or null if it succeeded.
   */
  public abstract void done(AVException e);

  protected final void internalDone0(java.lang.Void returnValue, AVException e) {
    done(e);
  }
}
