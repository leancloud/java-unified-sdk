package cn.leancloud.network;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;

import cn.leancloud.AVLogger;
import cn.leancloud.utils.LogUtil;

/**
 * Created by fengjunwen on 2018/8/7.
 */

public class AndroidNetworkingDetector implements NetworkingDetector {
  private static AVLogger LOGGER = LogUtil.getLogger(AndroidNetworkingDetector.class);

  private Context context = null;
  public AndroidNetworkingDetector(Context context) {
    this.context = context;
  }

  //@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
  @TargetApi(Build.VERSION_CODES.N)
  public boolean isConnected() {
    try {
      int hasPermission = ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_NETWORK_STATE);
      if (PackageManager.PERMISSION_GRANTED != hasPermission) {
        LOGGER.w("android.Manifest.permission.ACCESS_NETWORK_STATE is not granted.");
      } else {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (null != networkInfo && networkInfo.isConnected()) {
          return true;
        }
      }
    } catch (Exception ex) {
      LOGGER.w("failed to detect networking status.", ex);
    }
    return false;
  }

  @TargetApi(Build.VERSION_CODES.N)
  public NetworkingDetector.NetworkType getNetworkType() {
    NetworkType result = NetworkType.None;
    int hasPermission = ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_NETWORK_STATE);
    if (PackageManager.PERMISSION_GRANTED != hasPermission) {
      LOGGER.w("android.Manifest.permission.ACCESS_NETWORK_STATE is not granted.");
    } else {
      ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
      final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
      if (null != networkInfo) {
        switch (networkInfo.getType()) {
          case ConnectivityManager.TYPE_MOBILE:
            result = NetworkType.Mobile;
            break;
          case ConnectivityManager.TYPE_WIFI:
            result = NetworkType.WIFI;
            break;
          default:
            break;
        }
      }
    }
    return result;
  }
}
