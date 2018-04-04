package cn.leancloud.callback;

import cn.leancloud.AVException;
import cn.leancloud.query.AVCloudQueryResult;

public abstract class CloudQueryCallback<T extends AVCloudQueryResult>
        extends AVCallback<AVCloudQueryResult> {
  public abstract void done(AVCloudQueryResult result, AVException avException);

  @Override
  protected final void internalDone0(AVCloudQueryResult returnValue, AVException e) {
    done(returnValue, e);
  }
}
