package cn.leancloud;

import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import cn.leancloud.push.AndroidNotificationManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by fengjunwen on 2018/8/28.
 */

public class LCFirebaseMessagingService extends FirebaseMessagingService {
  private final static LCLogger LOGGER = LogUtil.getLogger(LCFirebaseMessagingService.class);
  private final String VENDOR = "fcm";

  /**
   * FCM 有两种消息：通知消息与数据消息。
   * 通知消息 -- 就是普通的通知栏消息，应用在后台的时候，通知消息会直接显示在通知栏，默认情况下，
   * 用户点按通知即可打开应用启动器（通知消息附带的参数在 Bundle 内），我们无法处理。
   *
   * 数据消息 -- 类似于其他厂商的「透传消息」。应用在前台的时候，数据消息直接发送到应用内，应用层通过这一接口进行响应。
   *
   * @param remoteMessage
   */
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    Map<String, String> data = remoteMessage.getData();
    if (null == data || data.size() < 1) {
      return;
    }
    LOGGER.d("received message from: " + remoteMessage.getFrom() + ", payload: " + data.toString());

    if (remoteMessage.getNotification() == null) {
      return;
    }
    String title = remoteMessage.getNotification().getTitle();
    String alert = remoteMessage.getNotification().getBody();

    try {
      JSONObject jsonObject = JSON.parseObject(data.get("payload"));
      if (null != jsonObject) {
        String channel = jsonObject.getString("_channel");
        String action = jsonObject.getString("action");

        if (!StringUtil.isEmpty(title)) {
          jsonObject.put("title", title);
        }
        if (!StringUtil.isEmpty(alert)) {
          jsonObject.put("alert", alert);
        }
        AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
        androidNotificationManager.processFcmMessage(channel, action, jsonObject.toJSONString());
      }
    } catch (Exception ex) {
      LOGGER.e("failed to parse push data.", ex);
    }
  }

  @Override
  public void onNewToken(String token) {
    LOGGER.d("refreshed token: " + token);

    // If you want to send messages to this application instance or
    // manage this apps subscriptions on the server side, send the
    // FCM registration token to your app server.
    sendRegistrationToServer(token);
  }

  private void sendRegistrationToServer(String refreshedToken) {
    if (StringUtil.isEmpty(refreshedToken)) {
      return;
    }
    LCInstallation installation = LCInstallation.getCurrentInstallation();
    if (!VENDOR.equals(installation.getString(LCInstallation.VENDOR))) {
      installation.put(LCInstallation.VENDOR, VENDOR);
    }
    if (!refreshedToken.equals(installation.getString(LCInstallation.REGISTRATION_ID))) {
      installation.put(LCInstallation.REGISTRATION_ID, refreshedToken);
    }
    installation.saveInBackground().subscribe(
      ObserverBuilder.buildSingleObserver(new SaveCallback() {
      @Override
      public void done(LCException e) {
        if (null != e) {
          LOGGER.e("failed to update installation.", e);
        } else {
          LOGGER.d("succeed to update installation.");
        }
      }
    }));

    LOGGER.d("FCM registration success! registrationId=" + refreshedToken);
  }

  @Override
  public void onDeletedMessages() {
    super.onDeletedMessages();
  }
}