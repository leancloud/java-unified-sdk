package cn.leancloud.push;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * Created by fengjunwen on 2018/7/3.
 */

public class PushService extends Service {
  @Override
  public void onCreate() {
    ;
  }

  @TargetApi(Build.VERSION_CODES.ECLAIR)
  @Override
  public int onStartCommand(final Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    ;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
