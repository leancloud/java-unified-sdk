package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMMessage;

public abstract class AVIMMessageUpdatedCallback extends AVCallback<AVIMMessage> {

  public abstract void done(AVIMMessage message, AVException e);

  @Override
  protected void internalDone0(AVIMMessage message, AVException avException) {
    done(message, avException);
  }
}