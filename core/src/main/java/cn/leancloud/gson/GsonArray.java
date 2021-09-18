package cn.leancloud.gson;

import cn.leancloud.json.JSONArray;
import cn.leancloud.json.JSONObject;
import com.google.gson.JsonArray;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

public class GsonArray extends JSONArray {
  private com.google.gson.JsonArray gsonArray;

  static class InnerIterator implements Iterator<Object> {
    private Iterator<com.google.gson.JsonElement> gsonIterator = null;
    public InnerIterator(Iterator<com.google.gson.JsonElement> jsonIterator) {
      gsonIterator = jsonIterator;
    }

    public boolean hasNext() {
      return gsonIterator.hasNext();
    }

    public Object next() {
      com.google.gson.JsonElement elem = gsonIterator.next();
      if (null == elem) {
        return null;
      }
      return GsonWrapper.toJavaObject(elem);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  public GsonArray(com.google.gson.JsonArray array) {
    this.gsonArray = array;
  }
  public GsonArray(List<Object> list) {
    this.gsonArray = new com.google.gson.JsonArray(list.size());
    for (Object obj: list) {
      gsonArray.add(GsonWrapper.toJsonElement(obj));
    }
  }

  public GsonArray() {
    this.gsonArray = new com.google.gson.JsonArray();
  }

  public JsonArray getRawObject() {
    return gsonArray;
  }
  public int size() {
    return gsonArray.size();
  }

  public boolean isEmpty() {
    return size() <= 0;
  }
  public boolean contains(Object o) {
    com.google.gson.JsonElement elem = GsonWrapper.toJsonElement(o);
    return gsonArray.contains(elem);
  }

  public Iterator<Object> iterator() {
    return new GsonArray.InnerIterator(gsonArray.iterator());
  }

  public Object[] toArray() {
    List<Object> list = new ArrayList<>(size());
    Iterator<Object> it = iterator();
    while (it.hasNext()) {
      list.add(it.next());
    }
    return list.toArray();
  }

  public <T> T[] toArray(T[] a) {
    List<Object> list = new ArrayList<>(size());
    Iterator<Object> it = iterator();
    while (it.hasNext()) {
      list.add(it.next());
    }
    return list.toArray(a);
  }

  public boolean add(Object obj) {
    gsonArray.add(GsonWrapper.toJsonElement(obj));
    return true;
  }

  public JSONArray fluentAdd(Object e) {
    add(e);
    return this;
  }

  public boolean remove(Object o) {
    return gsonArray.remove(GsonWrapper.toJsonElement(o));
  }

  public JSONArray fluentRemove(Object o) {
    remove(o);
    return this;
  }

  public boolean containsAll(Collection<? extends Object> c) {
    for (Object o: c) {
      if (!gsonArray.contains(GsonWrapper.toJsonElement(o))) {
        return false;
      }
    }
    return true;
  }

  public boolean addAll(Collection<? extends Object> c) {
    for (Object o : c) {
      gsonArray.add(GsonWrapper.toJsonElement(o));
    }
    return true;
  }

  public JSONArray fluentAddAll(Collection<? extends Object> c) {
    addAll(c);
    return this;
  }

  public boolean addAll(int index, Collection<? extends Object> c) {
    throw new UnsupportedOperationException("addAll with specified index.");
  }

  public JSONArray fluentAddAll(int index, Collection<? extends Object> c) {
    addAll(index, c);
    return this;
  }

  public boolean removeAll(Collection<? extends Object> c) {
    for (Object o : c) {
      gsonArray.remove(GsonWrapper.toJsonElement(o));
    }
    return true;
  }

  public JSONArray fluentRemoveAll(Collection<?> c) {
    removeAll(c);
    return this;
  }

  public boolean retainAll(Collection<?> c) {
    return false;
  }

  public JSONArray fluentRetainAll(Collection<?> c) {
    retainAll(c);
    return this;
  }

  public void clear() {
    int total = size();
    while (total >= 1) {
      gsonArray.remove(total -1);
      total--;
    }
  }

  public JSONArray fluentClear() {
    clear();
    return this;
  }

  public Object set(int index, Object element) {
    gsonArray.set(index, GsonWrapper.toJsonElement(element));
    return element;
  }

  public JSONArray fluentSet(int index, Object element) {
    set(index, element);
    return this;
  }

  public void add(int index, Object element) {
    com.google.gson.JsonElement elem = gsonArray.get(index);
    if (elem.isJsonArray()) {
      ((com.google.gson.JsonArray)elem).add(GsonWrapper.toJsonElement(element));
    }
  }

  public JSONArray fluentAdd(int index, Object element) {
    add(index, element);
    return this;
  }

  public Object remove(int index) {
    return gsonArray.remove(index);
  }

  public JSONArray fluentRemove(int index) {
    remove(index);
    return this;
  }

  public int indexOf(Object o) {
    com.google.gson.JsonElement elem = GsonWrapper.toJsonElement(o);
    for (int i = 0;i < size();i ++) {
      if (elem.equals(gsonArray.get(i))) {
        return i;
      }
    }
    return -1;
  }

  public int lastIndexOf(Object o) {
    com.google.gson.JsonElement elem = GsonWrapper.toJsonElement(o);
    for (int i = size() - 1;i >= 0;i --) {
      if (elem.equals(gsonArray.get(i))) {
        return i;
      }
    }
    return -1;
  }

  public ListIterator<Object> listIterator() {
    throw new UnsupportedOperationException("remove");
  }

  public ListIterator<Object> listIterator(int index) {
    throw new UnsupportedOperationException("remove");
  }

  public List<Object> subList(int fromIndex, int toIndex) {
    List<Object> result = new ArrayList<>();
    for (int i = fromIndex; i >= 0 && i < size() && i < toIndex; i++) {
      result.add(GsonWrapper.toJavaObject(gsonArray.get(i)));
    }
    return result;
  }

  public Object get(int index) {
    return GsonWrapper.toJavaObject(getElement(index));
  }

  public JSONObject getJSONObject(int index) {
    com.google.gson.JsonElement result = getElement(index);
    if (null == result || !result.isJsonObject()) {
      return null;
    }
    return new GsonObject(result.getAsJsonObject());
  }

  public JSONArray getJSONArray(int index) {
    com.google.gson.JsonElement result = getElement(index);
    if (null == result || !result.isJsonArray()) {
      return null;
    }
    return new GsonArray(result.getAsJsonArray());
  }

  public <T> T getObject(int index, Class<T> clazz) {
    com.google.gson.JsonElement result = getElement(index);
    if (null == result) {
      return null;
    }
    return GsonWrapper.toJavaObject(result, clazz);
  }

  public <T> T getObject(int index, Type type) {
    return getObject(index, (Class<? extends T>) com.google.gson.reflect.TypeToken.get(type).getRawType());
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
    Object val = get(index);
    return GsonWrapper.castToDate(val);
  }

  public java.sql.Date getSqlDate(int index) {
    throw new UnsupportedOperationException("getSqlDate is not supported.");
  }

  public Timestamp getTimestamp(int index) {
    throw new UnsupportedOperationException("getTimestamp is not supported.");
  }

  public <T> List<T> toJavaList(Class<T> clazz) {
    List<T> result = new ArrayList<>(size());
    for ( int i = 0;i < size(); i++) {
      result.add(getObject(i, clazz));
    }
    return result;
  }

  public Object clone() {
    return new GsonArray(gsonArray.deepCopy());
  }

  public boolean equals(Object obj) {
    if (null == obj) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof GsonArray)) {
      return false;
    }

    return gsonArray.equals(((GsonArray) obj).gsonArray);
  }

  public int hashCode() {
    return gsonArray.hashCode();
  }

  public String toJSONString() {
    return gsonArray.toString();
  }
}
