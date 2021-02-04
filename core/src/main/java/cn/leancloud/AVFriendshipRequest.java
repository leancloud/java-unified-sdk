package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.utils.ErrorUtils;
import io.reactivex.Observable;

import java.util.Map;

@AVClassName("_FriendshipRequest")
public class AVFriendshipRequest extends AVObject {
  public static final String CLASS_NAME = "_FriendshipRequest";
  public static final String ATTR_FRIEND = "friend";
  public static final String ATTR_USER = "user";
  public static final String ATTR_STATUS = "status";

  public static final int STATUS_PENDING = 0x01;
  public static final int STATUS_ACCEPTED = 0x02;
  public static final int STATUS_DECLINED = 0x04;
  public static final int STATUS_ANY = STATUS_PENDING | STATUS_ACCEPTED | STATUS_DECLINED;

  enum RequestStatus {
    Pending, Accepted, Declined
  }

  public AVFriendshipRequest() {
    super(CLASS_NAME);
  }

  public AVFriendshipRequest(AVFriendshipRequest other) {
    super(other);
  }

  public void setFriend(AVUser user) {
    put(ATTR_FRIEND, user);
  }

  public AVUser getFriend() {
    return getAVObject(ATTR_FRIEND);
  }

  public AVUser getSourceUser() {
    return getAVObject(ATTR_USER);
  }

  public void setSourceUser(AVUser user) {
    put(ATTR_USER, user);
  }

  /**
   * accept friend request by current user.
   *
   * @param attributes additional attributes for this friend.
   * @return observable instance
   *
   * notice: attributes is necessary as parameter bcz they are not properties of FriendshipRequest.
   */
  public Observable<? extends AVObject> accept(Map<String, Object> attributes) {
    AVUser currentUser = AVUser.currentUser();
    if (null == currentUser) {
      logger.d("current user is null.");
      return Observable.error(ErrorUtils.propagateException(AVException.SESSION_MISSING,
              "No valid session token, make sure signUp or login has been called."));
    }
    return currentUser.acceptFriendshipRequest(this, attributes);
  }

  /**
   * decline friend request by current user.
   *
   * @return observable instance
   */
  public Observable<? extends AVObject> decline() {
    AVUser currentUser = AVUser.currentUser();
    if (null == currentUser) {
      logger.d("current user is null.");
      return Observable.error(ErrorUtils.propagateException(AVException.SESSION_MISSING,
              "No valid session token, make sure signUp or login has been called."));
    }
    return currentUser.declineFriendshipRequest(this);
  }

  @Override
  public Observable<? extends AVObject> saveInBackground(final AVSaveOption option) {
    return Observable.error(ErrorUtils.propagateException(AVException.OPERATION_FORBIDDEN,
            "save operation isn't allowed in AVFriendshipRequest class."));
  }
}
