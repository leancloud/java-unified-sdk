package cn.leancloud.push.lite;

import android.os.Looper;
import android.util.Log;

public abstract class AVCallback<T> {
  public static boolean isMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }
  public void internalDone(final T t, final AVException avException) {
    if (mustRunOnUIThread() && !isMainThread()) {
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
