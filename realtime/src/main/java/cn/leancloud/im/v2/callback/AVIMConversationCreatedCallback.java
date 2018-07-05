package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMException;

public abstract class AVIMConversationCreatedCallback extends AVCallback<AVIMConversation> {
  public abstract void done(AVIMConversation conversation, AVIMException e);

  @Override
  protected final void internalDone0(AVIMConversation returnValue, AVException e) {
    done(returnValue, AVIMException.wrapperAVException(e));
  }
}