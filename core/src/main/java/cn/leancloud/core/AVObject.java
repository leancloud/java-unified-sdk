package cn.leancloud.core;

import cn.leancloud.AVException;
import cn.leancloud.core.annotation.AVClassName;
import cn.leancloud.core.ops.ObjectFieldOperation;
import cn.leancloud.core.ops.OperationBuilder;
import cn.leancloud.core.types.AVGeoPoint;
import cn.leancloud.network.PaasClient;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.PascalNameFilter;
import com.alibaba.fastjson.serializer.SerializeConfig;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

@JSONType(deserializer = ObjectTypeAdapter.class, serializer = ObjectTypeAdapter.class)
public class AVObject {
  public static final String KEY_CREATED_AT = "createdAt";
  public static final String KEY_UPDATED_AT = "updatedAt";
  public static final String KEY_OBJECT_ID = "objectId";
  public static final String KEY_ACL = "ACL";

  static final String KEY_CLASSNAME = "className";

  private static final Set<String> RESERVED_ATTRS = new HashSet<String>(
          Arrays.asList(KEY_CREATED_AT, KEY_UPDATED_AT, KEY_OBJECT_ID, KEY_ACL));
  private final static Map<String, Class<? extends AVObject>> SUB_CLASSES_MAP =
          new HashMap<String, Class<? extends AVObject>>();
  private final static Map<Class<? extends AVObject>, String> SUB_CLASSES_REVERSE_MAP =
          new HashMap<Class<? extends AVObject>, String>();

  protected String className;

  protected String objectId = "";
  protected Map<String, Object> serverData = new ConcurrentHashMap<String, Object>();
  protected Map<String, ObjectFieldOperation> operations = new ConcurrentHashMap<String, ObjectFieldOperation>();
  protected AVACL acl = null;

  public AVObject(String className) {
    this.className = className;
  }

  public String getClassName() {
    return this.className;
  }
  public String internalClassName() {
    return this.getClassName();
  }
  public void setClassName(String name) {this.className = name;}

  public String getCreatedAt() {
    return (String) this.serverData.get(KEY_CREATED_AT);
  }

  public String getUpdatedAt() {
    return (String) this.serverData.get(KEY_UPDATED_AT);
  }

  public String getObjectId() {
    if (this.serverData.containsKey(KEY_OBJECT_ID)) {
      return (String) this.serverData.get(KEY_OBJECT_ID);
    } else {
      return this.objectId;
    }
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  /**
   * getter
   */
  public boolean containsKey(String key) {
    return serverData.containsKey(key);
  }

  public Object get(String key) {
    Object value = serverData.get(key);
    ObjectFieldOperation op = operations.get(key);
    if (null != op) {
      value = op.apply(value);
    }
    return value;
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

  public Map<String, Object> getServerData() {
    return this.serverData;
  }

  /**
   * changable operations.
   */
  public void add(String key, Object value) {
    ;
  }

  public void addUnique(String key, Object value) {
    ;
  }

  public void put(String key, Object value) {
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Set, key, value);
    addNewOperation(op);
  }

  public void remove(String key) {
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Delete, key, null);
    addNewOperation(op);
  }

  public void increment(String key) {
    this.increment(key, 1);
  }
  public void increment(String key, Number value) {
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Increment, key, value);
    addNewOperation(op);
  }

  protected void addNewOperation(ObjectFieldOperation op) {
    if (null == op) {
      return;
    }
    ObjectFieldOperation previous = null;
    if (this.operations.containsKey(op.getField())) {
      previous = this.operations.get(op.getField());
    }
    this.operations.put(op.getField(), op.merge(previous));
  }

  /**
   * save/update with server.
   */
  protected JSONObject generateChangedParam() {
    Map<String, Object> params = new HashMap<String, Object>();
    Set<Map.Entry<String, ObjectFieldOperation>> entries = operations.entrySet();
    for (Map.Entry<String, ObjectFieldOperation> entry: entries) {
      Map<String, Object> oneOp = entry.getValue().encode();
      params.putAll(oneOp);
    }
    if (null != this.acl) {
      // TODO: need to check whether acl is changed or not.
      ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Set, KEY_ACL, acl);
      params.putAll(op.encode());
    }
    return new JSONObject(params);
  }

  public Observable<? extends AVObject> saveInBackground() {
    JSONObject paramData = generateChangedParam();
    if (StringUtil.isEmpty(getObjectId())) {
      return PaasClient.getStorageClient().createObject(this.className, paramData);
    } else {
      return PaasClient.getStorageClient().saveObject(this.className, getObjectId(), paramData);
    }
  }

  public Observable<Void> deleteInBackground() {
    return PaasClient.getStorageClient().deleteObject(this.className, getObjectId());
  }

  public Observable<AVObject> refreshInBackground() {
    Observable<AVObject> result = PaasClient.getStorageClient().fetchObject(this.className, getObjectId());
    return result.map(new Function<AVObject, AVObject>() {
      public AVObject apply(@NonNull AVObject avObject) throws Exception {
        System.out.println("update self.");
        AVObject.this.resetByServerData(avObject);
        return avObject;
      }
    });
  }

  protected void resetAll() {
    this.serverData.clear();
  }

  protected void resetByServerData(AVObject avObject) {
    if (null == avObject) {
      return;
    }
    resetAll();
    this.serverData.putAll(avObject.serverData);
  }

  /**
   * subclass
   */
  static Class<? extends AVObject> getSubClass(String className) {
    return SUB_CLASSES_MAP.get(className);
  }

  static String getSubClassName(Class<? extends AVObject> clazz) {
    if (AVUser.class.isAssignableFrom(clazz)) {
      return "_User";
    } else if (AVRole.class.isAssignableFrom(clazz)) {
      return "_Role";
    } else if (AVStatus.class.isAssignableFrom(clazz)) {
      return "_Status";
    } else {
      return SUB_CLASSES_REVERSE_MAP.get(clazz);
    }
  }

  /**
   * Register subclass to AVOSCloud SDK.It must be invocated before AVOSCloud.initialize.
   *
   * @param clazz The subclass.
   * @since 1.3.6
   */
  public static <T extends AVObject> void registerSubclass(Class<T> clazz) {
    AVClassName avClassName = clazz.getAnnotation(AVClassName.class);
    if (avClassName == null) {
      throw new IllegalArgumentException("The class is not annotated by @AVClassName");
    }
    String className = avClassName.value();
    SUB_CLASSES_MAP.put(className, clazz);
    SUB_CLASSES_REVERSE_MAP.put(clazz, className);
    // register object serializer/deserializer.
    ParserConfig.getGlobalInstance().putDeserializer(clazz, new ObjectTypeAdapter());
    SerializeConfig.getGlobalInstance().put(clazz, new ObjectTypeAdapter());
  }

  /**
   * ACL
   */
  public AVACL getACL() {
    if (null == this.acl) {
      synchronized (this) {
        if (null == this.acl) {
          this.acl = generateACLFromServerData();
        }
      }
    }
    return this.acl;
  }

  public void setACL(AVACL acl) {
    this.acl = acl;
  }

  private AVACL generateACLFromServerData() {
    if (!this.serverData.containsKey(KEY_ACL)) {
      return new AVACL();
    } else {
      JSONObject obj = (JSONObject) this.serverData.get(KEY_ACL);
      return new AVACL(obj);
    }
  }

  /**
   * common methods.
   */

  public JSONObject toJSONObject() {
    // TODO
    return null;
  }

  @Override
  public String toString() {
    String serverDataStr = JSON.toJSONString(this.serverData);
    return "AVObject{" +
            "className='" + className + '\'' +
            ", serverData=" + serverDataStr +
            '}';
  }
}
