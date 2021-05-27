package cn.leancloud;

import cn.leancloud.callback.SendCallback;
import cn.leancloud.core.PaasClient;
import cn.leancloud.utils.LCUtils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.*;

public class LCPush {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCPush.class);

  private static final String deviceTypeTag = "deviceType";
  private static final Set<String> DEVICE_TYPES = new HashSet<String>();

  private static final String FlowControlTag = "flow_control";
  private static final String APNsTeamIdTag = "apns_team_id";
  private static final String APNsTopicTag = "topic";
  private static final String iOSEnvironmentTag = "prod";
  private static final String NotificationIdTag = "notification_id";
  private static final String RequestIdTag = "req_id";
  public static final String iOSEnvironmentDev = "dev";
  public static final String iOSEnvironmentProd = "prod";
  private static final int FlowControlMinValue = 1000;

  static {
    DEVICE_TYPES.add("android");
    DEVICE_TYPES.add("ios");
  }

  private final Set<String> channelSet;
  private LCQuery<? extends LCInstallation> pushQuery;
  private String cql;
  private long expirationTime;
  private long expirationTimeInterval;
  private final Set<String> pushTarget;
  private final Map<String, Object> pushData;
  private LCObject notification;
  private Date pushDate = null;
  private int flowControl = 0;          // add since v6.1.2

  private String iOSEnvironment = null; // add since v6.5.2
  private String APNsTopic = null;      // add since v6.5.2
  private String APNsTeamId = null;     // add since v6.5.2
  private String notificationId = null; // add since v6.5.2
  private String requestId = null;      // add since v6.5.2

  /**
   * Creates a new push notification. The default channel is the empty string, also known as the
   * global broadcast channel, but this value can be overridden using AVPush.setChannel(String),
   * AVPush.setChannels(Collection) or AVPush.setQuery(AVQuery). Before sending the push
   * notification you must call either AVPush.setMessage(String) or AVPush.setData(JSONObject).
   */
  public LCPush() {
    channelSet = new HashSet<String>();
    pushData = new HashMap<String, Object>();
    pushTarget = new HashSet<String>(DEVICE_TYPES);
    pushQuery = LCInstallation.getQuery();
  }

  /**
   * Return channel set.
   * @return channel set.
   */
  public Set<String> getChannelSet() {
    return channelSet;
  }

  /**
   * Return the instance of _Notification。
   *
   * @return notification instance.
   */
  public LCObject getNotification() {
    return notification;
  }

  /**
   * Return push query instance.
   * @return push query instance.
   */
  public LCQuery<? extends LCInstallation> getPushQuery() {
    return pushQuery;
  }

  /**
   * Get push date.
   * @return push date
   */
  public Date getPushDate() {
    return pushDate;
  }

  /**
   * Get expiration time.
   * @return expiration time
   */
  public long getExpirationTime() {
    return expirationTime;
  }

  /**
   * Get expiration time interval.
   * @return expiration time interval
   */
  public long getExpirationTimeInterval() {
    return expirationTimeInterval;
  }

  /**
   * Get push target.
   * @return push target
   */
  public Set<String> getPushTarget() {
    return pushTarget;
  }

  /**
   * Get push data.
   * @return push data
   */
  public Map<String, Object> getPushData() {
    return pushData;
  }

  /**
   * Get push flow control value.
   * @return flow control value.
   */
  public int getFlowControl() {
    return flowControl;
  }

  /**
   * set flow control for send speed.
   * flow control value indicates how many devices will be pushed per second.
   * the min value is 1000, if flowControl less than 1000, it will be replaced with 1000.
   *
   * @since 6.1.2
   * @param flowControl flow control value which stands how many devices will be pushed per second.
   */
  public void setFlowControl(int flowControl) {
    if (flowControl < FlowControlMinValue) {
      flowControl = FlowControlMinValue;
    }
    this.flowControl = flowControl;
  }

  /**
   * set iOS Environment(optinal, default is production environment).
   * When using Token Authentication, this parameter determines which of environment(dev or prod)
   * will become the push target.
   * @param iOSEnvironment iOS environment, allowed values as following:
   *                       AVPush.iOSEnvironmentDev("dev") - development environment
   *                       AVPush.iOSEnvironmentProd("prod") - production environment
   * @since 6.5.2
   */
  public void setiOSEnvironment(String iOSEnvironment) {
    this.iOSEnvironment = iOSEnvironment;
  }

  /**
   * set APNs Topic(optinal, only used by Token Authentication)
   * @param APNsTopic apns topic
   * @since 6.5.2
   */
  public void setAPNsTopic(String APNsTopic) {
    this.APNsTopic = APNsTopic;
  }

  /**
   * set APNs Team Id(optinal, only used by Token Authentication)
   * @param APNsTeamId apns team id.
   * @since 6.5.2
   */
  public void setAPNsTeamId(String APNsTeamId) {
    this.APNsTeamId = APNsTeamId;
  }

  /**
   * set notification id(optional).
   * at now, notification id's max length is 16 characters, only letter and number is valid.
   *
   * @param notificationId customized notification id.
   */
  public void setNotificationId(String notificationId) {
    this.notificationId = notificationId;
  }

  /**
   * set customized request id(optional).
   * at now, request id's max length is 16 characters, only letter and number is valid.
   * when many requests with the same request id within 5 minutes, only one request works.
   * @param requestId customized request id.
   */
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  /**
   * Sets the channel on which this push notification will be sent. The channel name must start with
   * a letter and contain only letters, numbers, dashes, and underscores. A push can either have
   * channels or a query. Setting this will unset the query.
   * @param channel  channel string.
   */
  public void setChannel(String channel) {
    channelSet.clear();
    channelSet.add(channel);
  }

  /**
   * Sets the collection of channels on which this push notification will be sent. Each channel name
   * must start with a letter and contain only letters, numbers, dashes, and underscores. A push can
   * either have channels or a query. Setting this will unset the query.
   *
   * @param channels channel collection.
   */
  public void setChannels(Collection<String> channels) {
    channelSet.clear();
    channelSet.addAll(channels);
  }

  /**
   * Sets the entire data of the push message. See the push guide for more details on the data
   * format. This will overwrite any data specified in AVPush.setMessage(String).
   *
   * @param data push data.
   * @since 1.4.4
   */
  public void setData(Map<String, Object> data) {
    this.pushData.put("data", data);
  }

  /**
   * Sets the entire data of the push message. See the push guide for more details on the data
   * format. This will overwrite any data specified in AVPush.setMessage(String).
   * @param data push data.
   */
  public void setData(JSONObject data) {
    try {
      Map<String, Object> map = new HashMap<String, Object>();
      Iterator<Map.Entry<String, Object>> iter = data.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<String, Object> entry = iter.next();
        map.put(entry.getKey(), entry.getValue());
      }
      this.pushData.put("data", map);
    } catch (Exception exception) {
      LOGGER.w(exception);
    }
  }

  private Date expirationDateTime() {
    return new Date(expirationTime);
  }

  /**
   * Set the push date at which the push will be sent.
   *
   * @param date The push date.
   */
  public void setPushDate(Date date) {
    this.pushDate = date;
  }

  /**
   * Sets a UNIX epoch timestamp at which this notification should expire, in seconds (UTC). This
   * notification will be sent to devices which are either online at the time the notification is
   * sent, or which come online before the expiration time is reached. Because device clocks are not
   * guaranteed to be accurate, most applications should instead use
   * AVPush.setExpirationTimeInterval(long).
   * @param time timestamp.
   */
  public void setExpirationTime(long time) {
    this.expirationTime = time;
  }

  /**
   * Sets the time interval after which this notification should expire, in seconds. This
   * notification will be sent to devices which are either online at the time the notification is
   * sent, or which come online within the given number of seconds of the notification being
   * received by AVOSCloud's server. An interval which is less than or equal to zero indicates that
   * the message should only be sent to devices which are currently online.
   * @param timeInterval time interval.
   */
  public void setExpirationTimeInterval(long timeInterval) {
    this.expirationTimeInterval = timeInterval;
  }

  /**
   * Sets the message that will be shown in the notification. This will overwrite any data specified
   * in AVPush.setData(JSONObject).
   * @param message push message.
   */
  public void setMessage(String message) {
    pushData.clear();
    Map<String, Object> map = LCUtils.createStringObjectMap("alert", message);
    pushData.put("data", map);
  }

  /**
   * set push target only android device.
   * @param pushToAndroid flag to push to android or not.
   */
  public void setPushToAndroid(boolean pushToAndroid) {
    if (pushToAndroid) {
      this.pushTarget.add("android");
    } else {
      this.pushTarget.remove("android");
    }
  }

  /**
   * set push target only ios device.
   * @param pushToIOS flag to push to iOS or not.
   */
  public void setPushToIOS(boolean pushToIOS) {
    if (pushToIOS) {
      this.pushTarget.add("ios");
    } else {
      this.pushTarget.remove("ios");
    }
  }

  /**
   * set push target only windows phone device.
   * @param pushToWP flag to push to Windows Phone or not.
   */
  public void setPushToWindowsPhone(boolean pushToWP) {
    if (pushToWP) {
      this.pushTarget.add("wp");
    } else {
      this.pushTarget.remove("wp");
    }
  }

  /**
   * Sets the query for this push for which this push notification will be sent. This query will be
   * executed in the AVOSCloud cloud; this push notification will be sent to Installations which
   * this query yields. A push can either have channels or a query. Setting this will unset the
   * channels.
   *
   * @param query A query to which this push should target. This must be a AVInstallation query.
   */
  public void setQuery(LCQuery<? extends LCInstallation> query) {
    this.pushQuery = query;
  }

  /**
   * 可以通过 cql来针对push进行筛选
   *
   * 请注意cql 的主体应该是_Installation表
   *
   * 请在设置cql的同时，不要设置pushTarget(ios,android,wp)
   *
   * @param cql query cql.
   * @since 2.6.7
   */
  public void setCloudQuery(String cql) {
    this.cql = cql;
  }

  /**
   * Clears both expiration values, indicating that the notification should never expire.
   */
  public void clearExpiration() {
    expirationTime = 0L;
    expirationTimeInterval = 0L;
  }

  /**
   * Sends this push notification while blocking this thread until the push notification has
   * successfully reached the AVOSCloud servers. Typically, you should use AVPush.sendInBackground()
   * instead of this, unless you are managing your own threading.
   */
  public void send() {
    sendInBackground().blockingFirst();
  }

  /**
   * Sends this push notification in a background thread. This is preferable to using send(), unless
   * your code is already running from a background thread.
   * @return observable instance.
   */
  public Observable<JSONObject> sendInBackground() {
    try {
      Map<String, Object> map = postDataMap();
      return PaasClient.getPushClient().sendPushRequest(map);
    } catch (Exception ex) {
      return Observable.error(ex);
    }
  }

  private Map<String, Object> pushChannelsData() {
    return LCUtils.createStringObjectMap("channels", channelSet);
  }

  private Map<String, Object> postDataMap() throws LCException {
    Map<String, Object> map = new HashMap<String, Object>();

    if (pushQuery != null) {
      if (pushTarget.size() == 0) {
        pushQuery.whereNotContainedIn(deviceTypeTag, DEVICE_TYPES);
      } else if (pushTarget.size() == 1) {
        pushQuery.whereEqualTo(deviceTypeTag, pushTarget.toArray()[0]);
      }
      Map<String, String> pushParameters = pushQuery.assembleParameters();
      if (pushParameters.keySet().size() > 0 && !StringUtil.isEmpty(cql)) {
        throw new IllegalStateException("You can't use AVQuery and Cloud query at the same time.");
      }
      for (Map.Entry<String, String> entry: pushParameters.entrySet()) {
        map.put(entry.getKey(), JSON.parse(entry.getValue()));
      }
    }

    if (!StringUtil.isEmpty(cql)) {
      map.put("cql", cql);
    }

    if (channelSet.size() > 0) {
      map.putAll(pushChannelsData());
    }

    if (this.expirationTime > 0) {
      map.put("expiration_time", this.expirationDateTime());
    }

    if (this.expirationTimeInterval > 0) {
      map.put("push_time", StringUtil.stringFromDate(new Date()));
      map.put("expiration_interval", Long.valueOf(this.expirationTimeInterval));
    }

    if (this.pushDate != null) {
      map.put("push_time", StringUtil.stringFromDate(pushDate));
    }

    if (this.flowControl > 0) {
      map.put(FlowControlTag, this.flowControl);
    }

    if (!StringUtil.isEmpty(this.iOSEnvironment)) {
      map.put(iOSEnvironmentTag, this.iOSEnvironment);
    }

    if (!StringUtil.isEmpty(this.APNsTopic)) {
      map.put(APNsTopicTag, this.APNsTopic);
    }

    if (!StringUtil.isEmpty(this.APNsTeamId)) {
      map.put(APNsTeamIdTag, this.APNsTeamId);
    }

    if (!StringUtil.isEmpty(this.notificationId)) {
      map.put(NotificationIdTag, this.notificationId);
    }

    map.putAll(pushData);
    return map;
  }

  /**
   * Sends this push notification in a background thread. This is preferable to using send(), unless
   * your code is already running from a background thread.
   *
   * @param callback callback.done(e) is called when the send completes.
   */
  public void sendInBackground(final SendCallback callback) {
    sendInBackground().subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(JSONObject jsonObject) {
        notification = new LCObject("_Notification");
        notification.resetServerData(jsonObject.getInnerMap());
        if (null != callback) {
          callback.internalDone(null);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        if (null != callback) {
          callback.internalDone(new LCException(throwable));
        }
      }

      @Override
      public void onComplete() {

      }
    });
  }

  /**
   * A helper method to concisely send a push to a query. This method is equivalent to
   *
   * <pre>
   * AVPush push = new AVPush();
   * push.setData(data);
   * push.setQuery(query);
   * push.sendInBackground(callback);
   * </pre>
   *
   * @param data The entire data of the push message. See the push guide for more details on the
   *          data format.
   * @param query A AVInstallation query which specifies the recipients of a push.
   * @param callback callback.done(e) is called when the send completes.
   */
  public static void sendDataInBackground(JSONObject data, LCQuery<? extends LCInstallation> query,
                                          final SendCallback callback) {
    LCPush push = new LCPush();
    push.setData(data);
    push.setQuery(query);
    push.sendInBackground(callback);
  }

  /**
   * A helper method to concisely send a push to a query. This method is equivalent to
   *
   * <pre>
   * AVPush push = new AVPush();
   * push.setData(data);
   * push.setQuery(query);
   * push.sendInBackground(callback);
   * </pre>
   *
   * @param data The entire data of the push message. See the push guide for more details on the
   *          data format.
   * @param query A AVInstallation query which specifies the recipients of a push.
   * @return observable instance.
   */
  public static Observable<JSONObject> sendDataInBackground(JSONObject data, LCQuery<? extends LCInstallation> query) {
    LCPush push = new LCPush();
    push.setData(data);
    push.setQuery(query);
    return push.sendInBackground();
  }

  /**
   * A helper method to concisely send a push message to a query. This method is equivalent to
   *
   * <pre>
   * AVPush push = new AVPush();
   * push.setMessage(message);
   * push.setQuery(query);
   * push.sendInBackground();
   * </pre>
   *
   * @param message The message that will be shown in the notification.
   * @param query A AVInstallation query which specifies the recipients of a push.
   * @return observable instance.
   */
  public static Observable<JSONObject> sendMessageInBackground(String message, LCQuery<? extends LCInstallation> query) {
    LCPush push = new LCPush();
    push.setMessage(message);
    push.setQuery(query);
    return push.sendInBackground();
  }

  /**
   * A helper method to concisely send a push message to a query. This method is equivalent to
   *
   * <pre>
   * AVPush push = new AVPush();
   * push.setMessage(message);
   * push.setQuery(query);
   * push.sendInBackground(callback);
   * </pre>
   *
   * @param message The message that will be shown in the notification.
   * @param query A AVInstallation query which specifies the recipients of a push.
   * @param callback callback.done(e) is called when the send completes.
   */
  public static void sendMessageInBackground(String message,
                                             LCQuery<? extends LCInstallation> query, final SendCallback callback) {
    LCPush push = new LCPush();
    push.setMessage(message);
    push.setQuery(query);
    push.sendInBackground(callback);
  }
}
