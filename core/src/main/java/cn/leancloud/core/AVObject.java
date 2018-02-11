package cn.leancloud.core;

import cn.leancloud.AVException;
import cn.leancloud.core.types.AVGeoPoint;
import cn.leancloud.network.PaasClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.*;

import io.reactivex.Observable;

@JSONType(deserializer = ObjectTypeAdapter.class, serializer = ObjectTypeAdapter.class)
public class AVObject {
  public static final String KEY_CREATED_AT = "createdAt";
  public static final String KEY_UPDATED_AT = "updatedAt";
  public static final String KEY_OBJECT_ID = "objectId";

  private static final Set<String> RESERVED_ATTRS = new HashSet<String>(
          Arrays.asList(KEY_CREATED_AT, KEY_UPDATED_AT, KEY_OBJECT_ID));

  private String className;

  protected Map<String, Object> serverData = new HashMap<String, Object>();
  protected Map<String, Object> localData = new HashMap<String, Object>();

  public AVObject(String className) {
    this.className = className;
  }

  public String getClassName() {
    return this.className;
  }

  public String getCreatedAt() {
    return (String) this.serverData.get(KEY_CREATED_AT);
  }

  public String getUpdatedAt() {
    return (String) this.serverData.get(KEY_UPDATED_AT);
  }

  public String getObjectId() {
    return (String) this.serverData.get(KEY_OBJECT_ID);
  }

  public void add(String key, Object value) {
    ;
  }

  public void addUnique(String key, Object value) {
    ;
  }

  public boolean containsKey(String key) {
    return serverData.containsKey(key);
  }

  public Object get(String key) {
    return serverData.get(key);
  }

  public void put(String key, Object value) {
    this.serverData.put(key, value);
  }

  public boolean getBoolean(String key) {
    Boolean b = (Boolean) get(key);
    return b == null ? false : b;
  }

  public byte[] getBytes(String key) {
    return (byte[]) (get(key));
  }

  public Date getDate(String key) {
    return (Date) get(key);
  }

  public int getInt(String key) {
    Number v = (Number) get(key);
    if (v != null) return v.intValue();
    return 0;
  }

  public long getLong(String key) {
    Number v = (Number) get(key);
    if (v != null) return v.longValue();
    return 0l;
  }

  public double getDouble(String key) {
    Number number = (Number) get(key);
    if (number != null) return number.doubleValue();
    return 0f;
  }

  public Number getNumber(String key) {
    Number number = (Number) get(key);
    return number;
  }

  public JSONArray getJSONArray(String key) {
    Object list = get(key);
    if (list == null) {
      return null;
    }
    if (list instanceof JSONArray) {
      return (JSONArray) list;
    }
    if (list instanceof List<?>) {
      JSONArray array = new JSONArray((List<Object>) list);
      return array;
    }
    if (list instanceof Object[]) {
      JSONArray array = new JSONArray();
      for (Object obj : (Object[]) list) {
        array.add(obj);
      }
      return array;
    }
    return null;
  }

  public JSONObject getJSONObject(String key) {
    Object object = get(key);
    if (object instanceof JSONObject) {
      return (JSONObject) object;
    }
    String jsonString = JSON.toJSONString(object);
    JSONObject jsonObject = null;
    try {
      jsonObject = JSON.parseObject(jsonString);
    } catch (Exception exception) {
      throw new IllegalStateException("Invalid json string", exception);
    }
    return jsonObject;
  }

  public AVGeoPoint getAVGeoPoint(String key) {
    return (AVGeoPoint) get(key);
  }

  public AVFile getAVFile(String key) {
    return (AVFile) get(key);
  }

  public <T extends AVObject> T getAVObject(String key) {
    return (T) get(key);
  }

  public Observable<Void> saveInBackground() {
    return PaasClient.getStorageClient().batchSave(null);
  }

  public Observable<AVObject> refreshInBackground() {
    return PaasClient.getStorageClient().fetchObject(this.className, getObjectId());
  }

  @Override
  public String toString() {
    String serverDataStr = JSON.toJSONString(this.serverData);
    return "AVObject{" +
            "className='" + className + '\'' +
            ", serverData=" + serverDataStr +
            ", localData=" + localData +
            '}';
  }
}
