package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMException;

import java.util.List;

public abstract class AVIMOnlineClientsCallback extends AVCallback<List<String>> {
  public abstract void done(List<String> object, AVIMException e);

  @Override
  protected final void internalDone0(List<String> object, AVException error) {
    this.done(object, AVIMException.wrapperAVException(error));
  }
}