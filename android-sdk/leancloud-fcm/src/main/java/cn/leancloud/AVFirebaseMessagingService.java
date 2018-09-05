package cn.leancloud;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import cn.leancloud.push.AVPushMessageListener;
import cn.leancloud.push.AndroidNotificationManager;
import cn.leancloud.utils.LogUtil;

/**
 * Created by fengjunwen on 2018/8/28.
 */

public class AVFirebaseMessagingService extends FirebaseMessagingService {
  private final static AVLogger LOGGER = LogUtil.getLogger(AVFirebaseMessagingService.class);

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
    if (null == data) {
      return;
    }
    LOGGER.d("received message from: " + remoteMessage.getFrom() + ", payload: " + data.toString());
    try {
      JSONObject jsonObject = JSON.parseObject(data.get("payload"));
      if (null != jsonObject) {
        String channel = jsonObject.getString("_channel");
        String action = jsonObject.getString("action");

        AndroidNotificationManager androidNotificationManager = AndroidNotificationManager.getInstance();
        androidNotificationManager.processGcmMessage(channel, action, jsonObject.toJSONString());
      }
    } catch (Exception ex) {
      LOGGER.e("failed to parse push data.", ex);
    }
  }

  @Override
  public void onDeletedMessages() {
    super.onDeletedMessages();
  }
}