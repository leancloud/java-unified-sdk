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

import java.util.HashMap;
import java.util.Map;

/**
 * status sample：
 *
{
 "ACL": {
 "*": {
 "read": true,
 "write": false
 }
 },
 "content": [
 {
 "__type": "Pointer",
 "className": "Feed",
 "objectId": "objectid"
 }
 ],
 "inboxType": "draft",
 "type": "postFeedDraft",
 "source": {
 "__type": "Pointer",
 "className": "_User",
 "objectId": "a particular user"
 },
 "contributor": {
 "__type": "Pointer",
 "className": "_User",
 "objectId": "another particular user"
 },
 "objectId": "status object id",
 "createdAt": "2018-04-19T08:43:16.277Z",
 "updatedAt": "2018-04-19T08:43:16.277Z"
}
 *
 */
@AVClassName("_Status")
@JSONType(ignores = {"acl", "updatedAt", "uuid"})
public class AVStatus extends AVObject {
  public static String CLASS_NAME = "_Status";
  public static final String IMAGE_TAG = "image";
  public static final String MESSAGE_TAG = "message";
  public static final String DATAMAP_TAG = "dataMap";

  public AVStatus() {
    super(CLASS_NAME);
  }

  public enum INBOX_TYPE {
    TIMELINE("default"), PRIVATE("private");
    private String type;

    private INBOX_TYPE(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return this.type;
    }
  }

  public void setImageUrl(final String imageUrl) {
    put(IMAGE_TAG, imageUrl);
  }

  public String getImageUrl() {
    return getString(IMAGE_TAG);
  }

  public void setMessage(String msg) {
    put(MESSAGE_TAG, msg);
  }

  public String getMessage() {
    return getString(MESSAGE_TAG);
  }

  @Override
  public Observable<AVNull> deleteInBackground() {
    return deleteStatusInBackground(getObjectId());
  }

  public static Observable<AVNull> deleteStatusInBackground(String statusId) {
    if (!checkUserAuthenticated()) {
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
    params.put("messageId", String.valueOf(messageId));
    params.put("inboxType", inboxType);
    params.put("owner", ownerString);
    return PaasClient.getStorageClient().deleteInboxStatus(params);
  }

  public static AVStatusQuery statusQuery(AVUser owner) throws AVException {
    AVStatusQuery query = new AVStatusQuery();
//    query.setSelfQuery(true);
    query.whereEqualTo("source", owner);
//    query.setExternalQueryPath(AVStatus.STATUS_ENDPOINT);
    return query;
  }

  private static boolean checkUserAuthenticated() {
    AVUser currentUser = AVUser.getCurrentUser();
    if (null != currentUser && currentUser.isAuthenticated()) {
      return true;
    } else {
      return false;
    }
  }
}
