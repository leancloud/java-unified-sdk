package cn.leancloud.cache;

import java.util.Map;

public interface SystemSetting {
  boolean getBoolean(String keyZone, String key, boolean defaultValue);
  int getInteger(String keyZone, String key, int defaultValue);
  float getFloat(String keyZone, String key, float defaultValue);
  long getLong(String keyZone, String key, long defaultValue);
  String getString(String keyZone, String key, String defaultValue);
  Map<String, Object> getAll(String keyZone);

  void saveBoolean(String keyZone, String key, boolean value);
  void saveInteger(String keyZone, String key, int value);
  void saveFloat(String keyZone, String key, float value);
  void saveLong(String keyZone, String key, long value);
  void saveString(String keyZone, String key, String value);

  void removeKey(String keyZone, String key);
  void removeKeyZone(String keyZone);
}
