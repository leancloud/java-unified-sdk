package cn.leancloud.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by fengjunwen on 2018/7/3.
 */

public class LCShutdownReceiver extends BroadcastReceiver {
  private LCShutdownListener listener;

  public LCShutdownReceiver(LCShutdownListener listener) {
    this.listener = listener;
  }

  public void onReceive(Context context, Intent intent) {
    this.listener.onShutdown(context);
  }
}
