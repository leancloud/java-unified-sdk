package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMException;

public abstract class AVIMClientCallback extends AVCallback<AVIMClient> {
  public abstract void done(AVIMClient client, AVIMException e);

  @Override
  protected void internalDone0(AVIMClient client, AVException avException) {
    done(client, AVIMException.wrapperAVException(avException));
  }
}
