package cn.leancloud.push;

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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;

import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.leancloud.AVException;
import cn.leancloud.AVInstallation;
import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.im.AndroidInitializer;
import cn.leancloud.im.DirectlyOperationTube;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.im.v2.Conversation.AVIMOperation;
import cn.leancloud.im.v2.AVIMClient.AVIMClientStatus;
import cn.leancloud.livequery.AVLiveQuery;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.session.AVSession;
import cn.leancloud.session.AVSessionManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static cn.leancloud.im.v2.AVIMClient.AVIMClientStatus.AVIMClientStatusNone;

/**
 * PushService
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
  DirectlyOperationTube directlyOperationTube;
  private Timer cleanupTimer = new Timer();

  @Override
  public void onCreate() {
    LOGGER.d("PushService#onCreate");
    super.onCreate();

    AndroidNotificationManager.getInstance().setServiceContext(this);
    AndroidNotificationManager.getInstance().setNotificationIcon(this.getApplicationInfo().icon);

    directlyOperationTube = new DirectlyOperationTube(true);

    connectionManager = AVConnectionManager.getInstance();
    new Thread(new Runnable() {
      @Override
      public void run() {
        connectionManager.startConnection();
      }
    }).start();

    connectivityReceiver = new AVConnectivityReceiver(new AVConnectivityListener() {
      private volatile boolean connectionEstablished = false;

      @Override
      public void onMobile(Context context) {
        LOGGER.d("Connection resumed with Mobile...");
        connectionEstablished = true;
        connectionManager.startConnection();
      }

      @Override
      public void onWifi(Context context) {
        LOGGER.d("Connection resumed with Wifi...");
        connectionEstablished = true;
        connectionManager.startConnection();
      }

      public void onOtherConnected(Context context) {
        LOGGER.d("Connectivity resumed with Others");
        connectionEstablished = true;
        connectionManager.startConnection();
      }

      @Override
      public void onNotConnected(Context context) {
        if(!connectionEstablished) {
          LOGGER.d("Connectivity isn't established yet.");
          return;
        }
        LOGGER.d("Connectivity broken");
        connectionEstablished = false;
        cleanupTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            if (!connectionEstablished) {
              LOGGER.d("reset Connection now.");
              connectionManager.resetConnection();
            } else {
              LOGGER.d("Connection has been resumed");
            }
          }
        }, 3000);
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
      LOGGER.d("networking is fine and try to start connection to leancloud.");
      synchronized (connecting) {
        connectionManager.startConnection(new AVCallback<Integer>() {
          @Override
          protected void internalDone0(Integer resultCode, AVException exception) {
            if (null == exception) {
              processIMRequests(intent);
            } else {
              LOGGER.w("failed to start connection. cause:", exception);
              processRequestsWithException(intent, exception);
            }
          }
        });
      }
    } else if (!connected) {
      LOGGER.d("network is broken, try to re-connect to leancloud for user action.");
      if (connectionManager.isConnectionEstablished()) {
        connectionManager.cleanup();
      }
      synchronized (connecting) {
        connectionManager.startConnection(new AVCallback<Integer>() {
          @Override
          protected void internalDone0(Integer resultCode, AVException exception) {
            if (null == exception) {
              processIMRequests(intent);
            } else {
              LOGGER.w("failed to start connection. cause:", exception);
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

  /*
 * https://groups.google.com/forum/#!topic/android-developers/H-DSQ4-tiac
 * @see android.app.Service#onTaskRemoved(android.content.Intent)
 */
  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  @Override
  public void onTaskRemoved(Intent rootIntent) {
    LOGGER.d("try to restart service on task Removed");

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
      AVNotificationManager manager = AVPushMessageListener.getInstance().getNotificationManager();
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
    AVPushMessageListener.getInstance().getNotificationManager().setNotificationIcon(icon);
  }

  /**
   * Start Service explicitly.
   * In generally, you don't need to call this method to start service manually.
   * Only for LiveQuery, while you don't use LeanPush and LeanMessage, it is mandatory to call this method
   * within Application#onCreate, otherwise you will encounter issue on `cn.leancloud.websocket.AVStandardWebSocketClient.send` invocation.
   *
   * @param context context
   */
  public static void startIfRequired(android.content.Context context) {
    startServiceIfRequired(context, null);
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
    LOGGER.d("setDefaultPushCallback cls=" + cls.getName());
    startServiceIfRequired(context, cls);
    AVPushMessageListener.getInstance().getNotificationManager().addDefaultPushCallback(AVOSCloud.getApplicationId(), cls.getName());
  }

  /**
   * Set whether to automatically wake up PushService
   * @param isAutoWakeUp the default value is true
   */
  public static void setAutoWakeUp(boolean isAutoWakeUp) {
    PushService.isAutoWakeUp = isAutoWakeUp;
  }

  /**
   * Set default channel for Android Oreo or newer version
   * Notice: it isn"t necessary to invoke this method for any Android version before Oreo.
   *
   * @param context   context
   * @param channelId default channel id.
   */
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
      LOGGER.w("failed to create NotificationChannel, then perhaps PushNotification doesn't work well on Android O and newer version.");
    }
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
    AVPushMessageListener.getInstance().getNotificationManager().removeDefaultPushCallback(channel);
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

//  @TargetApi(Build.VERSION_CODES.N)
  private static void startServiceIfRequired(Context context,
                                             final java.lang.Class<? extends android.app.Activity> cls) {
    if (isStarted) {
      return;
    }

    if (context == null) {
      LOGGER.d("context is null");
      return;
    }

    if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, "android.permission.INTERNET")) {
      LOGGER.e("Please add <uses-permission android:name=\"android.permission.INTERNET\"/> in your AndroidManifest file");
      return;
    }

    if (!isPushServiceAvailable(context, PushService.class)) {
      LOGGER.e("Please add <service android:name=\"cn.leancloud.push.PushService\"/> in your AndroidManifest file");
      return;
    }

    if (!AppConfiguration.getGlobalNetworkingDetector().isConnected()) {
      LOGGER.d( "No network available now");
    }

    AndroidInitializer.init(context);

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
        LOGGER.d( "Start service");
        try {
          Intent intent = new Intent(context, PushService.class);
          intent.putExtra(AV_PUSH_SERVICE_APPLICATION_ID, AVOSCloud.getApplicationId());
          if (cls != null) {
            intent.putExtra(AV_PUSH_SERVICE_DEFAULT_CALLBACK, cls.getName());
          }
          context.startService(intent);
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
    LOGGER.d("processIMRequests...");
    if (null == intent) {
      LOGGER.w("intent is null, invalid operation.");
      return;
    }
    if (Conversation.AV_CONVERSATION_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
      processIMRequestsFromClient(intent);
    } else {
      processLiveQueryRequestsFromClient(intent);
    }
  }

  private void processIMRequestsFromClient(Intent intent) {
    LOGGER.d("processIMRequestsFromClient...");

    String clientId = intent.getExtras().getString(Conversation.INTENT_KEY_CLIENT);

    int requestId = intent.getExtras().getInt(Conversation.INTENT_KEY_REQUESTID);
    Conversation.AVIMOperation operation = AVIMOperation.getAVIMOperation(
        intent.getExtras().getInt(Conversation.INTENT_KEY_OPERATION));

    String keyData = intent.getExtras().getString(Conversation.INTENT_KEY_DATA);
    AVIMMessage existedMessage = null;
    Map<String, Object> param = null;
    if (!StringUtil.isEmpty(keyData)) {
      param = JSON.parseObject(keyData, Map.class);
    }
    String conversationId = intent.getExtras().getString(Conversation.INTENT_KEY_CONVERSATION);
    int convType = intent.getExtras().getInt(Conversation.INTENT_KEY_CONV_TYPE, Conversation.CONV_TYPE_NORMAL);

    switch (operation) {
      case CLIENT_OPEN:
        String tag = (String) param.get(Conversation.PARAM_CLIENT_TAG);
        String userSession = (String) param.get(Conversation.PARAM_CLIENT_USERSESSIONTOKEN);
        boolean reConnection = (boolean) param.get(Conversation.PARAM_CLIENT_RECONNECTION);
        this.directlyOperationTube.openClientDirectly(clientId, tag, userSession, reConnection, requestId);
        break;
      case CLIENT_DISCONNECT:
        this.directlyOperationTube.closeClientDirectly(clientId, requestId);
        break;
      case CLIENT_REFRESH_TOKEN:
        this.directlyOperationTube.renewSessionTokenDirectly(clientId, requestId);
        break;
      case CLIENT_STATUS:
        AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
        AVIMClientStatus status = AVIMClientStatusNone;
        if (AVSession.Status.Opened != session.getCurrentStatus()) {
          status = AVIMClientStatus.AVIMClientStatusPaused;
        } else {
          status = AVIMClientStatus.AVIMClientStatusOpened;
        }
        HashMap<String, Object> bundle = new HashMap<>();
        bundle.put(Conversation.callbackClientStatus, status.getCode());
        InternalConfiguration.getOperationTube().onOperationCompletedEx(clientId, null,
            requestId, AVIMOperation.CLIENT_STATUS, bundle);
        break;
      case CLIENT_ONLINE_QUERY:
        List<String> idList = (List<String>) param.get(Conversation.PARAM_ONLINE_CLIENTS);
        this.directlyOperationTube.queryOnlineClientsDirectly(clientId, idList, requestId);
        break;
      case CONVERSATION_CREATION:
        List<String> members = (List<String>) param.get(Conversation.PARAM_CONVERSATION_MEMBER);
        boolean isUnique = false;
        if (param.containsKey(Conversation.PARAM_CONVERSATION_ISUNIQUE)) {
          isUnique = (boolean) param.get(Conversation.PARAM_CONVERSATION_ISUNIQUE);
        }
        boolean isTransient = false;
        if (param.containsKey(Conversation.PARAM_CONVERSATION_ISTRANSIENT)) {
          isTransient = (boolean) param.get(Conversation.PARAM_CONVERSATION_ISTRANSIENT);
        }
        boolean isTemp = false;
        if (param.containsKey(Conversation.PARAM_CONVERSATION_ISTEMPORARY)) {
          isTemp = (boolean) param.get(Conversation.PARAM_CONVERSATION_ISTEMPORARY);
        }
        int tempTTL = isTemp ? (int) param.get(Conversation.PARAM_CONVERSATION_TEMPORARY_TTL) : 0;
        Map<String, Object> attributes = (Map<String, Object>) param.get(Conversation.PARAM_CONVERSATION_ATTRIBUTE);
        directlyOperationTube.createConversationDirectly(clientId, members, attributes, isTransient,
            isUnique, isTemp, tempTTL, requestId);
        break;
      case CONVERSATION_QUERY:
        this.directlyOperationTube.queryConversationsDirectly(clientId, keyData, requestId);
        break;
      case CONVERSATION_UPDATE:
        this.directlyOperationTube.updateConversationDirectly(clientId, conversationId, convType, param, requestId);
        break;
      case CONVERSATION_QUIT:
      case CONVERSATION_JOIN:
      case CONVERSATION_MUTE:
      case CONVERSATION_UNMUTE:
        this.directlyOperationTube.participateConversationDirectly(clientId, conversationId, convType,
            param, operation, requestId);
        break;
      case CONVERSATION_ADD_MEMBER:
      case CONVERSATION_RM_MEMBER:
      case CONVERSATION_MUTE_MEMBER:
      case CONVERSATION_UNMUTE_MEMBER:
      case CONVERSATION_UNBLOCK_MEMBER:
      case CONVERSATION_BLOCK_MEMBER:
      case CONVERSATION_PROMOTE_MEMBER:
      case CONVERSATION_BLOCKED_MEMBER_QUERY:
      case CONVERSATION_MUTED_MEMBER_QUERY:
      case CONVERSATION_FETCH_RECEIPT_TIME:
      case CONVERSATION_MEMBER_COUNT_QUERY:
        this.directlyOperationTube.processMembersDirectly(clientId, conversationId, convType, keyData,
            operation, requestId);
        break;
      case CONVERSATION_MESSAGE_QUERY:
        this.directlyOperationTube.queryMessagesDirectly(clientId, conversationId, convType, keyData,
            AVIMOperation.CONVERSATION_MESSAGE_QUERY, requestId);
        break;
      case CONVERSATION_READ:
        this.directlyOperationTube.markConversationReadDirectly(clientId, conversationId, convType,
            param, requestId);
        break;
      case CONVERSATION_RECALL_MESSAGE:
        existedMessage = AVIMMessage.parseJSONString(keyData);
        this.directlyOperationTube.recallMessageDirectly(clientId, convType, existedMessage, requestId);
        break;
      case CONVERSATION_SEND_MESSAGE:
        existedMessage = AVIMMessage.parseJSONString(keyData);;
        AVIMMessageOption option = AVIMMessageOption.parseJSONString(intent.getExtras().getString(Conversation.INTENT_KEY_MESSAGE_OPTION));
        this.directlyOperationTube.sendMessageDirectly(clientId, conversationId, convType,
            existedMessage, option, requestId);
        break;
      case CONVERSATION_UPDATE_MESSAGE:
        existedMessage = AVIMMessage.parseJSONString(keyData);;
        AVIMMessage secondMessage = AVIMMessage.parseJSONString(intent.getExtras().getString(Conversation.INTENT_KEY_MESSAGE_EX));
        this.directlyOperationTube.updateMessageDirectly(clientId, convType, existedMessage, secondMessage, requestId);
        break;
      default:
        LOGGER.w("not support operation: " + operation);
        break;
    }
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

  private void processLiveQueryRequestsFromClient(Intent intent) {
    if (null == intent) {
      LOGGER.w("intent is null");
      return;
    }
    String action = intent.getAction();
    if (AVLiveQuery.ACTION_LIVE_QUERY_LOGIN.equals(action)) {
      int requestId = intent.getExtras().getInt(Conversation.INTENT_KEY_REQUESTID);
      String subscriptionId = intent.getExtras().getString(AVLiveQuery.SUBSCRIBE_ID);
      this.directlyOperationTube.loginLiveQueryDirectly(subscriptionId, requestId);
    } else {
      LOGGER.w("unknown action: " + action);
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
