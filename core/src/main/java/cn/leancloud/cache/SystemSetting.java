package cn.leancloud.cache;

public interface SystemSetting {
  <T> T getValue(String keyzone, String key, T defaultValue);
  <T> void saveValue(String keyzone, String key, T value);

  void removeKey(String keyzone, String key);
  void removeKeyZone(String keyzone);
}
