package cn.leancloud.sms;

import com.alibaba.fastjson.annotation.JSONField;

public class AVCaptchaDigest {
  @JSONField(name = "captcha_token")
  private String captchaToken;

  @JSONField(name = "captcha_url")
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
