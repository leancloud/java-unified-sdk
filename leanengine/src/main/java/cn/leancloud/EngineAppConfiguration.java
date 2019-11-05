package cn.leancloud;

import cn.leancloud.core.AppConfiguration;

import java.util.Map;

public class EngineAppConfiguration extends AppConfiguration {
  public static final String SYSTEM_ATTR_APP_ENV = "LEANCLOUD_APP_ENV";
  public static final String SYSTEM_ATTR_APP_PORT = "LEANCLOUD_APP_PORT";
  public static final String SYSTEM_ATTR_ANDX_KEY = "LEANCLOUD_APP_ANDX_KEY";

  private static EngineAppConfiguration instance;

  private String appEnv;
  private int port;
  private String appId;
  private String appKey;
  private Map<String, String> affiliatedKeys;
  private String masterKey;
  private static String userAgent = "";

  public static EngineAppConfiguration instance(String applicationId, String clientKey,
      String masterKey, Map<String, String> affiliatedKeys) {
    synchronized (EngineAppConfiguration.class) {
      if (instance == null) {
        instance = new EngineAppConfiguration();
      }
    }
    instance.setApplicationId(applicationId);
    instance.setClientKey(clientKey);
    instance.setMasterKey(masterKey);
    instance.setAffiliatedKeys(affiliatedKeys);
    instance.setAppEnv(getEnvOrProperty(SYSTEM_ATTR_APP_ENV));
    instance.setPort(Integer.parseInt(getEnvOrProperty(SYSTEM_ATTR_APP_PORT)));
    return instance;
  }

  private EngineAppConfiguration() {

  }

  public static String getEnvOrProperty(String key) {
    String value = System.getenv(key);
    if (value == null) {
      value = System.getProperty(key);
    }

    return value;
  }

  public void setApplicationId(String appId) {
    this.appId = appId;
  }
  public String getApplicationId() {
    return this.appId;
  }

  public void setClientKey(String appKey) {
    this.appKey = appKey;
  }
  public String getClientKey() {
    return this.appKey;
  }

  public void setAffiliatedKeys(Map<String, String> keys) {
    this.affiliatedKeys = keys;
  }
  public Map<String, String> getAffiliatedKeys() {
    return this.affiliatedKeys;
  }

  public void setMasterKey(String masterKey) {
    this.masterKey = masterKey;
  }
  public String getMasterKey() {
    return this.masterKey;
  }

  private void setAppEnv(String appEnv) {
    this.appEnv = appEnv;
  }

  private void setPort(int port) {
    this.port = port;
  }

  public String getAppEnv() {
    return appEnv;
  }

  public int getPort() {
    return port;
  }

  public static String getUserAgent() {
    return userAgent;
  }

}
