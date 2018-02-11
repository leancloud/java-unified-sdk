package cn.leancloud;

import cn.leancloud.utils.AVUtils;

public abstract class AVCallback<T> {
  public void internalDone(final T t, final AVException avException) {
    internalDone0(t, avException);
  }

  public void internalDone(final AVException avException) {
    this.internalDone(null, avException);
  }

  protected abstract void internalDone0(T t, AVException avException);
}
