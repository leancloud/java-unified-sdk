package cn.leancloud.callback;

import cn.leancloud.LCException;

import java.io.InputStream;

public abstract class GetDataStreamCallback extends LCCallback<InputStream> {
  public abstract void done(InputStream data, LCException e);

  protected final void internalDone0(InputStream returnValue, LCException e) {
    done(returnValue, e);
  }
}
