package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMException;

public abstract class LCIMConversationCreatedCallback extends LCCallback<LCIMConversation> {
  public abstract void done(LCIMConversation conversation, LCIMException e);

  @Override
  protected final void internalDone0(LCIMConversation returnValue, LCException e) {
    done(returnValue, LCIMException.wrapperAVException(e));
  }
}