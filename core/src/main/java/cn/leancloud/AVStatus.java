package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.core.PaasClient;
import cn.leancloud.ops.Utils;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.ErrorUtils;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import io.reactivex.Observable;

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
@AVClassName("_Status")
@JSONType(ignores = {"ACL", "updatedAt"})
public class AVStatus extends AVObject {
  public final static String CLASS_NAME = "_Status";

  public static final String ATTR_MESSAGE_ID = "messageId";
  public static final String ATTR_INBOX_TYPE = "inboxType";
  public static final String ATTR_SOURCE = "source";
  public static final String ATTR_OWNER = "owner";
  public static final String ATTR_IMAGE = "image";
  public static final String ATTR_MESSAGE = "message";

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

  public static AVStatus createStatus(String imageUrl, String message) {
    AVStatus status = new AVStatus();
    status.setImageUrl(imageUrl);
    status.setMessage(message);
    return status;
  }

  public static AVStatus createStatusWithData(Map<String, Object> data) {
    AVStatus status = new AVStatus();
    status.resetServerData(data);
    return status;
  }

  public AVStatus() {
    super(CLASS_NAME);
    this.totallyOverwrite = true;
    this.endpointClassName = "statuses";
  }

  public void setImageUrl(final String imageUrl) {
    put(ATTR_IMAGE, imageUrl);
  }

  public String getImageUrl() {
    return getString(ATTR_IMAGE);
  }

  public void setMessage(String msg) {
    put(ATTR_MESSAGE, msg);
  }

  public String getMessage() {
    return getString(ATTR_MESSAGE);
  }

  /**
   * 此状态在用户Inbox中的ID
   *
   * @warning 仅用于分片查询,不具有唯一性
   */
  public long getMessageId() {
    return getLong(ATTR_MESSAGE_ID);
  }

  protected void setMessageId(long messageId) {
    put(ATTR_MESSAGE_ID, messageId);
  }

  /**
   * 到达收件箱类型, 默认是`default`,私信是`private`, 可以自定义任何类型
   */
  public String getInboxType() {
    return getString(ATTR_INBOX_TYPE);
  }

  /**
   * 获取Status的发送者
   *
   * @return
   */
  public AVUser getSource() {
    return (AVUser) getAVObject(ATTR_SOURCE);
  }

  public void setSource(AVObject source) {
    put(ATTR_SOURCE, Utils.mapFromAVObject(source, false));
  }

  public void setInboxType(final String type) {
    if (!StringUtil.isEmpty(type)) {
      put(ATTR_INBOX_TYPE, type);
    }
  }

  /**
   * 添加AVStatus中的一对自定义内容
   *
   * @param key
   * @param value
   */
  @Override
  public void put(String key, Object value) {
    this.serverData.put(key, value);
  }

  @Override
  public Object get(String key) {
    return this.serverData.get(key);
  }

  /**
   * 删除AVStatus中的一对自定义内容
   *
   * @param key
   */
  @Override
  public void remove(String key) {
    this.serverData.remove(key);
  }

  @Override
  public Observable<AVNull> deleteInBackground() {
    return deleteStatusInBackground(getObjectId());
  }

  public static Observable<AVNull> deleteStatusInBackground(String statusId) {
    if (!checkCurrentUserAuthenticated()) {
      return Observable.error(ErrorUtils.sessionMissingException());
    }
    if (StringUtil.isEmpty(statusId)) {
      return Observable.error(ErrorUtils.invalidObjectIdException());
    }
    return PaasClient.getStorageClient().deleteStatus(statusId);
  }

  public static Observable<AVNull> deleteInboxStatusInBackground(long messageId, String inboxType, AVUser owner) {
    if (null == owner || StringUtil.isEmpty(owner.getObjectId())) {
      return Observable.error(new AVException(AVException.USER_DOESNOT_EXIST, "Owner can't be null"));
    }
    if (StringUtil.isEmpty(inboxType)) {
      return Observable.error(new IllegalArgumentException("messageId can't be null/empty"));
    }
    String ownerString = JSON.toJSONString(Utils.mapFromPointerObject(owner));
    Map<String, String> params = new HashMap<String, String>();
    params.put(ATTR_MESSAGE_ID, String.valueOf(messageId));
    params.put(ATTR_INBOX_TYPE, inboxType);
    params.put(ATTR_OWNER, ownerString);
    return PaasClient.getStorageClient().deleteInboxStatus(params);
  }

  public static Observable<Integer> getUnreadStatusesCountInBackground(String inboxType) {
    return null;
  }

  public static Observable<AVStatus> getStatusWithIdInBackground(String statusId) {
    return null;
  }

  public Observable<AVStatus> sendToUsersInBackground(AVQuery query) {
    return sendToUsersInBackground(INBOX_TYPE.TIMELINE.toString(), query);
  }

  public Observable<AVStatus> sendToUsersInBackground(String inboxType, AVQuery query) {
    return sendInBackground(inboxType, query);
  }

  public Observable<AVStatus> sendToFollowersInBackgroud() {
    return sendToFollowersInBackgroud(INBOX_TYPE.TIMELINE.toString());
  }

  private AVQuery generateFollowerQuery(String userObjectId) {
    AVUser user = new AVUser();
    user.setObjectId(userObjectId);

    List<String> keys = new ArrayList<>();
    keys.add("follower");

    AVQuery query = new AVQuery("_Follower");
    query.whereEqualTo("user", Utils.mapFromAVObject(user, false));
    query.selectKeys(keys);
    return query;
  }

  public Observable<AVStatus> sendToFollowersInBackgroud(String inboxType) {
    if (!checkCurrentUserAuthenticated()) {
      return Observable.error(new IllegalStateException("Current User isn't authenticated, please login at first."));
    }
    // TODO: need to confirm.
    AVQuery followerQuery = generateFollowerQuery(AVUser.currentUser().getObjectId());
    return sendInBackground(inboxType, followerQuery);
  }

  public Observable<AVStatus> sendPrivatelyInBackgroud(final String receiverObjectId) {
    AVQuery userQuery = AVUser.getQuery();
    userQuery.whereEqualTo(AVObject.KEY_OBJECT_ID, receiverObjectId);
    return sendInBackground(INBOX_TYPE.PRIVATE.toString(), userQuery);
  }

  private Observable<AVStatus> sendInBackground(String inboxType, AVQuery query) {
    if (!checkCurrentUserAuthenticated()) {
      return Observable.error(new IllegalStateException("Current User isn't authenticated, please login at first."));
    }
    setSource(AVUser.currentUser());

    Map<String, Object> param = new HashMap<>();
    param.put("data", this.serverData);
    param.put("inboxType", inboxType);

    Map<String, Object> queryCondition = query.assembleJsonParam();
    param.put("query", queryCondition);
    return PaasClient.getStorageClient().postStatus(param);
  }

  /**
   * query statuses sent by User owner.
   *
   * @param source
   * @return
   * @throws AVException
   */
  public static AVStatusQuery statusQuery(AVUser source) throws AVException {
    AVStatusQuery query = new AVStatusQuery();
    query.whereEqualTo(ATTR_SOURCE, source);
    return query;
  }

  /**
   * query statuses send to User owner and with inboxType
   *
   * @param owner
   * @param inboxType
   * @return
   */
  public static AVStatusQuery inboxQuery(AVUser owner, String inboxType) {
    AVStatusQuery query = new AVStatusQuery();
    query.setInboxType(inboxType);
    query.setOwner(owner);
    return query;
  }

  public AVObject toObject() {
    return AVObject.createWithoutData(CLASS_NAME, this.objectId);
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
    AVStatus other = (AVStatus) obj;
    if (!objectId.equals(other.objectId)) {
      return false;
    }
    return true;
  }

  private static boolean checkCurrentUserAuthenticated() {
    AVUser currentUser = AVUser.getCurrentUser();
    if (null != currentUser && currentUser.isAuthenticated()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void add(String key, Object value) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public AVACL getACL() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void setACL(AVACL acl) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void addAll(String key, Collection<?> values) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void addAllUnique(String key, Collection<?> values) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void addUnique(String key, Object value) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public AVObject fetch() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public AVObject fetch(String includedKeys) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void refresh() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void refresh(String includedKeys) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public AVObject fetchIfNeeded() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<AVObject> fetchIfNeededInBackground() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<AVObject> fetchInBackground() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<AVObject> refreshInBackground() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<AVObject> fetchInBackground(String includeKeys) {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void save() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public Observable<? extends AVObject> saveInBackground() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void saveEventually() {
    throw new UnsupportedOperationException();
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public boolean isFetchWhenSave() {
    return false;
  }

  /**
   * 此方法并没有实现，调用会抛出UnsupportedOperationException
   */
  @Deprecated
  @Override
  public void setFetchWhenSave(boolean fetchWhenSave) {
    throw new UnsupportedOperationException();
  }
}
