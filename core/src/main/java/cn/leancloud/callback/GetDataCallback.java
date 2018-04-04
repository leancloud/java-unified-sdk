package cn.leancloud.callback;

import cn.leancloud.AVException;

public abstract class GetDataCallback extends AVCallback<byte[]> {
  public abstract void done(byte[] data, AVException e);

  protected final void internalDone0(byte[] returnValue, AVException e) {
    done(returnValue, e);
  }
}