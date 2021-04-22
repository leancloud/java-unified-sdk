package cn.leancloud.im;

import cn.leancloud.session.SessionCacheHelper;

public class SignatureTask implements Runnable {
  private final SignatureCallback callback;
  private final String clientId;
  public SignatureTask(SignatureCallback callback, String clientId) {
    this.callback = callback;
    this.clientId = clientId;
  }
  public void run() {
    if (null == this.callback) {
      return;
    }
    try {
      Signature signature;
      if (callback.useSignatureCache()) {
        signature = SessionCacheHelper.SignatureCache.getSessionSignature(this.clientId);
        if (null != signature && !signature.isExpired()) {
          ;
        } else {
          signature = this.callback.computeSignature();
        }
      } else {
        signature = this.callback.computeSignature();
      }
      this.callback.onSignatureReady(signature, null);
      if (callback.cacheSignature()) {
        SessionCacheHelper.SignatureCache.addSessionSignature(this.clientId, signature);
      }
    } catch (SignatureFactory.SignatureException ex) {
      this.callback.onSignatureReady(null, ex);
    }
  }
  public void start() {
    BackgroundThreadpool.getInstance().execute(this);
  }
}
