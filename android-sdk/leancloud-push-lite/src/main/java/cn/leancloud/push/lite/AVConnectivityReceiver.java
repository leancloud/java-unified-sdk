package cn.leancloud.push.lite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AVConnectivityReceiver extends BroadcastReceiver {
  private final AVConnectivityListener listener;

  public AVConnectivityReceiver(AVConnectivityListener listener) {
    this.listener = listener;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (null == this.listener) {
      return;
    }
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    try {
      NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
      if (null == activeNetwork || !activeNetwork.isConnected()) {
        this.listener.onNotConnected(context);
        return;
      }
      if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
        this.listener.onMobile(context);
      } else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
        this.listener.onWifi(context);
      } else {
        this.listener.onOtherConnected(context);
      }
    } catch (Exception ex) {
      Log.w("AVConnectivityReceiver", "failed to call CONNECTIVITY_SERVICE, cause:" + ex.getMessage());
    }
  }
}
