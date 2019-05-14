package cn.leancloud.push.lite.utils;

import android.os.Looper;

public class AVUtils {
  public static boolean isMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }
}
