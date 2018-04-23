package cn.leancloud;

import cn.leancloud.core.AppConfiguration;

public class EngineAppConfiguration extends AppConfiguration {

  private static EngineAppConfiguration instance;

  private String appEnv;
  private int port;
  private String appId;
  private String appKey;
  private String masterKey;
  private static String userAgent = "";

  public static EngineAppConfiguration instance(String applicationId, String clientKey,
      String masterKey) {
    synchronized (EngineAppConfiguration.class) {
      if (instance == null) {
        instance = new EngineAppConfiguration();
      }
    }
    instance.setApplicationId(applicationId);
    instance.setClientKey(clientKey);
    instance.setMasterKey(masterKey);
    instance.setAppEnv(getEnvOrProperty("LEANCLOUD_APP_ENV"));
    instance.setPort(Integer.parseInt(getEnvOrProperty("LEANCLOUD_APP_PORT")));
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
