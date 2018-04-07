package cn.leancloud.sms;

import cn.leancloud.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class AVSMSOption {
  private String applicationName;
  private String operation;
  private AVSMS.TYPE type = AVSMS.TYPE.TEXT_SMS;
  private String templateName;
  private String signatureName;
  private Map<String, Object> envMap;
  private String captchaValidateToken;
  private int ttl = 0;

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public void setType(AVSMS.TYPE type) {
    this.type = type;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public void setSignatureName(String signatureName) {
    this.signatureName = signatureName;
  }

  public void setEnvMap(Map<String, Object> envMap) {
    this.envMap = envMap;
  }

  public void setCaptchaValidateToken(String captchaValidateToken) {
    this.captchaValidateToken = captchaValidateToken;
  }

  public void setTtl(int ttl) {
    this.ttl = ttl;
  }

  Map<String, Object> getOptionMap() {
    Map<String, Object> result = new HashMap<String, Object>();
    fillMap("name", applicationName, result);
    fillMap("op", operation, result);
    fillMap("template", templateName, result);
    fillMap("sign", signatureName, result);
    fillMap("ttl", ttl, result);
    fillMap("validate_token", captchaValidateToken, result);
    if (null != type) {
      fillMap("smsType", type.toString(), result);
    }
    if (null != envMap && !envMap.isEmpty()) {
      result.putAll(envMap);
    }
    return result;
  }

  private static Map<String, Object> fillMap(String key, String value, Map<String, Object> map) {
    if (!StringUtil.isEmpty(value)) {
      map.put(key, value);
    }
    return map;
  }

  private static Map<String, Object> fillMap(String key, int value, Map<String, Object> map) {
    if (value > 0) {
      map.put(key, value);
    }
    return map;
  }

}
