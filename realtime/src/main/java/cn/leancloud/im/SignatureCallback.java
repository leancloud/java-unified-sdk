package cn.leancloud.im;

import cn.leancloud.AVException;
import cn.leancloud.im.SignatureFactory.SignatureException;

abstract class SignatureCallback {

  public abstract Signature computeSignature() throws SignatureException;

  public abstract void onSignatureReady(Signature sig, AVException e);

  /**
   * 是否使用缓存的signature
   * 仅仅是在v2中得自动重连才会使用
   *
   * @return
   */
  public boolean useSignatureCache() {
    return false;
  }

  /**
   * 是否需要缓存signature
   * 仅仅是在v2的open API中间才会需要去缓存
   *
   * @return
   */
  public boolean cacheSignature() {
    return false;
  }
}

