package cn.leancloud.core;

import cn.leancloud.AVException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.*;

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
    return (String)this.serverData.get(KEY_CREATED_AT);
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
    return false;
  }
  public Object get(String key) {
    return null;
  }
  public void put(String key, Object value) {
    ;
  }
  public boolean getBoolean(String key) {
    return false;
  }
  public byte[] getBytes(String key) {
    return null;
  }
  public Date getDate(String key) {
    return null;
  }
  public int getInt(String key) {
    return 0;
  }
  public long getLong(String key) {
    return 0l;
  }

  public void saveInBackground() throws AVException {
    ;
  }

  public void refreshInBackground() throws AVException {
    ;
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
