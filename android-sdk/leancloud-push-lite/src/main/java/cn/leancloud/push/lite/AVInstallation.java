package cn.leancloud.push.lite;

import java.util.Map;

public class AVInstallation {
  private String className;
  protected String objectId;
  protected String updatedAt;
  protected String createdAt;
  Map<String, Object> serverData;

  public static AVInstallation getCurrentInstallation() {
    return null;
  }

  public String getInstallationId() {
    return null;
  }

  public String getObjectId() {
    return this.objectId;
  }
  public void setObjectId(String id) {
    this.objectId = id;
  }

  Map<String, Object> getServerData() {
    return serverData;
  }

  void setServerData(Map<String, Object> serverData) {
    this.serverData.clear();
    this.serverData.putAll(serverData);
  }

  void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }
  public String getUpdatedAt() {
    return this.updatedAt;
  }

  /**
   * Internal usesage.You SHOULD NOT invoke this method.
   *
   * @return
   */
  void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
  public String getCreatedAt() {
    return this.createdAt;
  }

  void setClassName(String className) {
    this.className = className;
  }
  public String getClassName() {
    return this.className;
  }

}
