package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMException;

import java.util.List;

/**
 * 从AVIMClient查询AVIMConversation时的回调抽象类
 */
public abstract class AVIMConversationQueryCallback
        extends AVCallback<List<AVIMConversation>> {

  public abstract void done(List<AVIMConversation> conversations, AVIMException e);

  @Override
  protected final void internalDone0(List<AVIMConversation> returnValue, AVException e) {
    done(returnValue, AVIMException.wrapperAVException(e));
  }

}
