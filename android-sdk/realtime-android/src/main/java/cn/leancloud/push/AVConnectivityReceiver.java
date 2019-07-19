package cn.leancloud.push;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;

import cn.leancloud.utils.LogUtil;

/**
 * Created by fengjunwen on 2018/7/3.
 */

public class AVConnectivityReceiver extends BroadcastReceiver {
  private final AVConnectivityListener listener;
  private boolean connectivityBroken = false;

  public AVConnectivityReceiver(AVConnectivityListener listener) {
    this.listener = listener;
  }

  public boolean isConnectivityBroken() {
    return connectivityBroken;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (null == this.listener || null == context) {
      return;
    }
    int hasPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
    if (PackageManager.PERMISSION_GRANTED != hasPermission) {
      LogUtil.getLogger(AVConnectivityReceiver.class).w("android.Manifest.permission.ACCESS_NETWORK_STATE is not granted.");
      return;
    }

    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    try {
      NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
      if (null == activeNetwork || !activeNetwork.isConnected()) {
        this.listener.onNotConnected(context);
        connectivityBroken = true;
        return;
      }
      connectivityBroken = false;
      if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
        this.listener.onMobile(context);
      } else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
        this.listener.onWifi(context);
      } else {
        this.listener.onOtherConnected(context);
      }
    } catch (Exception ex) {
      ;
    }
  }
}
