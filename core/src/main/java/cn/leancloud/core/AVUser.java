package cn.leancloud.core;

public class AVUser extends AVObject {
  public AVUser() {
    super("_User");
  }

  public static AVUser currentUser() {
    return null;
  }
}
