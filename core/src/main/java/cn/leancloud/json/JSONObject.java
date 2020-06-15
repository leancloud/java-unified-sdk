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
  private com.google.gson.JsonObject gsonObject;
  public JSONObject(com.google.gson.JsonObject object) {
    this.gsonObject = object;
  }

  public JSONObject(Map<String, Object> map) {
    this.gsonObject = new com.google.gson.JsonObject();
    if (null != map) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        gsonObject.add(entry.getKey(), ConverterUtils.toJsonElement(entry.getValue()));
      }
    }
  }

  public JSONObject() {
    this.gsonObject = new com.google.gson.JsonObject();
  }

  public com.google.gson.JsonObject getRawObject() {
    return this.gsonObject;
  }

  public int size() {
    return this.gsonObject.size();
  }

  public boolean isEmpty() {
    return this.gsonObject.size() <= 0;
  }

  public boolean containsKey(Object key) {
    return this.gsonObject.has((String)key);
  }

  public boolean containsValue(Object value) {
    return false;//this.gsonObject.containsValue(value);
  }

  public Object get(Object key) {
    com.google.gson.JsonElement element = this.gsonObject.get((String)key);
    return null;
  }

  public JSONObject getJSONObject(String key) {
    if (!gsonObject.has(key)) {
      return null;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonObject()) {
      return null;
    }
    return new JSONObject(element.getAsJsonObject());
  }

  public JSONArray getJSONArray(String key) {
    if (!gsonObject.has(key)) {
      return null;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonArray()) {
      return null;
    }
    return new JSONArray(element.getAsJsonArray());
  }

  public <T> T getObject(String key, Class<T> clazz) {
    if (!gsonObject.has(key)) {
      return null;
    }
    com.google.gson.JsonElement element = this.gsonObject.get(key);
    if (element.isJsonNull()) {
      return null;
    }
    return null;//this.gsonObject.getObject(key, clazz);
  }

  /**
   * get object value with specified key.
   * @param key
   * @param type
   * @param <T>
   * @return
   *
   * @since 1.8
   */
  public <T> T getObject(String key, Type type) {
    try {
      return (T) getObject(key, Class.forName(type.getTypeName()));
    } catch (ClassNotFoundException ex) {
      return null;
    }
  }

  public <T> T getObject(String key, TypeReference typeReference) {
    return getObject(key, null == typeReference? null : typeReference.getType());
  }

  public Boolean getBoolean(String key) {
    if (!gsonObject.has(key)) {
      return false;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return false;
    }
    return element.getAsBoolean();
  }

  public byte[] getBytes(String key) {
    String ret = getString(key);
    if (null == ret) {
      return null;
    }
    return ret.getBytes();
  }

  public boolean getBooleanValue(String key) {
    return getBoolean(key).booleanValue();
  }

  public Byte getByte(String key) {
    if (!gsonObject.has(key)) {
      return 0;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return 0;
    }
    return element.getAsByte();
  }

  public byte getByteValue(String key) {
    return getByte(key).byteValue();
  }

  public Short getShort(String key) {
    if (!gsonObject.has(key)) {
      return 0;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return 0;
    }
    return element.getAsShort();
  }
  public short getShortValue(String key) {
    return getShort(key).shortValue();
  }

  public Integer getInteger(String key) {
    if (!gsonObject.has(key)) {
      return 0;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return 0;
    }
    return element.getAsInt();
  }

  public int getIntValue(String key) {
    return getInteger(key).intValue();
  }

  public Long getLong(String key) {
    if (!gsonObject.has(key)) {
      return 0l;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return 0l;
    }
    return element.getAsLong();
  }
  public long getLongValue(String key) {
    return getLong(key).longValue();
  }
  public Float getFloat(String key) {
    if (!gsonObject.has(key)) {
      return 0f;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return 0f;
    }
    return element.getAsFloat();
  }
  public float getFloatValue(String key) {
    return getFloat(key).floatValue();
  }
  public Double getDouble(String key) {
    if (!gsonObject.has(key)) {
      return 0d;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return 0d;
    }
    return element.getAsDouble();
  }
  public double getDoubleValue(String key) {
    return getDouble(key).doubleValue();
  }
  public BigDecimal getBigDecimal(String key) {
    if (!gsonObject.has(key)) {
      return BigDecimal.ZERO;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return BigDecimal.ZERO;
    }
    return element.getAsBigDecimal();
  }
  public BigInteger getBigInteger(String key) {
    if (!gsonObject.has(key)) {
      return BigInteger.ZERO;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return BigInteger.ZERO;
    }
    return element.getAsBigInteger();
  }
  public String getString(String key) {
    if (!gsonObject.has(key)) {
      return null;
    }
    com.google.gson.JsonElement element = gsonObject.get(key);
    if (!element.isJsonPrimitive()) {
      return null;
    }
    return element.getAsString();
  }
  public Date getDate(String key) {
    Object val = getObject(key, Object.class);
    return ConverterUtils.castToDate(val);
  }

  public java.sql.Date getSqlDate(String key) {
    throw new UnsupportedOperationException("getSqlDate is not supported.");
  }
  public Timestamp getTimestamp(String key) {
    throw new UnsupportedOperationException("getTimestamp is not supported.");
  }

  public Object put(String key, Object value) {
    com.google.gson.JsonElement element = ConverterUtils.toJsonElement(value);
    this.gsonObject.add(key, element);
    return value;
  }

  public JSONObject fluentPut(String key, Object value) {
    com.google.gson.JsonElement ele = ConverterUtils.toJsonElement(value);
    this.gsonObject.add(key, ele);
    return this;
  }

  public void putAll(Map<? extends String, ? extends Object> m) {
    for(Map.Entry<? extends String, ? extends Object> entry: m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  public JSONObject fluentPutAll(Map<? extends String, ? extends Object> m) {
    putAll(m);
    return this;
  }

  public void clear() {
    for (String key : gsonObject.keySet()) {
      remove(key);
    }
  }

  public JSONObject fluentClear() {
    clear();
    return this;
  }

  public Object remove(Object key) {
    return gsonObject.remove((String)key);
  }

  public Set<String> keySet() {
    return gsonObject.keySet();
  }

  public Collection<Object> values() {
    return null;//gsonObject.values();
  }
  public Set<Map.Entry<String, Object>> entrySet() {
    Set<Map.Entry<String, com.google.gson.JsonElement>> objects = this.gsonObject.entrySet();
    return null;
  }

  @Override
  public Object clone() {
    return new JSONObject((com.google.gson.JsonObject) gsonObject.deepCopy());
  }

  public Map<String, Object> getInnerMap() {
    return null;//gsonObject.getInnerMap();
  }

  public <T> T toJavaObject(Class<T> clazz) {
    return null;//gsonObject.toJavaObject(clazz);
  }

  public int hashCode() {
    return gsonObject.hashCode();
  }
  public boolean equals(Object obj) {
    if (null == obj) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof JSONObject)) {
      return false;
    }
    return gsonObject.equals(((JSONObject)obj).gsonObject);
  }

  public String toJSONString() {
    return this.gsonObject.toString();
  }
}
