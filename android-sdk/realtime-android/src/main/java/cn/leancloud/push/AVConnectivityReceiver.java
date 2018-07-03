package cn.leancloud.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by fengjunwen on 2018/7/3.
 */

public class AVConnectivityReceiver extends BroadcastReceiver {
  private final AVConnectivityListener listener;

  public AVConnectivityReceiver(AVConnectivityListener listener) {
    this.listener = listener;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    ;
  }
}
