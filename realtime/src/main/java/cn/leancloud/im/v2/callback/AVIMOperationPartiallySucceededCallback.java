package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.Conversation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AVIMOperationPartiallySucceededCallback extends AVCallback<Map<String, Object>> {
  /**
   * 部分成功结果回调函数
   *
   * @param e                    异常实例，如果 null != e 则表示操作失败，此时不用参考后面的两个参数 successfulClientIds/failures。
   * @param successfulClientIds  操作整体成功（此时 null == e），其中成功的成员 id 列表。
   * @param failures             操作整体成功（此时 null == e），其中部分失败的成员信息列表。
   */
  public abstract void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures);

  @Override
  protected final void internalDone0(Map<String, Object> returnValue, AVException e) {
    if (null != e) {
      done(AVIMException.wrapperAVException(e), null, null);
    } else {
      String[] allowed = (String[]) returnValue.get(Conversation.callbackConvMemberMuted_SUCC);
      ArrayList<AVIMOperationFailure> failed = (ArrayList<AVIMOperationFailure>) returnValue.get(Conversation.callbackConvMemberMuted_FAIL);

      done(null, Arrays.asList(allowed), failed);
    }
  }
}