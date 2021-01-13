package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.utils.ErrorUtils;
import io.reactivex.Observable;

@AVClassName("_Followee")
public class AVFriendship extends AVObject {
  public static final String CLASS_NAME = "_Followee";

  public static final String ATTR_FOLLOWEE = "followee";// user who was followed by other
  public static final String ATTR_FOLLOWER = "follower";// user who followed someone.
  public static final String ATTR_FRIEND_STATUS = "friendStatus";

  public AVFriendship() {
    super(CLASS_NAME);
  }

  public AVUser getFollowee() {
    return getAVObject(ATTR_FOLLOWEE);
  }

  public void setFollowee(AVUser followee) {
    put(ATTR_FOLLOWEE, followee);
  }

  public AVUser getFollower() {
    return getAVObject(ATTR_FOLLOWER);
  }

  public void setFollower(AVUser follower) {
    put(ATTR_FOLLOWER, follower);
  }

  @Override
  public Observable<? extends AVObject> saveInBackground(final AVSaveOption option) {
    AVUser currentUser = AVUser.currentUser();
    if (null == currentUser) {
      return Observable.error(ErrorUtils.propagateException(AVException.SESSION_MISSING,
              "No valid session token, make sure signUp or login has been called."));
    }
    return currentUser.updateFriendship(this);
  }
}
