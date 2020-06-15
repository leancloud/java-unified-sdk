package cn.leancloud.json;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

public class JSONArray implements List<Object>, Cloneable, Serializable {
  private com.google.gson.JsonArray gsonArray;

  public JSONArray(com.google.gson.JsonArray array) {
    this.gsonArray = array;
  }
  public JSONArray(List<Object> list) {
    this.gsonArray = new com.google.gson.JsonArray(list.size());
    // TODO: add objects to gsonArray.
  }

  public JSONArray() {
    this.gsonArray = new com.google.gson.JsonArray();
  }

  public int size() {
    return gsonArray.size();
  }

  public boolean isEmpty() {
    return size() <= 0;
  }
  public boolean contains(Object o) {
    // TODO: convert object to element.
    com.google.gson.JsonElement elem = null;
    return gsonArray.contains(elem);
  }

  public Iterator<Object> iterator() {
    return null;//gsonArray.iterator();
  }

  public Object[] toArray() {
    return null; // return gsonArray.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return null;//gsonArray.toArray(a);
  }

  public boolean add(Object e) {
    return false;//gsonArray.add(e);
  }

  public JSONArray fluentAdd(Object e) {
    //gsonArray.fluentAdd(e);
    return this;
  }

  public boolean remove(Object o) {
    return false;//gsonArray.remove(o);
  }

  public JSONArray fluentRemove(Object o) {
    //gsonArray.fluentRemove(o);
    return this;
  }

  public boolean containsAll(Collection<?> c) {
    return false;//gsonArray.containsAll(c);
  }

  public boolean addAll(Collection<? extends Object> c) {
    return false;//gsonArray.addAll(c);
  }

  public JSONArray fluentAddAll(Collection<? extends Object> c) {
    //gsonArray.fluentAddAll(c);
    return this;
  }

  public boolean addAll(int index, Collection<? extends Object> c) {
    return false;//gsonArray.addAll(index, c);
  }

  public JSONArray fluentAddAll(int index, Collection<? extends Object> c) {
    //gsonArray.fluentAddAll(index, c);
    return this;
  }

  public boolean removeAll(Collection<?> c) {
    return false;//gsonArray.removeAll(c);
  }

  public JSONArray fluentRemoveAll(Collection<?> c) {
    //gsonArray.fluentRemoveAll(c);
    return this;
  }

  public boolean retainAll(Collection<?> c) {
    return false;//gsonArray.retainAll(c);
  }

  public JSONArray fluentRetainAll(Collection<?> c) {
    //gsonArray.fluentRetainAll(c);
    return this;
  }

  public void clear() {
    //gsonArray.clear();
  }

  public JSONArray fluentClear() {
    //gsonArray.fluentClear();
    return this;
  }

  public Object set(int index, Object element) {
    return null;//gsonArray.set(index, element);
  }

  public JSONArray fluentSet(int index, Object element) {
    //gsonArray.fluentSet(index, element);
    return this;
  }

  public void add(int index, Object element) {
    //gsonArray.add(index, element);
  }

  public JSONArray fluentAdd(int index, Object element) {
    //gsonArray.fluentAdd(index, element);
    return this;
  }

  public Object remove(int index) {
    return null;//gsonArray.remove(index);
  }

  public JSONArray fluentRemove(int index) {
    //gsonArray.fluentRemove(index);
    return this;
  }

  public int indexOf(Object o) {
    return -1;//gsonArray.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return -1;//gsonArray.lastIndexOf(o);
  }

  public ListIterator<Object> listIterator() {
    return null;//return gsonArray.listIterator();
  }

  public ListIterator<Object> listIterator(int index) {
    return null;//gsonArray.listIterator(index);
  }

  public List<Object> subList(int fromIndex, int toIndex) {
    return null;//gsonArray.subList(fromIndex, toIndex);
  }

  public Object get(int index) {
    return null;//gsonArray.get(index);
  }

  public JSONObject getJSONObject(int index) {
    com.google.gson.JsonObject result = null;//gsonArray.getJSONObject(index);
    if (null == result) {
      return null;
    }
    return new JSONObject(result);
  }

  public JSONArray getJSONArray(int index) {
    com.google.gson.JsonArray result = null;//gsonArray.getJSONArray(index);
    if (null == result) {
      return null;
    }
    return new JSONArray(result);
  }

  public <T> T getObject(int index, Class<T> clazz) {
    return null;//gsonArray.getObject(index, clazz);
  }

  public <T> T getObject(int index, Type type) {
    return null;//gsonArray.getObject(index, type);
  }

  private com.google.gson.JsonElement getElement(int index) {
    if (index >= size()) {
      return null;
    }
    return gsonArray.get(index);
  }

  public Boolean getBoolean(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return false;
    }
    return elem.getAsBoolean();
  }

  public boolean getBooleanValue(int index) {
    return getBoolean(index).booleanValue();
  }

  public Byte getByte(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return 0;
    }
    return elem.getAsByte();
  }

  public byte getByteValue(int index) {
    return getByte(index).byteValue();
  }

  public Short getShort(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return 0;
    }
    return elem.getAsShort();
  }

  public short getShortValue(int index) {
    return getShort(index).shortValue();
  }

  public Integer getInteger(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return 0;
    }
    return elem.getAsInt();
  }

  public int getIntValue(int index) {
    return getInteger(index).intValue();
  }

  public Long getLong(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return 0l;
    }
    return elem.getAsLong();
  }

  public long getLongValue(int index) {
    return getLong(index).longValue();
  }

  public Float getFloat(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return 0f;
    }
    return elem.getAsFloat();
  }

  public float getFloatValue(int index) {
    return getFloat(index).floatValue();
  }

  public Double getDouble(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return 0d;
    }
    return elem.getAsDouble();
  }

  public double getDoubleValue(int index) {
    return getDouble(index).doubleValue();
  }

  public BigDecimal getBigDecimal(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return null;
    }
    return elem.getAsBigDecimal();
  }

  public BigInteger getBigInteger(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return null;
    }
    return elem.getAsBigInteger();
  }

  public String getString(int index) {
    com.google.gson.JsonElement elem = getElement(index);
    if (null == elem) {
      return null;
    }
    return elem.getAsString();
  }

  public Date getDate(int index) {
    return null;//gsonArray.getDate(index);
  }

  public java.sql.Date getSqlDate(int index) {
    return null;//gsonArray.getSqlDate(index);
  }

  public Timestamp getTimestamp(int index) {
    return null;//gsonArray.getTimestamp(index);
  }

  public <T> List<T> toJavaList(Class<T> clazz) {
    return null;//gsonArray.toJavaList(clazz);
  }

  public Object clone() {
    return new JSONArray((com.google.gson.JsonArray) gsonArray.deepCopy());
  }

  public boolean equals(Object obj) {
    if (null == obj) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof JSONArray)) {
      return false;
    }

    return gsonArray.equals(obj);
  }

  public int hashCode() {
    return gsonArray.hashCode();
  }

  public String toJSONString() {
    return gsonArray.toString();
  }
}
