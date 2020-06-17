package cn.leancloud.fastjson;

import cn.leancloud.json.JSONArray;
import cn.leancloud.json.JSONObject;
import cn.leancloud.json.TypeReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class FastJsonObject extends JSONObject {
  @Override
  public JSONObject getJSONObject(String key) {
    return null;
  }

  @Override
  public JSONArray getJSONArray(String key) {
    return null;
  }

  @Override
  public <T> T getObject(String key, Class<T> clazz) {
    return null;
  }

  @Override
  public <T> T getObject(String key, Type type) {
    return null;
  }

  @Override
  public <T> T getObject(String key, TypeReference typeReference) {
    return null;
  }

  @Override
  public Boolean getBoolean(String key) {
    return null;
  }

  @Override
  public byte[] getBytes(String key) {
    return new byte[0];
  }

  @Override
  public boolean getBooleanValue(String key) {
    return false;
  }

  @Override
  public Byte getByte(String key) {
    return null;
  }

  @Override
  public byte getByteValue(String key) {
    return 0;
  }

  @Override
  public Short getShort(String key) {
    return null;
  }

  @Override
  public short getShortValue(String key) {
    return 0;
  }

  @Override
  public Integer getInteger(String key) {
    return null;
  }

  @Override
  public int getIntValue(String key) {
    return 0;
  }

  @Override
  public Long getLong(String key) {
    return null;
  }

  @Override
  public long getLongValue(String key) {
    return 0;
  }

  @Override
  public Float getFloat(String key) {
    return null;
  }

  @Override
  public float getFloatValue(String key) {
    return 0;
  }

  @Override
  public Double getDouble(String key) {
    return null;
  }

  @Override
  public double getDoubleValue(String key) {
    return 0;
  }

  @Override
  public BigDecimal getBigDecimal(String key) {
    return null;
  }

  @Override
  public BigInteger getBigInteger(String key) {
    return null;
  }

  @Override
  public String getString(String key) {
    return null;
  }

  @Override
  public Date getDate(String key) {
    return null;
  }

  @Override
  public java.sql.Date getSqlDate(String key) {
    return null;
  }

  @Override
  public Timestamp getTimestamp(String key) {
    return null;
  }

  @Override
  public JSONObject fluentPut(String key, Object value) {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean containsKey(Object key) {
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    return false;
  }

  @Override
  public Object get(Object key) {
    return null;
  }

  @Nullable
  @Override
  public Object put(String key, Object value) {
    return null;
  }

  @Override
  public Object remove(Object key) {
    return null;
  }

  @Override
  public void putAll(Map<? extends String, ?> m) {

  }

  @Override
  public void clear() {

  }

  @NotNull
  @Override
  public Set<String> keySet() {
    return null;
  }

  @NotNull
  @Override
  public Collection<Object> values() {
    return null;
  }

  @NotNull
  @Override
  public Set<Entry<String, Object>> entrySet() {
    return null;
  }

  @Override
  public JSONObject fluentPutAll(Map<? extends String, ?> m) {
    return null;
  }

  @Override
  public JSONObject fluentClear() {
    return null;
  }

  @Override
  public Map<String, Object> getInnerMap() {
    return null;
  }

  @Override
  public <T> T toJavaObject(Class<T> clazz) {
    return null;
  }

  @Override
  public String toJSONString() {
    return null;
  }
}
