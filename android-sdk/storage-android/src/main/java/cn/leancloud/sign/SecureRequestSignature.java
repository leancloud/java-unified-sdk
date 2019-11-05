package cn.leancloud.sign;

import cn.leancloud.core.RequestSignature;

public class SecureRequestSignature implements RequestSignature {

  public String generateSign() {
    return NativeSignHelper.generateRequestAuth();
  }
}
