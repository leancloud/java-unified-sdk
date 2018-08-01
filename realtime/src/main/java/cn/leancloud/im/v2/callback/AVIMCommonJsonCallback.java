package cn.leancloud.im.v2.callback;

import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.AVIMException;

import java.util.Map;

public abstract class AVIMCommonJsonCallback extends AVCallback<Map<String, Object>>{
  public abstract void done(Map<String, Object> result, AVIMException e);

  @Override
  protected void internalDone0(Map<String, Object> result, AVException avException) {
    done(result, AVIMException.wrapperAVException(avException));
  }
}
