package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.Conversation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class LCIMOperationPartiallySucceededCallback extends LCCallback<Map<String, Object>> {
  /**
   * 部分成功结果回调函数
   *
   * @param e                    异常实例，如果 null != e 则表示操作失败，此时不用参考后面的两个参数 successfulClientIds/failures。
   * @param successfulClientIds  操作整体成功（此时 null == e），其中成功的成员 id 列表。
   * @param failures             操作整体成功（此时 null == e），其中部分失败的成员信息列表。
   */
  public abstract void done(LCIMException e, List<String> successfulClientIds, List<LCIMOperationFailure> failures);

  @Override
  protected final void internalDone0(Map<String, Object> returnValue, LCException e) {
    if (null != e) {
      done(LCIMException.wrapperException(e), null, null);
    } else if (null != returnValue){
      String[] allowed = (String[]) returnValue.get(Conversation.callbackConvMemberPartial_SUCC);
      ArrayList<LCIMOperationFailure> failed = (ArrayList<LCIMOperationFailure>) returnValue.get(Conversation.callbackConvMemberPartial_FAIL);

      done(null, Arrays.asList(allowed), failed);
    } else {
      done(null, null, null);
    }
  }
}