package cn.leancloud.im;

public class AVIMMessageStorage {
  private DatabaseDelegate delegate = null;
  public AVIMMessageStorage(DatabaseDelegate delegate) {
    this.delegate = delegate;
  }

}
