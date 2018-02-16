package cn.leancloud.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AVFile {
  private String name = "";
  private Map<String, Object> metaData = new ConcurrentHashMap<String, Object>();

  public AVFile() {
  }

  public static String getClassName() {
    return "_File";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getMetaData() {
    return metaData;
  }

  public void setMetaData(Map<String, Object> metaData) {
    this.metaData = metaData;
  }
}
