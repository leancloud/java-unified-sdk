package cn.leancloud.json;

import cn.leancloud.core.AppConfiguration;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

public abstract class JSONObject implements Map<String, Object>, Cloneable, Serializable {
  public static class Builder {
    public static JSONObject create(Map<String, Object> param) {
      return AppConfiguration.getJsonParser().toJSONObject(param);
    }
  }
  public abstract JSONObject getJSONObject(String key);

  public abstract JSONArray getJSONArray(String key);

  public abstract <T> T getObject(String key, Class<T> clazz);
  public abstract <T> T getObject(String key, Type type);

  public abstract <T> T getObject(String key, TypeReference typeReference);
  public abstract Boolean getBoolean(String key);

  public abstract byte[] getBytes(String key);

  public abstract boolean getBooleanValue(String key);
  public abstract Byte getByte(String key);
  public abstract byte getByteValue(String key);

  public abstract Short getShort(String key);
  public abstract short getShortValue(String key);

  public abstract Integer getInteger(String key);

  public abstract int getIntValue(String key);

  public abstract Long getLong(String key);
  public abstract long getLongValue(String key);
  public abstract Float getFloat(String key);
  public abstract float getFloatValue(String key);
  public abstract Double getDouble(String key);
  public abstract double getDoubleValue(String key);
  public abstract BigDecimal getBigDecimal(String key);
  public abstract BigInteger getBigInteger(String key);
  public abstract String getString(String key);
  public abstract Date getDate(String key);

  public abstract java.sql.Date getSqlDate(String key);
  public abstract Timestamp getTimestamp(String key);

  public abstract JSONObject fluentPut(String key, Object value);

  public abstract void putAll(Map<? extends String, ? extends Object> m);

  public abstract JSONObject fluentPutAll(Map<? extends String, ? extends Object> m);
  public abstract JSONObject fluentClear();
  public abstract Map<String, Object> getInnerMap();

  public abstract <T> T toJavaObject(Class<T> clazz);

  public abstract String toJSONString();
}
