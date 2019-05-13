package cn.leancloud.push.lite;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class PushService extends Service {

  public static String DefaultChannelId = "";

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
