package cn.leancloud.sms;

import com.alibaba.fastjson.annotation.JSONField;

public class AVCaptchaValidateResult {
  @JSONField(name= "validate_token")
  private String token;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
