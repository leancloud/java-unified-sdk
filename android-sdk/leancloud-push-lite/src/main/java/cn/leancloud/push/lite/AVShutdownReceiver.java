package cn.leancloud.push.lite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AVShutdownReceiver extends BroadcastReceiver {
  private AVShutdownListener listener;

  public AVShutdownReceiver(AVShutdownListener listener) {
    this.listener = listener;
  }

  public void onReceive(Context context, Intent intent) {
    this.listener.onShutdown(context);
  }
}
