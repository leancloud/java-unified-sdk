package cn.leancloud.sms;

public class LCCaptchaValidateResult {
  private String validateToken;

  public String getValidateToken() {
    return validateToken;
  }

  public void setValidateToken(String validateToken) {
    this.validateToken = validateToken;
  }

  public String getToken() {
    return getValidateToken();
  }

  public void setToken(String validateToken) {
    setValidateToken(validateToken);
  }
}
