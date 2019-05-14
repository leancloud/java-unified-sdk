package cn.leancloud.push.lite;

import android.util.Log;

import cn.leancloud.push.lite.utils.AVUtils;

public abstract class AVCallback<T> {

  public void internalDone(final T t, final AVException avException) {
    if (mustRunOnUIThread() && !AVUtils.isMainThread()) {
      if (!AVOSCloud.handler.post(new Runnable() {
        @Override
        public void run() {
          internalDone0(t, avException);
        }
      })) {
        Log.e("AVCallback","Post runnable to handler failed.");
      }
    } else {
      internalDone0(t, avException);
    }
  }

  protected boolean mustRunOnUIThread() {
    return true;
  }

  public void internalDone(final AVException avException) {
    this.internalDone(null, avException);
  }

  protected abstract void internalDone0(T t, AVException avException);
}
