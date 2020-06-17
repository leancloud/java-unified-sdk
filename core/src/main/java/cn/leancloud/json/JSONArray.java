package cn.leancloud.json;

import cn.leancloud.core.AppConfiguration;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

public abstract class JSONArray implements List<Object>, Cloneable, Serializable {
  public static class Builder {
    public static JSONArray create(List<Object> param) {
      return AppConfiguration.getJsonParser().toJSONArray(param);
    }
  }

  public abstract JSONArray fluentAdd(Object a);
  public abstract JSONArray fluentRemove(Object o);
  public abstract JSONArray fluentAddAll(Collection<? extends Object> c);
  public abstract JSONArray fluentAddAll(int index, Collection<? extends Object> c);
  public abstract JSONArray fluentRemoveAll(Collection<?> c);
  public abstract JSONArray fluentRetainAll(Collection<?> c);
  public abstract JSONArray fluentClear();
  public abstract JSONArray fluentRemove(int index);
  public abstract JSONArray fluentSet(int index, Object element);
  public abstract JSONArray fluentAdd(int index, Object element);

  public abstract JSONObject getJSONObject(int index);

  public abstract JSONArray getJSONArray(int index);

  public abstract <T> T getObject(int index, Class<T> clazz);

  public abstract <T> T getObject(int index, Type type);

  public abstract Boolean getBoolean(int index);

  public abstract boolean getBooleanValue(int index);

  public abstract Byte getByte(int index);

  public abstract byte getByteValue(int index);

  public abstract Short getShort(int index);

  public abstract short getShortValue(int index);

  public abstract Integer getInteger(int index);

  public abstract int getIntValue(int index);

  public abstract Long getLong(int index);

  public abstract long getLongValue(int index);

  public abstract Float getFloat(int index);
  public abstract float getFloatValue(int index);

  public abstract Double getDouble(int index);

  public abstract double getDoubleValue(int index);

  public abstract BigDecimal getBigDecimal(int index);

  public abstract BigInteger getBigInteger(int index);

  public abstract String getString(int index);

  public abstract Date getDate(int index);

  public abstract java.sql.Date getSqlDate(int index);

  public abstract Timestamp getTimestamp(int index);

  public abstract <T> List<T> toJavaList(Class<T> clazz);
  public abstract String toJSONString();
}
