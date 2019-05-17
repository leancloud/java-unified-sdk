package cn.leancloud;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.support.v4.app.ActivityCompat;

import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 2016/11/7.
 * 判断 AndroidManifest 中的各种注册条件
 */

public class AVManifestUtils {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVManifestUtils.class);

  /**
   * 判断 Mainifest 中是否包含对应到 permission
   * 如有，则返回 true，反之，则返回 false 并输出日志
   *
   * @param context
   * @param permission
   * @return
   */
  public static boolean checkPermission(Context context, String permission) {
    boolean hasPermission =
        (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context, permission));
    if (!hasPermission) {
      printErrorLog("permission " + permission + " is missing!");
    }
    return hasPermission;
  }

  /**
   * 判断 Mainifest 中是否包含对应到 Service
   * 如有，则返回 true，反之，则返回 false 并输出日志
   *
   * @param context
   * @param service
   * @return
   */
  public static boolean checkService(Context context, Class<?> service) {
    try {
      ServiceInfo info = context.getPackageManager().getServiceInfo(
          new ComponentName(context, service), 0);
      return null != info;
    } catch (PackageManager.NameNotFoundException e) {
      printErrorLog("service " + service.getName() + " is missing!");
      return false;
    }
  }

  /**
   * 判断 Mainifest 中是否包含对应到 Receiver
   * 如有，则返回 true，反之，则返回 false 并输出日志
   *
   * @param context
   * @param receiver
   * @return
   */
  public static boolean checkReceiver(Context context, Class<?> receiver) {
    try {
      ActivityInfo info = context.getPackageManager().getReceiverInfo(
          new ComponentName(context, receiver), 0);
      return null != info;
    } catch (PackageManager.NameNotFoundException e) {
      printErrorLog("receiver " + receiver.getName() + " is missing!");
      return false;
    }
  }

  private static void printErrorLog(String error) {
    if (!StringUtil.isEmpty(error)) {
      LOGGER.e(error);
    }
  }
}