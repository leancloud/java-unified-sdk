package cn.leancloud.push;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.callback.SendCallback;
import cn.leancloud.service.PushClient;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.*;

public class AVPush {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVPush.class);

  private static final String deviceTypeTag = "deviceType";
  private static final Set<String> DEVICE_TYPES = new HashSet<String>();
  static {
    DEVICE_TYPES.add("android");
    DEVICE_TYPES.add("ios");
  }
  private final Set<String> channelSet;
  private AVQuery<? extends AVInstallation> pushQuery;
  private String cql;
  private long expirationTime;
  private long expirationTimeInterval;
  private final Set<String> pushTarget;
  private final Map<String, Object> pushData;
  private volatile AVObject notification;
  private Date pushDate = null;

  /**
   * Creates a new push notification. The default channel is the empty string, also known as the
   * global broadcast channel, but this value can be overridden using AVPush.setChannel(String),
   * AVPush.setChannels(Collection) or AVPush.setQuery(AVQuery). Before sending the push
   * notification you must call either AVPush.setMessage(String) or AVPush.setData(JSONObject).
   */
  public AVPush() {
    channelSet = new HashSet<String>();
    pushData = new HashMap<String, Object>();
    pushTarget = new HashSet<String>(DEVICE_TYPES);
    pushQuery = AVInstallation.getQuery();
  }

  public Set<String> getChannelSet() {
    return channelSet;
  }

  /**
   * 返回推送后创建的_Notification对象。
   *
   * @return
   */
  public AVObject getNotification() {
    return notification;
  }

  public AVQuery<? extends AVInstallation> getPushQuery() {
    return pushQuery;
  }

  public Date getPushDate() {
    return pushDate;
  }

  public long getExpirationTime() {
    return expirationTime;
  }

  public long getExpirationTimeInterval() {
    return expirationTimeInterval;
  }

  public Set<String> getPushTarget() {
    return pushTarget;
  }

  public Map<String, Object> getPushData() {
    return pushData;
  }

  /**
   * Sets the channel on which this push notification will be sent. The channel name must start with
   * a letter and contain only letters, numbers, dashes, and underscores. A push can either have
   * channels or a query. Setting this will unset the query.
   */
  public void setChannel(String channel) {
    channelSet.clear();
    channelSet.add(channel);
  }

  /**
   * Sets the collection of channels on which this push notification will be sent. Each channel name
   * must start with a letter and contain only letters, numbers, dashes, and underscores. A push can
   * either have channels or a query. Setting this will unset the query.
   */
  public void setChannels(Collection<String> channels) {
    channelSet.clear();
    channelSet.addAll(channels);
  }

  /**
   * Sets the entire data of the push message. See the push guide for more details on the data
   * format. This will overwrite any data specified in AVPush.setMessage(String).
   *
   * @since 1.4.4
   */
  public void setData(Map<String, Object> data) {
    this.pushData.put("data", data);
  }

  /**
   * Sets the entire data of the push message. See the push guide for more details on the data
   * format. This will overwrite any data specified in AVPush.setMessage(String).
   */
  public void setData(JSONObject data) {
    try {
      Map<String, Object> map = new HashMap<String, Object>();
      Iterator iter = data.keySet().iterator();
      while (iter.hasNext()) {
        String key = (String) iter.next();
        Object value = data.get(key);
        map.put(key, value);
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
   */
  public void setExpirationTimeInterval(long timeInterval) {
    this.expirationTimeInterval = timeInterval;
  }

  /**
   * Sets the message that will be shown in the notification. This will overwrite any data specified
   * in AVPush.setData(JSONObject).
   */
  public void setMessage(String message) {
    pushData.clear();
    Map<String, Object> map = AVUtils.createStringObjectMap("alert", message);
    pushData.put("data", map);
  }

  public void setPushToAndroid(boolean pushToAndroid) {
    if (pushToAndroid) {
      this.pushTarget.add("android");
    } else {
      this.pushTarget.remove("android");
    }
  }

  public void setPushToIOS(boolean pushToIOS) {
    if (pushToIOS) {
      this.pushTarget.add("ios");
    } else {
      this.pushTarget.remove("ios");
    }
  }

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
  public void setQuery(AVQuery<? extends AVInstallation> query) {
    this.pushQuery = query;
  }

  /**
   * 可以通过 cql来针对push进行筛选
   *
   * 请注意cql 的主体应该是_Installation表
   *
   * 请在设置cql的同时，不要设置pushTarget(ios,android,wp)
   *
   * @param cql
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
   */
  public Observable<JSONObject> sendInBackground() {
    try {
      Map<String, Object> map = postDataMap();
      return PushClient.getInstance().sendPushRequest(map);
    } catch (Exception ex) {
      return Observable.error(ex);
    }
  }

  private Map<String, Object> pushChannelsData() {
    return AVUtils.createStringObjectMap("channels", channelSet);
  }

  private Map<String, Object> postDataMap() throws AVException {
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
      for (String k : pushParameters.keySet()) {
        map.put(k, JSON.parse(pushParameters.get(k)));
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
      map.put("expiration_interval", new Long(this.expirationTimeInterval));
    }
    if (this.pushDate != null) {
      map.put("push_time", StringUtil.stringFromDate(pushDate));
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
  public void sendInBackground(SendCallback callback) {
    sendInBackground().subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(JSONObject jsonObject) {
        notification = new AVObject("_Notification");
        notification.resetServerData(jsonObject.getInnerMap());
        if (null != callback) {
          callback.internalDone(null);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        if (null != callback) {
          callback.internalDone(new AVException(throwable));
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
  public static void sendDataInBackground(JSONObject data, AVQuery<? extends AVInstallation> query,
                                          SendCallback callback) {
    AVPush push = new AVPush();
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
   *
   */
  public static Observable<JSONObject> sendDataInBackground(JSONObject data, AVQuery<? extends AVInstallation> query) {
    AVPush push = new AVPush();
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
   */
  public static Observable<JSONObject> sendMessageInBackground(String message, AVQuery<? extends AVInstallation> query) {
    AVPush push = new AVPush();
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
                                             AVQuery<? extends AVInstallation> query, SendCallback callback) {
    AVPush push = new AVPush();
    push.setMessage(message);
    push.setQuery(query);
    push.sendInBackground(callback);
  }
}
