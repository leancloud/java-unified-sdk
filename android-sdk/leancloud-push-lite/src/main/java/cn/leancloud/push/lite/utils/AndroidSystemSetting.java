package cn.leancloud.push.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class AndroidSystemSetting {
  private Context context;
  private static AndroidSystemSetting instance;

  public static AndroidSystemSetting createInstance(Context context) {
    if (null == instance) {
      instance = new AndroidSystemSetting(context);
    }
    return instance;
  }

  public static AndroidSystemSetting getInstance() {
    return instance;
  }

  private AndroidSystemSetting(Context context) {
    this.context = context;
  }

  public boolean getBoolean(String keyZone, String key, boolean defaultValue) {
    if (null == this.context) {
      return defaultValue;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    return setting.getBoolean(key, defaultValue);
  }

  public int getInteger(String keyZone, String key, int defaultValue) {
    if (null == this.context) {
      return defaultValue;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    return setting.getInt(key, defaultValue);
  }

  public float getFloat(String keyZone, String key, float defaultValue) {
    if (null == this.context) {
      return defaultValue;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    return setting.getFloat(key, defaultValue);
  }

  public long getLong(String keyZone, String key, long defaultValue) {
    if (null == this.context) {
      return defaultValue;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    return setting.getLong(key, defaultValue);
  }

  public String getString(String keyZone, String key, String defaultValue) {
    if (null == this.context) {
      return defaultValue;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    return setting.getString(key, defaultValue);
  }

  public Map<String, Object> getAll(String keyZone) {
    if (null == this.context) {
      return null;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    return (Map<String, Object>)setting.getAll();
  }

  public void saveBoolean(String keyZone, String key, boolean value) {
    if (null == this.context) {
      return;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = setting.edit();
    editor.putBoolean(key, value);
    editor.apply();
  }
  public void saveInteger(String keyZone, String key, int value) {
    if (null == this.context) {
      return;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = setting.edit();
    editor.putInt(key, value);
    editor.apply();
  }
  public void saveFloat(String keyZone, String key, float value) {
    if (null == this.context) {
      return;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = setting.edit();
    editor.putFloat(key, value);
    editor.apply();
  }
  public void saveLong(String keyZone, String key, long value) {
    if (null == this.context) {
      return;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = setting.edit();
    editor.putLong(key, value);
    editor.apply();
  }
  public void saveString(String keyZone, String key, String value) {
    if (null == this.context) {
      return;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = setting.edit();
    editor.putString(key, value);
    editor.apply();
  }

  public void removeKey(String keyZone, String key) {
    if (null == this.context) {
      return;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = setting.edit();
    editor.remove(key);
    editor.apply();
  }

  public void removeKeyZone(String keyZone) {
    if (null == this.context) {
      return;
    }
    SharedPreferences setting = this.context.getSharedPreferences(keyZone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = setting.edit();
    editor.clear();
    editor.apply();
  }
}
