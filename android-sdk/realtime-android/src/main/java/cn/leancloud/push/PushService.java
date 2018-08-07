package cn.leancloud.push;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import java.util.Arrays;
import java.util.List;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * <p>
 * A service to listen for push notifications. This operates in the same process as the parent
 * application. To use this class, the PushService must be registered. Add this XML right before the
 * </application> tag in your AndroidManifest.xml:
 * </p>
 * <p/>
 * <pre>
 *    <service android:name="cn.leancloud.push.PushService" />
 *    <receiver android:name="cn.leancloud.push.AVBroadcastReceiver">
 *        <intent-filter>
 *            <action android:name="android.intent.action.BOOT_COMPLETED" />
 *            <action android:name="android.intent.action.USER_PRESENT" />
 *        </intent-filter>
 *    </receiver>
 * </pre>
 * <p>
 * Next, you must ensure your app has the permissions needed to show a notification. Make sure that
 * these permissions are present in your AndroidManifest.xml, typically immediately before the
 * </manifest> tag:
 * </p>
 * <p/>
 * <pre>
 *    <uses-permission android:name="android.permission.INTERNET" />
 *    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
 *    <uses-permission android:name="android.permission.VIBRATE" />
 * </pre>
 * <p>
 * Once push notifications are configured in the manifest, you can subscribe to a push channel by
 * calling
 * </p>
 * <p/>
 * <pre>
 * PushService.subscribe(context, &quot;the_channel_name&quot;, YourActivity.class);
 * </pre>
 * <p>
 * When the client receives a push message, a notification will appear in the system tray. When the
 * user taps the notification, they will enter the application through a new instance of
 * YourActivity.
 * </p>
 */

public class PushService extends Service {
  private static final AVLogger LOGGER = LogUtil.getLogger(PushService.class);

  static final String AV_PUSH_SERVICE_APPLICATION_ID = "AV_APPLICATION_ID";
  static final String AV_PUSH_SERVICE_DEFAULT_CALLBACK = "AV_DEFAULT_CALLBACK";
  static final String SERVICE_RESTART_ACTION = "com.avos.avoscloud.notify.action";

  // 是否需要唤醒其他同样使用 LeanCloud 服务的 app，此变量用于缓存结果，避免无意义调用
  private static boolean isNeedNotifyApplication = true;

  private AVConnectionManager connectionManager = null;
  private static Object connecting = new Object();
  private volatile static boolean isStarted = false;

  private static boolean isAutoWakeUp = true;
  static String DefaultChannelId = "";

  AVConnectivityReceiver connectivityReceiver;
  AVShutdownReceiver shutdownReceiver;

  @Override
  public void onCreate() {
    LOGGER.d("PushService#onCreate");
    super.onCreate();
    connectionManager = AVConnectionManager.getInstance();

    connectivityReceiver = new AVConnectivityReceiver(new AVConnectivityListener() {
      @Override
      public void onMobile(Context context) {
        connectionManager.startConnection();
      }

      @Override
      public void onWifi(Context context) {
        connectionManager.startConnection();
      }

      @Override
      public void onNotConnected(Context context) {
        LOGGER.d("Connection Lost...");
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

  @TargetApi(Build.VERSION_CODES.ECLAIR)
  @Override
  public int onStartCommand(final Intent intent, int flags, int startId) {
    LOGGER.d("PushService#onStartCommand");
    notifyOtherApplication(null != intent ? intent.getAction() : null);

    boolean connected = AppConfiguration.getGlobalNetworkingDetector().isConnected();
    if (connected && !connectionManager.isConnectionEstablished()) {
      synchronized (connecting) {
        connectionManager.startConnection(new AVCallback<Integer>() {
          @Override
          protected void internalDone0(Integer resultCode, AVException exception) {
            if (null == exception) {
              processIMRequests(intent);
            } else {
              processRequestsWithException(intent, exception);
            }
          }
        });
      }
    } else {
      processIMRequests(intent);
    }

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    LOGGER.d("PushService#onDestroy");
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
        LOGGER.e("failed to start PushService. cause: " + ex.getMessage());
      }
    }
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    LOGGER.d("PushService#onBind");
    return null;
  }

  /**
   * Helper function to subscribe to push notifications with the default application icon.
   *
   * @param context This is used to access local storage to cache the subscription, so it must
   *                currently be a viable context.
   * @param channel A string identifier that determines which messages will cause a push
   *                notification to be sent to this client. The channel name must start with a letter and
   *                contain only letters, numbers, dashes, and underscores.
   * @param cls     This should be a subclass of Activity. An instance of this Activity is started when
   *                the user responds to this push notification. If you are not sure what to use here,
   *                just
   *                use your application's main Activity subclass.
   */
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

  /**
   * 设置推送消息的Icon图标，如果没有设置，默认使用您配置里的应用图标。
   *
   * @param icon A resource ID in the application's package of the drawble to use.
   * @since 1.4.4
   */
  public static void setNotificationIcon(int icon) {
    AVNotificationManager.getInstance().setNotificationIcon(icon);
  }

  /**
   * Provides a default Activity class to handle pushes. Setting a default allows your program to
   * handle pushes that aren't registered with a subscribe call. This can happen when your
   * application changes its subscriptions directly through the AVInstallation or via push-to-query.
   *
   * @param context This is used to access local storage to cache the subscription, so it must
   *                currently be a viable context.
   * @param cls     This should be a subclass of Activity. An instance of this Activity is started when
   *                the user responds to this push notification. If you are not sure what to use here,
   *                just
   *                use your application's main Activity subclass.
   */
  public static void setDefaultPushCallback(android.content.Context context,
                                            java.lang.Class<? extends android.app.Activity> cls) {
    startServiceIfRequired(context, cls);
    AVNotificationManager.getInstance().addDefaultPushCallback(AVOSCloud.getApplicationId(), cls.getName());
  }

  /**
   * Cancels a previous call to subscribe. If the user is not subscribed to this channel, this is a
   * no-op. This call does not require internet access. It returns without blocking
   *
   * @param context A currently viable Context.
   * @param channel The string defining the channel to unsubscribe from.
   */
  public static synchronized void unsubscribe(android.content.Context context,
                                              java.lang.String channel) {
    if (channel == null) {
      return;
    }
    AVNotificationManager.getInstance().removeDefaultPushCallback(channel);
    final java.lang.String finalChannel = channel;
    if (StringUtil.isEmpty(AVInstallation.getCurrentInstallation().getObjectId())) {
      AVInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<AVObject>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(AVObject avObject) {
          AVInstallation.getCurrentInstallation().removeAll("channels", Arrays.asList(finalChannel));
          _installationSaveHandler.sendMessage(Message.obtain());
        }

        @Override
        public void onError(Throwable e) {
          LOGGER.w(e);
        }

        @Override
        public void onComplete() {

        }
      });
    } else {
      AVInstallation.getCurrentInstallation().removeAll("channels", Arrays.asList(finalChannel));
      _installationSaveHandler.sendMessage(Message.obtain());
    }
  }

  @TargetApi(Build.VERSION_CODES.N)
  private static void startServiceIfRequired(Context context,
                                             final java.lang.Class<? extends android.app.Activity> cls) {
    if (isStarted) {
      return;
    }

    if (context == null) {
      LOGGER.d("context is null");
      return;
    }

    if (PackageManager.PERMISSION_GRANTED != context.checkSelfPermission("android.permission.INTERNET")) {
      LOGGER.e("Please add <uses-permission android:name=\"android.permission.INTERNET\"/> in your AndroidManifest file");
      return;
    }

    if (!AppConfiguration.getGlobalNetworkingDetector().isConnected()) {
      LOGGER.d( "No network available now");
      return;
    }

    if (!isPushServiceAvailable(context, PushService.class)) {
      LOGGER.e("Please add <service android:name=\"cn.leancloud.push.PushService\"/> in your AndroidManifest file");
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

  private static synchronized void startService(Context context, final java.lang.Class cls) {
    final Context finalContext = context;
    new Thread(new Runnable() {
      @Override
      public void run() {
        LOGGER.d( "Start service");
        try {
          Intent intent = new Intent(finalContext, PushService.class);
          intent.putExtra(AV_PUSH_SERVICE_APPLICATION_ID, AVOSCloud.getApplicationId());
          if (cls != null) {
            intent.putExtra(AV_PUSH_SERVICE_DEFAULT_CALLBACK, cls.getName());
          }
          finalContext.startService(intent);
        } catch (Exception ex) {
          // i have tried my best.
          LOGGER.e("failed to start PushService. cause: " + ex.getMessage());
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
      } catch (PackageManager.NameNotFoundException e) {
      }
    }
  }

  private void processIMRequests(Intent intent) {
    // FIXME
  }

  private void processRequestsWithException(Intent intent, AVException exception) {
    if (intent != null
        && Conversation.AV_CONVERSATION_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
      int operationCode = intent.getExtras().getInt(Conversation.INTENT_KEY_OPERATION);
      String clientId = intent.getExtras().getString(Conversation.INTENT_KEY_CLIENT);
      String conversationId = intent.getExtras().getString(Conversation.INTENT_KEY_CONVERSATION);
      int requestId = intent.getExtras().getInt(Conversation.INTENT_KEY_REQUESTID);

      InternalConfiguration.getOperationTube().onOperationCompleted(clientId, conversationId, requestId,
          Conversation.AVIMOperation.getAVIMOperation(operationCode), exception);
    }
  }

  private static Handler _installationSaveHandler = new Handler(Looper.getMainLooper()) {

    public void handleMessage(Message m) {

      AVInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<AVObject>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(AVObject avObject) {

        }

        @Override
        public void onError(Throwable e) {
          if (e != null && "already has one request sending".equals(e.getMessage())) {
            _installationSaveHandler.removeMessages(0);
            Message m = Message.obtain();
            m.what = 0;
            _installationSaveHandler.sendMessageDelayed(m, 2000);
          }
        }

        @Override
        public void onComplete() {

        }
      });
    }
  };
}
