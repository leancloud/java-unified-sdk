package cn.leancloud;

class AVFellowshipQuery<T extends AVUser> extends AVQuery<T>{
  private String friendshipTag;
  AVFellowshipQuery(String theClassName, Class<T> clazz) {
    super(theClassName, clazz);
  }
  String getFriendshipTag() {
    return this.friendshipTag;
  }
  void setFriendshipTag(String tag) {
    this.friendshipTag = tag;
  }
}
