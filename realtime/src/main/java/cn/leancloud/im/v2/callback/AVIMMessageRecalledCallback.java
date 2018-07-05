package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.messages.AVIMRecalledMessage;

public abstract class AVIMMessageRecalledCallback extends AVCallback<AVIMRecalledMessage> {

  public abstract void done(AVIMRecalledMessage recalledMessage, AVException e);

  @Override
  protected void internalDone0(AVIMRecalledMessage avimRecalledMessage, AVException avException) {
    done(avimRecalledMessage, avException);
  }
}
