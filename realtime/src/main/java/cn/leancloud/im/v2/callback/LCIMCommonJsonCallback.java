package cn.leancloud.im.v2.callback;

import cn.leancloud.LCException;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.LCIMException;

import java.util.Map;

public abstract class LCIMCommonJsonCallback extends LCCallback<Map<String, Object>> {
  public abstract void done(Map<String, Object> result, LCIMException e);

  @Override
  protected void internalDone0(Map<String, Object> result, LCException LCException) {
    done(result, LCIMException.wrapperException(LCException));
  }
}
