package cn.leancloud.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.os.Process;

/**
 * Created by fengjunwen on 2018/8/11.
 */

public class AndroidUtil {
  public static boolean isMainThread() {
    return (Looper.myLooper() == Looper.getMainLooper());
  }

  public static int checkPermission(Context context, String permission) {
    if (null == context) {
      return PackageManager.PERMISSION_DENIED;
    }
    return context.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
  }
}
