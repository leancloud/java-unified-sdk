package cn.leancloud.sms;

import cn.leancloud.annotation.JsonField;

public class LCCaptchaDigest {
  @JsonField("captcha_token")
  private String captchaToken;

  @JsonField("captcha_url")
  private String captchaUrl;

  public String getCaptchaToken() {
    return captchaToken;
  }

  public void setCaptchaToken(String captchaToken) {
    this.captchaToken = captchaToken;
  }

  public String getCaptchaUrl() {
    return captchaUrl;
  }

  public void setCaptchaUrl(String captchaUrl) {
    this.captchaUrl = captchaUrl;
  }
}
