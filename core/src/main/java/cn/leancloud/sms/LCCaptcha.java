package cn.leancloud.sms;


import cn.leancloud.core.PaasClient;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;

public class LCCaptcha {
  public static Observable<LCCaptchaDigest> requestCaptchaInBackground(LCCaptchaOption option) {
    if (null == option) {
      throw new IllegalArgumentException("option is null");
    }
    return PaasClient.getStorageClient().requestCaptcha(option);
  }

  public static Observable<LCCaptchaValidateResult> verifyCaptchaCodeInBackground(String captchaCode,
                                                                                  LCCaptchaDigest captchaDigest) {
    if (StringUtil.isEmpty(captchaCode)) {
      throw new IllegalArgumentException("captcha code is empty");
    }
    if (null == captchaDigest) {
      throw new IllegalArgumentException("captcha digest is null");
    }
    return PaasClient.getStorageClient().verifyCaptcha(captchaCode, captchaDigest.getCaptchaToken());
  }
}
