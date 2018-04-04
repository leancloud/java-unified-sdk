package cn.leancloud.callback;

import cn.leancloud.AVException;

import java.io.InputStream;

public abstract class GetDataStreamCallback extends AVCallback<InputStream> {
  public abstract void done(InputStream data, AVException e);

  protected final void internalDone0(InputStream returnValue, AVException e) {
    done(returnValue, e);
  }
}
