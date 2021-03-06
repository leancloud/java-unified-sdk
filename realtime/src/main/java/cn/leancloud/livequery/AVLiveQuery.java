package cn.leancloud.livequery;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import cn.leancloud.codec.MDFive;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.ops.Utils;
import cn.leancloud.service.RealtimeClient;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONArray;
import cn.leancloud.json.JSONObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.*;

public class AVLiveQuery {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVLiveQuery.class);
  private static final String SP_LIVEQUERY_KEY = "livequery_keyzone";
  private static final String SP_SUBSCRIBE_ID = "subscribeId";
  private static final String QUERY_ID = "query_id";
  private static final String SESSION_TOKEN = "sessionToken";
  private static final String QUERY = "query";
  private static final String OBJECT = "object";
  private static final String OP = "op";
  private static final String UPDATE_KEYS = "updatedKeys";

  public static final String SUBSCRIBE_ID = "id";
  public static final String LIVEQUERY_PRIFIX = "live_query_";
  public static final String ACTION_LIVE_QUERY_LOGIN = "action_live_query_login";

  private static final LiveQueryConnectionListener liveQueryConnectionListener = new LiveQueryConnectionListener();

  static {
    AVConnectionManager.getInstance().subscribeDefaultConnectionListener(LiveQueryOperationDelegate.LIVEQUERY_DEFAULT_ID,
            liveQueryConnectionListener);
  }

  public enum EventType {
    CREATE("create"), UPDATE("update"), ENTER("enter"), LEAVE("leave"), DELETE("delete"), LOGIN("login"), UNKONWN("unknown");

    private String event;

    public static EventType getType(String event) {
      if (CREATE.getContent().equals(event)) {
        return CREATE;
      } else if (UPDATE.getContent().equals(event)) {
        return UPDATE;
      } else if (ENTER.getContent().equals(event)) {
        return ENTER;
      } else if (LEAVE.getContent().equals(event)) {
        return LEAVE;
      } else if (DELETE.getContent().equals(event)) {
        return DELETE;
      } else if (LOGIN.getContent().equals(event)) {
        return LOGIN;
      }
      return UNKONWN;
    }

    EventType(String event) {
      this.event = event;
    }

    public String getContent() {
      return event;
    }
  }

  public static void processData(ArrayList<String> dataList) {
    for (final String data : dataList) {
      try {
        JSONObject jsonObject = JSON.parseObject(data);
        String op = jsonObject.getString(OP);
        String queryId = jsonObject.getString(QUERY_ID);
        JSONObject object = jsonObject.getJSONObject(OBJECT);
        if (!StringUtil.isEmpty(queryId)) {
          ArrayList<String> updateKeyList = new ArrayList<String>();
          if (jsonObject.containsKey(UPDATE_KEYS)) {
            JSONArray jsonArray = jsonObject.getJSONArray(UPDATE_KEYS);
            for (Object item : jsonArray.toArray()) {
              updateKeyList.add((String)item);
            }
          }

          for (AVLiveQuery liveQuery : liveQuerySet) {
            if (queryId.equals(liveQuery.queryId) && null != liveQuery.eventHandler) {
              liveQuery.eventHandler.done(EventType.getType(op), Utils.parseObjectFromMap(object), updateKeyList);
            }
          }
        }
      } catch (Exception e) {
        LOGGER.e("Parsing json data error, ", e);
      }
    };
  }

  private static Set<AVLiveQuery> liveQuerySet = Collections.synchronizedSet(new HashSet<AVLiveQuery>());

  static void resumeSubscribeers() {
    for (AVLiveQuery query: liveQuerySet) {
      query.subscribeInBackground(null);
    }
  }

  private static String subscribeId;
  private String queryId;
  private AVQuery query;
  private AVLiveQueryEventHandler eventHandler;

  private AVLiveQuery(AVQuery query) {
    this.query = query;
  }
  /**
   * initialize AVLiveQuery with AVQuery
   * @param query query instance.
   * @return live query instance.
   */
  public static AVLiveQuery initWithQuery(AVQuery query) {
    if (null == query) {
      throw new IllegalArgumentException("query cannot be null");
    }
    return new AVLiveQuery(query);
  }

  /**
   * subscribe the query
   * @param callback callback function.
   */
  public void subscribeInBackground(final AVLiveQuerySubscribeCallback callback) {
    Map<String, String> params = query.assembleParameters();
    params.put("className", query.getClassName());

    final Map<String, Object> dataMap = new HashMap<>();
    dataMap.put(QUERY, params);
    String session = getSessionToken();
    if (!StringUtil.isEmpty(session)) {
      dataMap.put(SESSION_TOKEN, session);
    }

    dataMap.put(SUBSCRIBE_ID, getSubscribeId());

    if (liveQueryConnectionListener.connectionIsOpen()) {
      subscribeThroughRESTAPI(dataMap, callback);
    } else {
      loginLiveQuery(new AVLiveQuerySubscribeCallback() {
        @Override
        public void done(AVException e) {
          if (null != e) {
            if (null != callback) {
              callback.internalDone(e);
            }
            return;
          } else {
            subscribeThroughRESTAPI(dataMap, callback);
          }
        }
      });
    }
  }

  private void subscribeThroughRESTAPI(final Map<String, Object> dataMap, final AVLiveQuerySubscribeCallback callback) {
    RealtimeClient.getInstance().subscribeLiveQuery(dataMap).subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(JSONObject jsonObject) {
        if (null != jsonObject && jsonObject.containsKey(QUERY_ID)) {
          queryId = jsonObject.getString(QUERY_ID);
          liveQuerySet.add(AVLiveQuery.this);
          if (null != callback) {
            callback.internalDone(null);
          }
          return;
        }
        if (null != callback) {
          callback.internalDone(new AVException(AVException.UNKNOWN, "response isn't recognized"));
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
   * set connection handler globally.
   * @param connectionHandler connection handler.
   */
  public static void setConnectionHandler(AVLiveQueryConnectionHandler connectionHandler) {
    AVLiveQuery.liveQueryConnectionListener.setConnectionHandler(connectionHandler);
  }

  public void setEventHandler(AVLiveQueryEventHandler eventHandler) {
    if (null == eventHandler) {
      throw new IllegalArgumentException("eventHandler can not be null.");
    }
    this.eventHandler = eventHandler;
  }

  private void loginLiveQuery(final AVLiveQuerySubscribeCallback callback) {
    boolean ret = InternalConfiguration.getOperationTube().loginLiveQuery(AVConnectionManager.getInstance(), getSubscribeId(), callback);
    if (!ret && null != callback) {
      callback.internalDone(new AVException(AVException.OPERATION_FORBIDDEN, "can't invoke operation in background."));
    }
  }

  /**
   * unsubscribe the query
   * @param callback callback function.
   */
  public void unsubscribeInBackground(final AVLiveQuerySubscribeCallback callback) {
    Map<String, Object> map = new HashMap<>();
    map.put(SUBSCRIBE_ID, getSubscribeId());
    map.put(QUERY_ID, queryId);

    RealtimeClient.getInstance().unsubscribeLiveQuery(map).subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(JSONObject jsonObject) {
        liveQuerySet.remove(AVLiveQuery.this);
        queryId = "";
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

  private static String getSubscribeId() {
    if (StringUtil.isEmpty(subscribeId)) {
      subscribeId = AppConfiguration.getDefaultSetting().getString(SP_LIVEQUERY_KEY,SP_SUBSCRIBE_ID, "");
      if (StringUtil.isEmpty(subscribeId)) {
        String packageName = AppConfiguration.getApplicationPackageName();
        String additionalStr = UUID.randomUUID().toString();
        subscribeId = MDFive.computeMD5(packageName + additionalStr);
        AppConfiguration.getDefaultSetting().saveString(SP_LIVEQUERY_KEY, SP_SUBSCRIBE_ID, subscribeId);
      }
    }
    return subscribeId;
  }

  private String getSessionToken() {
    AVUser currentUser = AVUser.getCurrentUser();
    if (null != currentUser) {
      return currentUser.getSessionToken();
    }
    return "";
  }

}
