package cn.leancloud.util;

import android.os.Looper;

/**
 * Created by fengjunwen on 2018/8/11.
 */

public class AndroidUtil {
  public static boolean isMainThread() {
    return (Looper.myLooper() == Looper.getMainLooper());
  }
}
