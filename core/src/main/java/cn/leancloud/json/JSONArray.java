package cn.leancloud.json;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

public class JSONArray implements List<Object>, Cloneable, Serializable {
  private com.alibaba.fastjson.JSONArray fastArray;

  public JSONArray(com.alibaba.fastjson.JSONArray array) {
    this.fastArray = array;
  }
  public JSONArray(List<Object> list) {
    this.fastArray = new com.alibaba.fastjson.JSONArray(list);
  }

  public JSONArray() {
    this.fastArray = new com.alibaba.fastjson.JSONArray();
  }

  public int size() {
    return fastArray.size();
  }

  public boolean isEmpty() {
    return fastArray.isEmpty();
  }
  public boolean contains(Object o) {
    return fastArray.contains(o);
  }

  public Iterator<Object> iterator() {
    return fastArray.iterator();
  }

  public Object[] toArray() {
    return fastArray.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return fastArray.toArray(a);
  }

  public boolean add(Object e) {
    return fastArray.add(e);
  }

  public JSONArray fluentAdd(Object e) {
    fastArray.fluentAdd(e);
    return this;
  }

  public boolean remove(Object o) {
    return fastArray.remove(o);
  }

  public JSONArray fluentRemove(Object o) {
    fastArray.fluentRemove(o);
    return this;
  }

  public boolean containsAll(Collection<?> c) {
    return fastArray.containsAll(c);
  }

  public boolean addAll(Collection<? extends Object> c) {
    return fastArray.addAll(c);
  }

  public JSONArray fluentAddAll(Collection<? extends Object> c) {
    fastArray.fluentAddAll(c);
    return this;
  }

  public boolean addAll(int index, Collection<? extends Object> c) {
    return fastArray.addAll(index, c);
  }

  public JSONArray fluentAddAll(int index, Collection<? extends Object> c) {
    fastArray.fluentAddAll(index, c);
    return this;
  }

  public boolean removeAll(Collection<?> c) {
    return fastArray.removeAll(c);
  }

  public JSONArray fluentRemoveAll(Collection<?> c) {
    fastArray.fluentRemoveAll(c);
    return this;
  }

  public boolean retainAll(Collection<?> c) {
    return fastArray.retainAll(c);
  }

  public JSONArray fluentRetainAll(Collection<?> c) {
    fastArray.fluentRetainAll(c);
    return this;
  }

  public void clear() {
    fastArray.clear();
  }

  public JSONArray fluentClear() {
    fastArray.fluentClear();
    return this;
  }

  public Object set(int index, Object element) {
    return fastArray.set(index, element);
  }

  public JSONArray fluentSet(int index, Object element) {
    fastArray.fluentSet(index, element);
    return this;
  }

  public void add(int index, Object element) {
    fastArray.add(index, element);
  }

  public JSONArray fluentAdd(int index, Object element) {
    fastArray.fluentAdd(index, element);
    return this;
  }

  public Object remove(int index) {
    return fastArray.remove(index);
  }

  public JSONArray fluentRemove(int index) {
    fastArray.fluentRemove(index);
    return this;
  }

  public int indexOf(Object o) {
    return fastArray.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return fastArray.lastIndexOf(o);
  }

  public ListIterator<Object> listIterator() {
    return fastArray.listIterator();
  }

  public ListIterator<Object> listIterator(int index) {
    return fastArray.listIterator(index);
  }

  public List<Object> subList(int fromIndex, int toIndex) {
    return fastArray.subList(fromIndex, toIndex);
  }

  public Object get(int index) {
    return fastArray.get(index);
  }

  public JSONObject getJSONObject(int index) {
    com.alibaba.fastjson.JSONObject result = fastArray.getJSONObject(index);
    if (null == result) {
      return null;
    }
    return new JSONObject(result);
  }

  public JSONArray getJSONArray(int index) {
    com.alibaba.fastjson.JSONArray result = fastArray.getJSONArray(index);
    if (null == result) {
      return null;
    }
    return new JSONArray(result);
  }

  public <T> T getObject(int index, Class<T> clazz) {
    return fastArray.getObject(index, clazz);
  }

  public <T> T getObject(int index, Type type) {
    return fastArray.getObject(index, type);
  }

  public Boolean getBoolean(int index) {
    return fastArray.getBoolean(index);
  }

  public boolean getBooleanValue(int index) {
    return fastArray.getBooleanValue(index);
  }

  public Byte getByte(int index) {
    return fastArray.getByte(index);
  }

  public byte getByteValue(int index) {
    return fastArray.getByteValue(index);
  }

  public Short getShort(int index) {
    return fastArray.getShort(index);
  }

  public short getShortValue(int index) {
    return fastArray.getShortValue(index);
  }

  public Integer getInteger(int index) {
    return fastArray.getInteger(index);
  }

  public int getIntValue(int index) {
    return fastArray.getIntValue(index);
  }

  public Long getLong(int index) {
    return fastArray.getLong(index);
  }

  public long getLongValue(int index) {
    return fastArray.getLongValue(index);
  }

  public Float getFloat(int index) {
    return fastArray.getFloat(index);
  }

  public float getFloatValue(int index) {
    return fastArray.getFloatValue(index);
  }

  public Double getDouble(int index) {
    return fastArray.getDouble(index);
  }

  public double getDoubleValue(int index) {
    return fastArray.getDoubleValue(index);
  }

  public BigDecimal getBigDecimal(int index) {
    return fastArray.getBigDecimal(index);
  }

  public BigInteger getBigInteger(int index) {
    return fastArray.getBigInteger(index);
  }

  public String getString(int index) {
    return fastArray.getString(index);
  }

  public Date getDate(int index) {
    return fastArray.getDate(index);
  }

  public java.sql.Date getSqlDate(int index) {
    return fastArray.getSqlDate(index);
  }

  public Timestamp getTimestamp(int index) {
    return fastArray.getTimestamp(index);
  }

  public <T> List<T> toJavaList(Class<T> clazz) {
    return fastArray.toJavaList(clazz);
  }

  public Object clone() {
    return new JSONArray((com.alibaba.fastjson.JSONArray)fastArray.clone());
  }

  public boolean equals(Object obj) {
    if (null == obj) {
      return false;
    }
    if (!(obj instanceof JSONArray)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    return fastArray.equals(obj);
  }

  public int hashCode() {
    return fastArray.hashCode();
  }

  public String toJSONString() {
    return fastArray.toJSONString();
  }
}
