package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMClient;

public abstract class AVIMClientStatusCallback extends AVCallback<AVIMClient.AVIMClientStatus> {
  public abstract void done(AVIMClient.AVIMClientStatus client);

  @Override
  protected void internalDone0(AVIMClient.AVIMClientStatus status, AVException avException) {
    done(status);
  }
}
