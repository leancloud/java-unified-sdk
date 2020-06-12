package cn.leancloud.json;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class JSONObject implements Map<String, Object>, Cloneable, Serializable {
  private com.alibaba.fastjson.JSONObject fastObject;
  public JSONObject(com.alibaba.fastjson.JSONObject object) {
    this.fastObject = object;
  }

  public JSONObject(Map<String, Object> map) {
    this.fastObject = new com.alibaba.fastjson.JSONObject(map);
  }

  public JSONObject() {
    this.fastObject = new com.alibaba.fastjson.JSONObject();
  }

  protected com.alibaba.fastjson.JSONObject getRawObject() {
    return this.fastObject;
  }

  public int size() {
    return this.fastObject.size();
  }

  public boolean isEmpty() {
    return this.fastObject.isEmpty();
  }

  public boolean containsKey(Object key) {
    return this.fastObject.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return this.fastObject.containsValue(value);
  }

  public Object get(Object key) {
    return this.fastObject.get(key);
  }
  public JSONObject getJSONObject(String key) {
    return new JSONObject(this.fastObject.getJSONObject(key));
  }

  public JSONArray getJSONArray(String key) {
    return new JSONArray(this.fastObject.getJSONArray(key));
  }

  public <T> T getObject(String key, Class<T> clazz) {
    return this.fastObject.getObject(key, clazz);
  }

  public <T> T getObject(String key, Type type) {
    return fastObject.getObject(key, type);
  }

  public <T> T getObject(String key, TypeReference typeReference) {
    return fastObject.getObject(key, null == typeReference? null : typeReference.getType());
  }

  public Boolean getBoolean(String key) {
    return fastObject.getBoolean(key);
  }

  public byte[] getBytes(String key) {
    return fastObject.getBytes(key);
  }

  public boolean getBooleanValue(String key) {
    return fastObject.getBooleanValue(key);
  }

  public Byte getByte(String key) {
    return fastObject.getByte(key);
  }

  public byte getByteValue(String key) {
    return fastObject.getByteValue(key);
  }

  public Short getShort(String key) {
    return fastObject.getShort(key);
  }
  public Short getShortValue(String key) {
    return fastObject.getShortValue(key);
  }

  public Integer getInteger(String key) {
    return fastObject.getInteger(key);
  }

  public int getIntValue(String key) {
    return fastObject.getIntValue(key);
  }

  public Long getLong(String key) {
    return fastObject.getLong(key);
  }
  public long getLongValue(String key) {
    return fastObject.getLongValue(key);
  }
  public Float getFloat(String key) {
    return fastObject.getFloat(key);
  }
  public Float getFloatValue(String key) {
    return fastObject.getFloatValue(key);
  }
  public Double getDouble(String key) {
    return fastObject.getDouble(key);
  }
  public Double getDoubleValue(String key) {
    return fastObject.getDoubleValue(key);
  }
  public BigDecimal getBigDecimal(String key) {
    return fastObject.getBigDecimal(key);
  }
  public BigInteger getBigInteger(String key) {
    return fastObject.getBigInteger(key);
  }
  public String getString(String key) {
    return fastObject.getString(key);
  }
  public Date getDate(String key) {
    return fastObject.getDate(key);
  }

  public java.sql.Date getSqlDate(String key) {
    return fastObject.getSqlDate(key);
  }
  public Timestamp getTimestamp(String key) {
    return fastObject.getTimestamp(key);
  }
  public Object put(String key, Object value) {
    return this.fastObject.put(key, value);
  }

  public JSONObject fluentPut(String key, Object value) {
    this.fastObject.fluentPut(key, value);
    return this;
  }

  public void putAll(Map<? extends String, ? extends Object> m) {
    fastObject.putAll(m);
  }

  public JSONObject fluentPutAll(Map<? extends String, ? extends Object> m) {
    this.fastObject.fluentPutAll(m);
    return this;
  }

  public void clear() {
    fastObject.clear();
  }

  public JSONObject fluentClear() {
    fastObject.fluentClear();
    return this;
  }

  public Object remove(Object key) {
    return fastObject.remove(key);
  }

  public Set<String> keySet() {
    return fastObject.keySet();
  }

  public Collection<Object> values() {
    return fastObject.values();
  }
  public Set<Map.Entry<String, Object>> entrySet() {
    return this.fastObject.entrySet();
  }

  @Override
  public Object clone() {
    return new JSONObject((com.alibaba.fastjson.JSONObject)fastObject.clone());
  }

  public Map<String, Object> getInnerMap() {
    return fastObject.getInnerMap();
  }

  public <T> T toJavaObject(Class<T> clazz) {
    return fastObject.toJavaObject(clazz);
  }

  public int hashCode() {
    return fastObject.hashCode();
  }
  public boolean equals(Object obj) {
    if (null == obj) {
      return false;
    }
    if (!(obj instanceof JSONObject)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    return fastObject.equals(((JSONObject)obj).fastObject);
  }

  public String toJSONString() {
    return this.fastObject.toJSONString();
  }
}
