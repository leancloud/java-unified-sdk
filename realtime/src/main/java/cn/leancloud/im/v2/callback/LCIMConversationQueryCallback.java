package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMException;

import java.util.List;

/**
 * 从 IMClient 查询 IMConversation 时的回调抽象类
 */
public abstract class LCIMConversationQueryCallback
        extends LCCallback<List<LCIMConversation>> {

  public abstract void done(List<LCIMConversation> conversations, LCIMException e);

  @Override
  protected final void internalDone0(List<LCIMConversation> returnValue, LCException e) {
    done(returnValue, LCIMException.wrapperException(e));
  }

}
