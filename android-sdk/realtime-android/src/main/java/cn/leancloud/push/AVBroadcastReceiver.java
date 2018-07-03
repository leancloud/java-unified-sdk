package cn.leancloud.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.leancloud.AVLogger;
import cn.leancloud.utils.LogUtil;

/**
 * Created by fengjunwen on 2018/7/3.
 */

public class AVBroadcastReceiver extends BroadcastReceiver {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVBroadcastReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    // intent完全有可能是null的情况，就太糟糕了
    // 难道刚刚开机的时候移动ISP还没有识别出来的时候就不去尝试连接了么？
    // if (AVUtils.isConnected(context)) {
    try {
      context.startService(new Intent(context, cn.leancloud.push.PushService.class));
    } catch (Exception ex) {
      LOGGER.e("failed to start PushService. cause: " + ex.getMessage());
    }
    // }
  }
}
