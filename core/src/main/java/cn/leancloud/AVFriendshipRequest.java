package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.utils.ErrorUtils;
import io.reactivex.Observable;

@AVClassName("_FriendshipRequest")
public class AVFriendshipRequest extends AVObject {
  public static final String CLASS_NAME = "_FriendshipRequest";
  public static final String ATTR_FRIEND = "friend";
  public static final String ATTR_USER = "user";
  public static final String ATTR_STATUS = "status";

  public static final int STATUS_PENDING = 0x01;
  public static final int STATUS_ACCEPTED = 0x02;
  public static final int STATUS_DECLINED = 0x04;

  enum RequestStatus {
    Pending, Accepted, Declined
  }

  public void setFriend(AVUser user) {
    put(ATTR_FRIEND, user);
  }

  public AVUser getFriend() {
    return getAVObject(ATTR_FRIEND);
  }

  public AVUser setSourceUser() {
    return getAVObject(ATTR_USER);
  }

  public void setSourceUser(AVUser user) {
    put(ATTR_USER, user);
  }

  public AVFriendshipRequest() {
    super(CLASS_NAME);
  }

  public AVFriendshipRequest(AVFriendshipRequest other) {
    super(other);
  }

  public static AVFriendshipRequest createWithCurrentUser() {
    AVUser currentUser = AVUser.currentUser();
    if (null == currentUser) {
      logger.d("current user is null.");
      return null;
    }
    currentUser.applyFriendshipInBackground(null, null);
    AVFriendshipRequest request = new AVFriendshipRequest();
    request.put(ATTR_USER, currentUser);
    return request;
  }

  /**
   * accept friend request by current user.
   */
  public Observable<? extends AVObject> accept() {
    AVUser currentUser = AVUser.currentUser();
    if (null == currentUser) {
      return null;
    }
    return currentUser.acceptFriendshipRequest(this, null);
  }

  /**
   * decline friend request by current user.
   * @return
   */
  public Observable<? extends AVObject> decline() {
    AVUser currentUser = AVUser.currentUser();
    if (null == currentUser) {
      return null;
    }
    return currentUser.declineFriendshipRequest(this);
  }

  @Override
  public Observable<? extends AVObject> saveInBackground(final AVSaveOption option) {
    return Observable.error(ErrorUtils.propagateException(AVException.OPERATION_FORBIDDEN,
            "save operation isn't allowed in AVFriendshipRequest class."));
  }
}
