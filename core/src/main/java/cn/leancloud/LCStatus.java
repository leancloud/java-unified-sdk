package cn.leancloud;

import cn.leancloud.annotation.LCClassName;
import cn.leancloud.core.PaasClient;
import cn.leancloud.ops.Utils;
import cn.leancloud.types.LCNull;
import cn.leancloud.utils.ErrorUtils;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import java.util.*;

/**
 * Status 预定义属性：
 *  1. messageId, Integer, message sequence number, Receiver-side only
 *  2. inboxType, String, identifier for multiple purpose, default is 'default' which stands for timeline.
 *  3. source, Pointer, point to source user.
 *  4. owner, Pointer, point to target user, Receiver-side only.
 *
 * status sample：
 *
 *
 */
@LCClassName("_Status")
public class LCStatus extends LCObject {
  public final static String CLASS_NAME = "_Status";

  public static final String ATTR_MESSAGE_ID = "messageId";
  public static final String ATTR_INBOX_TYPE = "inboxType";
  public static final String ATTR_SOURCE = "source";
  public static final String ATTR_OWNER = "owner";
  public static final String ATTR_IMAGE = "image";
  public static final String ATTR_MESSAGE = "message";

  public static int INVALID_MESSAGE_ID = 0;

  public enum INBOX_TYPE {
    TIMELINE("default"), PRIVATE("private");
    private String type;

    INBOX_TYPE(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return this.type;
    }
  }

  /**
   * create a status instance.
   * @param imageUrl image url
   * @param message  message text
   * @return Return an instance of AVStatus
   */
  public static LCStatus createStatus(String imageUrl, String message) {
    LCStatus status = new LCStatus();
    status.setImageUrl(imageUrl);
    status.setMessage(message);
    return status;
  }

  /**
   * create a status instance.
   *
   * @param data map data
   * @return Return an instance of AVStatus
   */
  public static LCStatus createStatusWithData(Map<String, Object> data) {
    LCStatus status = new LCStatus();
    status.resetServerData(data);
    return status;
  }

  /**
   * default constructor.
   *
   */
  public LCStatus() {
    super(CLASS_NAME);
    this.totallyOverwrite = true;
    this.endpointClassName = "statuses";
  }

  /**
   * constructor from AVObject instance.
   * @param o object instance
   */
  public LCStatus(LCObject o) {
    super(o);
  }

  /**
   * set image url attribute.
   * @param imageUrl image url
   */
  public void setImageUrl(final String imageUrl) {
    put(ATTR_IMAGE, imageUrl);
  }

  /**
   * get image url attribute.
   * @return Return the value of image url
   */
  public String getImageUrl() {
    return getString(ATTR_IMAGE);
  }

  /**
   * set message text
   * @param msg the message text.
   */
  public void setMessage(String msg) {
    put(ATTR_MESSAGE, msg);
  }

  /**
   * get message text
   * @return Return the message text.
   */
  public String getMessage() {
    return getString(ATTR_MESSAGE);
  }

  /**
   * 此状态在用户 Inbox 中的 ID
   *
   * 注意: 仅用于分片查询,不具有唯一性
   * @return Return the message id in inbox.
   */
  public long getMessageId() {
    return getLong(ATTR_MESSAGE_ID);
  }

  protected void setMessageId(long messageId) {
    put(ATTR_MESSAGE_ID, messageId);
  }

  /**
   * 到达收件箱类型, 默认是`default`,私信是`private`, 可以自定义任何类型
   * @return inbox type.
   */
  public String getInboxType() {
    return getString(ATTR_INBOX_TYPE);
  }

  /**
   * 获取 Status 的发送者
   *
   * @return source user of the status
   */
  public LCUser getSource() {
    return (LCUser) getAVObject(ATTR_SOURCE);
  }

  /**
   * set source of status
   * @param source source user of the status
   */
  public void setSource(LCObject source) {
    put(ATTR_SOURCE, Utils.mapFromAVObject(source, false));
  }

  /**
   * set inbox type.
   * @param type inbox type
   */
  public void setInboxType(final String type) {
    if (!StringUtil.isEmpty(type)) {
      put(ATTR_INBOX_TYPE, type);
    }
  }

  /**
   * 添加 AVStatus 中的一对自定义内容
   *
   * @param key attribute key
   * @param value attribute value
   */
  @Override
  public void put(String key, Object value) {
    this.serverData.put(key, value);
  }

  /**
   * get customized key value.
   * @param key attribute key
   * @return attribute value
   */
  @Override
  public Object get(String key) {
    return this.serverData.get(key);
  }

  /**
   * 删除 AVStatus 中的一对自定义内容
   *
   * @param key attribute key
   */
  @Override
  public void remove(String key) {
    this.serverData.remove(key);
  }

  /**
   * delete status
   *
   * @return Observable instance
   */
  @Override
  public Observable<LCNull> deleteInBackground() {
    return deleteInBackground(this);
  }

  /**
   * delete status(class method)
   *
   * @param status instance of AVStatus
   * @return Observable instance
   */
  public static Observable<LCNull> deleteInBackground(LCStatus status) {
    return deleteInBackground(LCUser.currentUser(), status);
  }

  /**
   * delete status(class method)
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param status instance of AVStatus
   * @return Observable instance
   *
   * in general, this method should be invoked in lean engine.
   */
  public static Observable<LCNull> deleteInBackground(LCUser asAuthenticatedUser, LCStatus status) {
    if (!checkUserAuthenticated(asAuthenticatedUser)) {
      return Observable.error(ErrorUtils.sessionMissingException());
    }

    String currentUserObjectId = asAuthenticatedUser.getObjectId();

    LCObject source = null;
    Object sourceObject = status.get(ATTR_SOURCE);
    if (sourceObject instanceof LCObject) {
      source = (LCObject) sourceObject;
    } else if (sourceObject instanceof JSONObject) {
      JSONObject sourceJson = (JSONObject) sourceObject;
      source = LCObject.createWithoutData(sourceJson.getString(LCObject.KEY_CLASSNAME),
              sourceJson.getString(LCObject.KEY_OBJECT_ID));
    } else if (sourceObject instanceof HashMap) {
      HashMap<String, Object> sourceMap = (HashMap<String, Object>)sourceObject;
      source = LCObject.createWithoutData((String) sourceMap.get(LCObject.KEY_CLASSNAME),
              (String) sourceMap.get(LCObject.KEY_OBJECT_ID));
    }

    String statusObjectId = status.getObjectId();
    long messageId = status.getMessageId();

    if (null != source && currentUserObjectId.equals(source.getString(LCObject.KEY_OBJECT_ID))) {
      if (StringUtil.isEmpty(statusObjectId)) {
        return Observable.error(ErrorUtils.invalidObjectIdException());
      } else {
        return PaasClient.getStorageClient().deleteStatus(asAuthenticatedUser, statusObjectId);
      }
    } else {
      if (INVALID_MESSAGE_ID == messageId) {
        return Observable.error(ErrorUtils.invalidObjectIdException());
      } else {
        String ownerString = JSON.toJSONString(Utils.mapFromAVObject(asAuthenticatedUser, false));
        Map<String, Object> params = new HashMap<>();
        params.put(ATTR_MESSAGE_ID, String.valueOf(messageId));
        params.put(ATTR_INBOX_TYPE, status.getInboxType());
        params.put(ATTR_OWNER, ownerString);
        return PaasClient.getStorageClient().deleteInboxStatus(asAuthenticatedUser, params);
      }
    }
  }

  /**
   * fetch status with specified objectId
   *
   * @param statusId status id.
   * @return Observable instance
   */
  public static Observable<LCStatus> getStatusWithIdInBackground(String statusId) {
    return getStatusWithIdInBackground(null, statusId);
  }

  /**
   * fetch status with specified objectId
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param statusId status id.
   * @return Observable instance
   *
   * in general, this method should be invoked in lean engine.
   */
  public static Observable<LCStatus> getStatusWithIdInBackground(LCUser asAuthenticatedUser, String statusId) {
    return PaasClient.getStorageClient().fetchStatus(asAuthenticatedUser, statusId);
  }

  /**
   * send to user with query.
   *
   * @param query instance of AVQuery
   * @return Observable instance
   */
  public Observable<LCStatus> sendToUsersInBackground(LCQuery query) {
    return sendToUsersInBackground(INBOX_TYPE.TIMELINE.toString(), query);
  }

  /**
   * send to user with query and inboxType.
   * @param inboxType inbox type
   * @param query instance of AVQuery
   * @return Observable instance
   */
  public Observable<LCStatus> sendToUsersInBackground(String inboxType, LCQuery query) {
    return sendInBackground(inboxType, query);
  }

  /**
   * send status to followers.
   * @return Observable instance
   */
  public Observable<LCStatus> sendToFollowersInBackground() {
    return sendToFollowersInBackground(INBOX_TYPE.TIMELINE.toString());
  }

  private LCQuery generateFollowerQuery(String userObjectId) {
    LCUser user = new LCUser();
    user.setObjectId(userObjectId);

    List<String> keys = new ArrayList<>();
    keys.add("follower");

    LCQuery query = new LCQuery("_Follower");
    query.whereEqualTo("user", Utils.mapFromAVObject(user, false));
    query.selectKeys(keys);
    return query;
  }

  /**
   * send status with inboxType to followers.
   * @param inboxType inbox type
   * @return Observable instance
   */
  public Observable<LCStatus> sendToFollowersInBackground(String inboxType) {
    if (!checkCurrentUserAuthenticated()) {
      return Observable.error(ErrorUtils.sessionMissingException());
    }
    LCQuery followerQuery = generateFollowerQuery(LCUser.currentUser().getObjectId());
    return sendInBackground(inboxType, followerQuery);
  }

  /**
   * send privately message.
   *
   * @param receiverObjectId receiver objectId
   * @return Observable instance
   */
  public Observable<LCStatus> sendPrivatelyInBackground(final String receiverObjectId) {
    LCQuery userQuery = LCUser.getQuery();
    userQuery.whereEqualTo(LCObject.KEY_OBJECT_ID, receiverObjectId);
    return sendInBackground(INBOX_TYPE.PRIVATE.toString(), userQuery);
  }

  private Observable<LCStatus> sendInBackground(String inboxType, LCQuery query) {
    return sendInBackground(LCUser.currentUser(), inboxType, query);
  }

  private Observable<LCStatus> sendInBackground(LCUser asAuthenticatedUser, String inboxType, LCQuery query) {
    if (!checkUserAuthenticated(asAuthenticatedUser)) {
      return Observable.error(ErrorUtils.sessionMissingException());
    }
    setSource(asAuthenticatedUser);

    Map<String, Object> param = new HashMap<>();
    param.put("data", this.serverData);
    param.put("inboxType", inboxType);

    Map<String, Object> queryCondition = query.assembleJsonParam();
    param.put("query", queryCondition);
    return PaasClient.getStorageClient().postStatus(asAuthenticatedUser, param).map(new Function<LCStatus, LCStatus>() {
      @Override
      public LCStatus apply(LCStatus avStatus) throws Exception {
        LCStatus.this.mergeRawData(avStatus, true);
        return avStatus;
      }
    });
  }

  /**
   * query statuses sent by User owner.
   * default query direction: from NEW to OLD.
   *
   * @param source source User
   * @return instance of AVStatusQuery
   */
  public static LCStatusQuery statusQuery(LCUser source) {
    LCStatusQuery query = new LCStatusQuery(LCStatusQuery.SourceType.OWNED);
    query.setSource(source);
    query.setDirection(LCStatusQuery.PaginationDirection.NEW_TO_OLD);
    query.setInboxType(INBOX_TYPE.TIMELINE.toString());
    return query;
  }

  /**
   * query statuses send to User owner and with inboxType
   * default query direction: from NEW to OLD.
   *
   * @param owner owner user
   * @param inboxType inbox type
   * @return instance of AVStatusQuery
   */
  public static LCStatusQuery inboxQuery(LCUser owner, String inboxType) {
    LCStatusQuery query = new LCStatusQuery(LCStatusQuery.SourceType.INBOX);
    query.setOwner(owner);
    query.setDirection(LCStatusQuery.PaginationDirection.NEW_TO_OLD);
    query.setInboxType(inboxType);
    return query;
  }

  public LCObject toObject() {
    return LCObject.createWithoutData(CLASS_NAME, this.objectId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    if (StringUtil.isEmpty(this.objectId)) {
      return false;
    }
    LCStatus other = (LCStatus) obj;
    if (!objectId.equals(other.objectId)) {
      return false;
    }
    return true;
  }

  private static boolean checkCurrentUserAuthenticated() {
    LCUser currentUser = LCUser.getCurrentUser();
    return checkUserAuthenticated(currentUser);
  }

  private static boolean checkUserAuthenticated(LCUser currentUser) {
    if (null != currentUser && currentUser.isAuthenticated()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void add(String key, Object value) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public LCACL getACL() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void setACL(LCACL acl) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void addAll(String key, Collection<?> values) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void addAllUnique(String key, Collection<?> values) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void addUnique(String key, Object value) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public LCObject fetch() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public LCObject fetch(String includedKeys) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void refresh() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void refresh(String includedKeys) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public LCObject fetchIfNeeded() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<LCObject> fetchIfNeededInBackground() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<LCObject> fetchInBackground() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<LCObject> refreshInBackground() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<LCObject> fetchInBackground(String includeKeys) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void save() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<? extends LCObject> saveInBackground() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void saveEventually() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public boolean isFetchWhenSave() {
    return false;
  }

  /**
   * 此方法并没有实现，调用会抛出 UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void setFetchWhenSave(boolean fetchWhenSave) {
    throw new UnsupportedOperationException();
  }
}
