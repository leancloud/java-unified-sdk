package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.query.LCCloudQueryResult;

public abstract class CloudQueryCallback<T extends LCCloudQueryResult>
        extends LCCallback<LCCloudQueryResult> {
  public abstract void done(LCCloudQueryResult result, LCException LCException);

  @Override
  protected final void internalDone0(LCCloudQueryResult returnValue, LCException e) {
    done(returnValue, e);
  }
}
