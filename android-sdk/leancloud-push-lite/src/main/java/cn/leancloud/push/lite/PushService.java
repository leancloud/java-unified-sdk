package cn.leancloud.push.lite;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.leancloud.push.lite.utils.StringUtil;
import cn.leancloud.push.lite.ws.AVConnectionManager;
import cn.leancloud.push.lite.ws.AVPushMessageListener;

public class PushService extends Service {
  private static final String TAG = PushService.class.getSimpleName();

  static final String AV_PUSH_SERVICE_APPLICATION_ID = "AV_APPLICATION_ID";
  static final String AV_PUSH_SERVICE_DEFAULT_CALLBACK = "AV_DEFAULT_CALLBACK";
  static final String SERVICE_RESTART_ACTION = "com.avos.avoscloud.notify.action";

  // 是否需要唤醒其他同样使用 LeanCloud 服务的 app，此变量用于缓存结果，避免无意义调用
  private static boolean isNeedNotifyApplication = true;

  private AVConnectionManager connectionManager = null;
  private static Object connecting = new Object();
  private volatile static boolean isStarted = false;

  private static boolean isAutoWakeUp = true;

  AVConnectivityReceiver connectivityReceiver;
  AVShutdownReceiver shutdownReceiver;
//  private Timer cleanupTimer = new Timer();

  public static String DefaultChannelId = "";

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "PushServer#onCreate");
    connectionManager = AVConnectionManager.getInstance(this);
    new Thread(new Runnable() {
      @Override
      public void run() {
        boolean connected = isConnected();
        if (connected && !connectionManager.isConnectionEstablished()) {
          Log.d(TAG,"networking is fine and try to start connection to leancloud.");
          synchronized (connecting) {
            connectionManager.startConnection(new AVCallback<Integer>() {
              @Override
              protected void internalDone0(Integer resultCode, AVException exception) {
                if (null == exception) {
                  Log.d(TAG, "succeed to establish connection.");
                } else {
                  Log.w(TAG,"failed to start connection. cause:", exception);
                }
              }
            });
          }
        }
      }
    }).start();
    connectivityReceiver = new AVConnectivityReceiver(new AVConnectivityListener() {
      private volatile boolean connectEstablished = false;

      @Override
      public void onMobile(Context context) {
        connectEstablished = true;
        connectionManager.startConnection();
        Log.d(TAG, "Connection resumed with Mobile...");
      }

      @Override
      public void onWifi(Context context) {
        connectEstablished = true;
        connectionManager.startConnection();
        Log.d(TAG, "Connection resumed with Wifi...");
      }

      public void onOtherConnected(Context context) {
        Log.d(TAG, "Connectivity resumed with Others");
        connectEstablished = true;
        connectionManager.startConnection();
      }

      @Override
      public void onNotConnected(Context context) {
        if(!connectEstablished) {
          Log.d(TAG, "Connectivity isn't established yet.");
          return;
        }
        Log.d(TAG, "Connectivity broken");
        connectEstablished = false;
      }
    });
    registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    shutdownReceiver = new AVShutdownReceiver(new AVShutdownListener() {
      @Override
      public void onShutdown(Context context) {
        connectionManager.cleanup();
      }
    });
    registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

    isStarted = true;
  }

  @TargetApi(Build.VERSION_CODES.N)
  private boolean isConnected() {
    return isConnected(this);
  }

  @TargetApi(Build.VERSION_CODES.N)
  private static boolean isConnected(Context context) {
    try {
      int hasPermission = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);
      if (PackageManager.PERMISSION_GRANTED != hasPermission) {
        Log.w(TAG,"android.Manifest.permission.ACCESS_NETWORK_STATE is not granted.");
      } else {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (null != networkInfo && networkInfo.isConnected()) {
          return true;
        }
      }
    } catch (Exception ex) {
      Log.w(TAG,"failed to detect networking status.", ex);
    }
    return false;
  }

  @TargetApi(Build.VERSION_CODES.ECLAIR)
  @Override
  public int onStartCommand(final Intent intent, int flags, int startId) {
    Log.d(TAG,"PushService#onStartCommand");
    notifyOtherApplication(null != intent ? intent.getAction() : null);

    boolean connected = isConnected();
    if (connected && !connectionManager.isConnectionEstablished()) {
      if (AVOSCloud.isDebugLogEnabled()) {
        Log.d(TAG, "networking is fine and try to start connection to leancloud.");
      }
      synchronized (connecting) {
        connectionManager.startConnection(new AVCallback<Integer>() {
          @Override
          protected void internalDone0(Integer resultCode, AVException exception) {
            if (null == exception) {
              Log.d(TAG, "succeed to establish connection.");
            } else {
              Log.w(TAG,"failed to start connection. cause:", exception);
            }
          }
        });
      }
    }
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "PushService#onDestroy");
    connectionManager.cleanup();
    unregisterReceiver(this.connectivityReceiver);
    unregisterReceiver(this.shutdownReceiver);
    isStarted = false;

    if (isAutoWakeUp && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
      // Let's try to wake PushService again
      try {
        Intent i = new Intent(AVOSCloud.getContext(), PushService.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(i);
      } catch (Exception ex) {
        // i have tried my best.
        Log.e(TAG, "failed to start PushService. cause: " + ex.getMessage());
      }
    }
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG, "PushService#onBind");
    return null;
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  @Override
  public void onTaskRemoved(Intent rootIntent) {
    Log.d(TAG, "try to restart service on task Removed");
    if (isAutoWakeUp) {
      Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
      restartServiceIntent.setPackage(getPackageName());

      PendingIntent restartServicePendingIntent =
          PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent,
              PendingIntent.FLAG_UPDATE_CURRENT);
      AlarmManager alarmService =
          (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
      alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500,
          restartServicePendingIntent);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      super.onTaskRemoved(rootIntent);
    }
  }

  public static synchronized void subscribe(android.content.Context context,
                                            java.lang.String channel, java.lang.Class<? extends android.app.Activity> cls) {
    startServiceIfRequired(context, cls);
    final String finalChannel = channel;
    AVInstallation.getCurrentInstallation().addUnique("channels", finalChannel);
    _installationSaveHandler.sendMessage(Message.obtain());

    if (cls != null) {
      AVNotificationManager manager = AVNotificationManager.getInstance();
      manager.addDefaultPushCallback(channel, cls.getName());

      // set default push callback if it's not exist yet
      if (manager.getDefaultPushCallback(AVOSCloud.getApplicationId()) == null) {
        manager.addDefaultPushCallback(AVOSCloud.getApplicationId(), cls.getName());
      }
    }
  }

  public static void setNotificationIcon(int icon) {
    AVNotificationManager.getInstance().setNotificationIcon(icon);
  }

  public static void startIfRequired(android.content.Context context) {
    startServiceIfRequired(context, null);
  }

  public static void setDefaultPushCallback(android.content.Context context,
                                            java.lang.Class<? extends android.app.Activity> cls) {
    Log.d(TAG, "setDefaultPushCallback cls=" + cls.getName());
    startServiceIfRequired(context, cls);
    AVNotificationManager.getInstance().addDefaultPushCallback(AVOSCloud.getApplicationId(), cls.getName());
  }

  public static void setAutoWakeUp(boolean isAutoWakeUp) {
    PushService.isAutoWakeUp = isAutoWakeUp;
  }

  @TargetApi(Build.VERSION_CODES.O)
  public static void setDefaultChannelId(Context context, String channelId) {
    DefaultChannelId = channelId;
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
      // do nothing for Android versions before Ore
      return;
    }

    try {
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      CharSequence name = context.getPackageName();
      String description = "PushNotification";
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      android.app.NotificationChannel channel = new android.app.NotificationChannel(channelId, name, importance);
      channel.setDescription(description);
      notificationManager.createNotificationChannel(channel);
    } catch (Exception ex) {
      Log.w(TAG, "failed to create NotificationChannel, then perhaps PushNotification doesn't work well on Android O and newer version.");
    }
  }

  public static synchronized void unsubscribe(android.content.Context context,
                                              java.lang.String channel) {
    if (channel == null) {
      return;
    }
    AVNotificationManager.getInstance().removeDefaultPushCallback(channel);
    final java.lang.String finalChannel = channel;
    if (StringUtil.isEmpty(AVInstallation.getCurrentInstallation().getObjectId())) {
      AVInstallation.getCurrentInstallation().saveInBackground(new AVCallback<Void>() {
        @Override
        protected void internalDone0(Void aVoid, AVException avException) {
          if (null != avException) {
            Log.w(TAG, avException);
          } else {
            AVInstallation.getCurrentInstallation().removeAll("channels", Arrays.asList(finalChannel));
            _installationSaveHandler.sendMessage(Message.obtain());
          }
        }
      });
    } else {
      AVInstallation.getCurrentInstallation().removeAll("channels", Arrays.asList(finalChannel));
      _installationSaveHandler.sendMessage(Message.obtain());
    }
  }

  private static void startServiceIfRequired(Context context,
                                             final java.lang.Class<? extends android.app.Activity> cls) {
    if (isStarted) {
      return;
    }

    if (context == null) {
      Log.e(TAG,"context is null");
      return;
    }

    if (PackageManager.PERMISSION_GRANTED != context.checkCallingOrSelfPermission("android.permission.INTERNET")) {
      Log.e(TAG, "Please add <uses-permission android:name=\"android.permission.INTERNET\"/> in your AndroidManifest file");
      return;
    }

//    if (!isConnected(context)) {
//      Log.d(TAG, "No network available now");
//      return;
//    }

    if (!isPushServiceAvailable(context, PushService.class)) {
      Log.e(TAG, "Please add <service android:name=\"cn.leancloud.push.lite.PushService\"/> in your AndroidManifest file");
      return;
    }

    startService(context, cls);
  }

  private static boolean isPushServiceAvailable(Context context, final java.lang.Class cls) {
    final PackageManager packageManager = context.getPackageManager();
    final Intent intent = new Intent(context, cls);
    List resolveInfo =
        packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
    if (resolveInfo.size() > 0) {
      return true;
    }
    return false;
  }

  private static synchronized void startService(final Context context, final java.lang.Class cls) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "Start service");
        try {
          Intent intent = new Intent(context, PushService.class);
          intent.putExtra(AV_PUSH_SERVICE_APPLICATION_ID, AVOSCloud.getApplicationId());
          if (cls != null) {
            intent.putExtra(AV_PUSH_SERVICE_DEFAULT_CALLBACK, cls.getName());
          }
          context.startService(intent);
        } catch (Exception ex) {
          // i have tried my best.
          Log.e(TAG, "failed to start PushService. cause: " + ex.getMessage());
        }
      }
    }).start();
  }

  private void notifyOtherApplication(final String action) {
    if (isNeedNotifyApplication && !SERVICE_RESTART_ACTION.equals(action)) {
      // 每次 app 启动只需要唤醒一次就行了
      isNeedNotifyApplication = false;
      try {
        ServiceInfo info = getApplicationContext().getPackageManager().getServiceInfo(
            new ComponentName(getApplicationContext(), PushService.class), 0);
        if(info.exported) {
          NotifyUtil.notifyHandler.sendEmptyMessage(NotifyUtil.SERVICE_RESTART);
        }
      } catch (Exception e) {
        if (AVOSCloud.isDebugLogEnabled()) {
          Log.d(TAG, "failed to call notifyOtherApplication. cause: " + e.getMessage());
        }
      }
    }
  }

  private static Handler _installationSaveHandler = new Handler(Looper.getMainLooper()) {

    public void handleMessage(Message m) {

      AVInstallation.getCurrentInstallation().saveInBackground(new AVCallback<Void>() {
        @Override
        protected void internalDone0(Void aVoid, AVException avException) {
          if (null != avException) {
            Log.w(TAG,"failed to save Installation. cause: " + avException.getMessage());
          }
        }
      });
    }
  };
}
