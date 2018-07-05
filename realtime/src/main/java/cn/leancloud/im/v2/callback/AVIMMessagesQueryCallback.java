package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.AVIMMessage;

import java.util.List;

public abstract class AVIMMessagesQueryCallback extends AVCallback<List<AVIMMessage>> {

  public abstract void done(List<AVIMMessage> messages, AVIMException e);

  @Override
  protected final void internalDone0(List<AVIMMessage> returnValue, AVException e) {
    done(returnValue, AVIMException.wrapperAVException(e));
  }
}
