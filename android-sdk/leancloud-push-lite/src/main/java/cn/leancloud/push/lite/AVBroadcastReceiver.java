package cn.leancloud.push.lite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AVBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      context.startService(new Intent(context, cn.leancloud.push.lite.PushService.class));
    } catch (Exception ex) {
      Log.e("AVBroadcastReceiver","failed to start PushService. cause: " + ex.getMessage());
    }
  }
}
