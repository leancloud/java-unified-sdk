package cn.leancloud.cache;

import java.util.HashMap;
import java.util.Map;

public class InMemorySetting implements SystemSetting {
  private Map<String, Object> dataMap = new HashMap<>();

  public boolean getBoolean(String keyZone, String key, boolean defaultValue) {
    if (dataMap.containsKey(key)) {
      return (boolean)dataMap.get(key);
    } else {
      return defaultValue;
    }
  }
  public int getInteger(String keyZone, String key, int defaultValue) {
    if (dataMap.containsKey(key)) {
      return (int)dataMap.get(key);
    } else {
      return defaultValue;
    }
  }
  public float getFloat(String keyZone, String key, float defaultValue) {
    if (dataMap.containsKey(key)) {
      return (float)dataMap.get(key);
    } else {
      return defaultValue;
    }
  }
  public long getLong(String keyZone, String key, long defaultValue) {
    if (dataMap.containsKey(key)) {
      return (long)dataMap.get(key);
    } else {
      return defaultValue;
    }
  }
  public String getString(String keyZone, String key, String defaultValue) {
    if (dataMap.containsKey(key)) {
      return (String)dataMap.get(key);
    } else {
      return defaultValue;
    }
  }

  public void saveBoolean(String keyZone, String key, boolean value) {
    this.dataMap.put(key, value);
  }
  public void saveInteger(String keyZone, String key, int value) {
    this.dataMap.put(key, value);
  }
  public void saveFloat(String keyZone, String key, float value) {
    this.dataMap.put(key, value);
  }
  public void saveLong(String keyZone, String key, long value) {
    this.dataMap.put(key, value);
  }
  public void saveString(String keyZone, String key, String value) {
    this.dataMap.put(key, value);
  }

  public void removeKey(String keyZone, String key) {
    this.dataMap.remove(key);
  }
  public void removeKeyZone(String keyZone) {
    //
  }
}
