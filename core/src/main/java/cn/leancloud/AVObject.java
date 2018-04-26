package cn.leancloud;

import cn.leancloud.ops.BaseOperation;
import cn.leancloud.ops.CompoundOperation;
import cn.leancloud.ops.ObjectFieldOperation;
import cn.leancloud.ops.OperationBuilder;
import cn.leancloud.types.AVDate;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.core.PaasClient;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
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

  protected String className;
  protected static AVLogger LOGGER = LogUtil.getLogger(AVObject.class);

  protected String objectId = "";
  protected Map<String, Object> serverData = new ConcurrentHashMap<String, Object>();
  protected Map<String, ObjectFieldOperation> operations = new ConcurrentHashMap<String, ObjectFieldOperation>();
  protected AVACL acl = null;
  private volatile boolean fetchWhenSave = false;

  public AVObject() {
    this.className = Transformer.getSubClassName(this.getClass());
  }

  public AVObject(String className) {
    Transformer.checkClassName(className);
    this.className = className;
  }

  public AVObject(AVObject other) {
    this.className = other.className;
    this.objectId = other.objectId;
    this.serverData.putAll(other.serverData);
    this.operations.putAll(other.operations);
    this.acl = other.acl;
  }

  public String getClassName() {
    return this.className;
  }
  public String internalClassName() {
    return this.getClassName();
  }
  public void setClassName(String name) {
    Transformer.checkClassName(name);
    this.className = name;
  }

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

  public boolean isFetchWhenSave() {
    return fetchWhenSave;
  }

  public void setFetchWhenSave(boolean fetchWhenSave) {
    this.fetchWhenSave = fetchWhenSave;
  }

  /**
   * getter
   */
  public boolean containsKey(String key) {
    return serverData.containsKey(key);
  }

  public boolean has(String key) {
    return (this.get(key) != null);
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
    Object res = get(key);
    if (res instanceof Date) {
      return (Date)res;
    }
    JSONObject rawData = (JSONObject) get(key);
    if (null == rawData) {
      return null;
    }
    AVDate date = new AVDate((JSONObject) get(key));
    return (null == date) ? null : date.getDate();
  }

  public String getString(String key) {
    Object obj = get(key);
    if (obj instanceof String)
      return (String) obj;
    else
      return null;
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
    try {
      return (T) get(key);
    } catch (Exception ex) {
      LOGGER.w("failed to convert Object.", ex);
      return null;
    }
  }

  public <T extends AVObject> AVRelation<T> getRelation(String key) {
    return null;
  }
  void addRelation(final AVObject object, final String key, boolean submit) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.AddRelation, key, object);
    addNewOperation(op);
  }
  void removeRelation(final AVObject object, final String key, boolean submit) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.RemoveRelation, key, object);
    addNewOperation(op);
  }

  public List getList(String key) {
    return (List) get(key);
  }

  public Map<String, Object> getServerData() {
    return this.serverData;
  }

  protected void validFieldName(String key) {
    if (StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("Blank key");
    }
    if (key.startsWith("_")) {
      throw new IllegalArgumentException("key should not start with '_'");
    }
    if (RESERVED_ATTRS.contains(key)) {
      throw new IllegalArgumentException("key(" + key + ") is reserved by LeanCloud");
    }
  }

  public boolean isDataAvailable() {
    return !StringUtil.isEmpty(this.objectId) && !this.serverData.isEmpty();
  }

  /**
   * changable operations.
   */
  public void add(String key, Object value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Add, key, value);
    addNewOperation(op);
  }
  public void addAll(String key, Collection<?> values) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Add, key, values);
    addNewOperation(op);
  }

  public void addUnique(String key, Object value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.AddUnique, key, value);
    addNewOperation(op);
  }
  public void addAllUnique(String key, Collection<?> values) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.AddUnique, key, values);
    addNewOperation(op);
  }

  public void put(String key, Object value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Set, key, value);
    addNewOperation(op);
  }

  public void remove(String key) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Delete, key, null);
    addNewOperation(op);
  }

  public void removeAll(String key, Collection<?> values) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Remove, key, values);
    addNewOperation(op);
  }

  public void increment(String key) {
    this.increment(key, 1);
  }
  public void increment(String key, Number value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Increment, key, value);
    addNewOperation(op);
  }

  public void decrement(String key) {
    decrement(key, 1);
  }
  public void decrement(String key, Number value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Decrement, key, value);
    addNewOperation(op);
  }

  public void bitAnd(String key, long value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.BitAnd, key, value);
    addNewOperation(op);
  }
  public void bitOr(String key, long value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.BitOr, key, value);
    addNewOperation(op);
  }
  public void bitXor(String key, long value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.BitXor, key, value);
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

  private boolean needBatchMode() {
    for (ObjectFieldOperation op : this.operations.values()) {
      if (op instanceof CompoundOperation) {
        return true;
      }
    }
    return false;
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
      AVACL serverACL = generateACLFromServerData();
      if (!this.acl.equals(serverACL)) {
        ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Set, KEY_ACL, acl);
        params.putAll(op.encode());
      }
    }
    if (!needBatchMode()) {
      return new JSONObject(params);
    }
    params.put(BaseOperation.KEY_INTERNAL_ID, getObjectId());

    Map<String, Object> topParams = new HashMap<String, Object>();
    topParams.put(BaseOperation.KEY_PATH, getRequestRawEndpoint());
    topParams.put(BaseOperation.KEY_HTTP_METHOD, getRequestMethod());
    topParams.put(BaseOperation.KEY_BODY, params);

    List<Map<String, Object>> finalParams = new ArrayList<Map<String, Object>>();
    finalParams.add(topParams);
    for (ObjectFieldOperation ops : this.operations.values()) {
      if (ops instanceof CompoundOperation) {
        List<Map<String, Object>> restParams = ((CompoundOperation)ops).encodeRestOp(this);
        if (null != restParams && restParams.size() > 0) {
          finalParams.addAll(restParams);
        }
      }
    }
    Map<String, Object> finalResult = new HashMap<String, Object>(1);
    finalResult.put("requests", finalParams);

    return new JSONObject(finalResult);
  }

  public Observable<? extends AVObject> saveInBackground() {
    return saveInBackground(null);
  }

  public Observable<? extends AVObject> saveInBackground(AVSaveOption option) {
    final JSONObject paramData = generateChangedParam();
    LOGGER.d("saveObject param: " + paramData.toJSONString());
    final String currentObjectId = getObjectId();
    if (needBatchMode()) {
      LOGGER.w("Caution: batch mode will ignore fetchWhenSave flag.");
      if (StringUtil.isEmpty(currentObjectId)) {
        return PaasClient.getStorageClient().batchSave(paramData).map(new Function<JSONArray, AVObject>() {
          public AVObject apply(JSONArray object) throws Exception {
            if (null != object && object.size() > 0) {
              LOGGER.d("batchSave result: " + object.toJSONString());

              Map<String, Object> lastResult = object.getObject(object.size() - 1, Map.class);
              if (null != lastResult) {
                AVObject.this.serverData.putAll(lastResult);
              }
            }
            return AVObject.this;
          }
        });
      } else {
        return PaasClient.getStorageClient().batchUpdate(paramData).map(new Function<JSONObject, AVObject>() {
          public AVObject apply(JSONObject object) throws Exception {
            if (null != object) {
              LOGGER.d("batchUpdate result: " + object.toJSONString());
              Map<String, Object> lastResult = object.getObject(currentObjectId, Map.class);
              if (null != lastResult) {
                AVObject.this.serverData.putAll(lastResult);
              }
            }
            return AVObject.this;
          }
        });
      }
    } else {
      if (StringUtil.isEmpty(currentObjectId)) {
        return PaasClient.getStorageClient().createObject(this.className, paramData, isFetchWhenSave())
                .map(new Function<AVObject, AVObject>() {
                  @Override
                  public AVObject apply(AVObject avObject) throws Exception {
                    AVObject.this.mergeRawData(avObject);
                    return AVObject.this;
                  }
                });
      } else {
        return PaasClient.getStorageClient().saveObject(this.className, getObjectId(), paramData, isFetchWhenSave())
                .map(new Function<AVObject, AVObject>() {
                  @Override
                  public AVObject apply(AVObject avObject) throws Exception {
                    AVObject.this.mergeRawData(avObject);
                    return AVObject.this;
                  }
                });
      }
    }
  }

  public void save() {
    saveInBackground().blockingSubscribe();
  }

  public Observable<AVNull> deleteInBackground() {
    return PaasClient.getStorageClient().deleteObject(this.className, getObjectId());
  }

  public void delete() {
    deleteInBackground().blockingSubscribe();
  }

  public static Observable<AVNull> deleteAllInBackground(Collection<? extends AVObject> objects) {
    if (null == objects || objects.size() < 1) {
      return Observable.just(AVNull.getINSTANCE());
    }
    String className = null;
    StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for (AVObject o : objects) {
      if (StringUtil.isEmpty(o.getObjectId()) || StringUtil.isEmpty(o.getClassName())) {
        return Observable.error(new IllegalArgumentException("Invalid AVObject, the class name or objectId is blank."));
      }
      if (className == null) {
        className = o.getClassName();
        sb.append(o.getObjectId());
      } else if (className.equals(o.getClassName())) {
        sb.append(",").append(o.getObjectId());
      } else {
        return Observable.error(new IllegalArgumentException("The objects class name must be the same."));
      }
    }
    return PaasClient.getStorageClient().deleteObject(className, sb.toString());
  }

  public void refresh() {
    refresh(null);
  }

  public void refresh(String includeKeys) {
    refreshInBackground(includeKeys).blockingSubscribe();
  }

  public Observable<? extends AVObject> refreshInBackground() {
    return refreshInBackground(null);

  }

  public Observable<? extends AVObject> refreshInBackground(String includeKeys) {
    return PaasClient.getStorageClient().fetchObject(this.className, getObjectId(), includeKeys)
            .map(new Function<AVObject, AVObject>() {
              public AVObject apply(AVObject avObject) throws Exception {
                AVObject.this.serverData.clear();
                AVObject.this.serverData.putAll(avObject.serverData);
                return AVObject.this;
              }
            });
  }

  public AVObject fetch() {
    return fetch(null);
  }
  public AVObject fetch(String includeKeys) {
    refresh(includeKeys);
    return this;
  }

  public Observable<? extends AVObject> fetchIfNeededInBackground() {
    if (!StringUtil.isEmpty(getObjectId()) && this.serverData.size() > 0) {
      return Observable.just(this);
    } else {
      return refreshInBackground();
    }
  }

  public AVObject fetchIfNeeded() {
    fetchIfNeededInBackground().blockingSubscribe();
    return this;
  }

  protected void resetAll() {
    this.objectId = "";
    this.acl = null;
    this.serverData.clear();
    this.operations.clear();
  }

  protected void resetByRawData(AVObject avObject) {
    resetAll();
    if (null != avObject) {
      this.serverData.putAll(avObject.serverData);
    }
  }

  void mergeRawData(AVObject avObject) {
    if (null != avObject) {
      this.serverData.putAll(avObject.serverData);
    }
  }

  public void resetServerData(Map data) {
    this.serverData.clear();
    this.serverData.putAll(data);
  }

  public String getRequestRawEndpoint() {
    if (StringUtil.isEmpty(getObjectId())) {
      return "/1.1/classes/" + this.getClassName();
    } else {
      return "/1.1/classes/" + this.getClassName() + "/" + getObjectId();
    }
  }

  public String getRequestMethod() {
    if (StringUtil.isEmpty(getObjectId())) {
      return "POST";
    } else {
      return "PUT";
    }
  }

  /**
   * Register subclass to AVOSCloud SDK.It must be invocated before AVOSCloud.initialize.
   *
   * @param clazz The subclass.
   * @since 1.3.6
   */
  public static <T extends AVObject> void registerSubclass(Class<T> clazz) {
    Transformer.registerClass(clazz);
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

  public static <T extends AVObject> AVQuery<T> getQuery(Class<T> clazz) {
    return new AVQuery<T>(Transformer.getSubClassName(clazz), clazz);
  }

  /**
   * common methods.
   */

  public JSONObject toJSONObject() {
    return new JSONObject(this.serverData);
  }

  public static AVObject createWithoutData(String className, String objectId) {
    AVObject object = new AVObject(className);
    object.setObjectId(objectId);
    return object;
  }

  public static <T extends AVObject> T createWithoutData(Class<T> clazz, String objectId) throws AVException{
    try {
      T obj = clazz.newInstance();
      obj.setClassName(Transformer.getSubClassName(clazz));
      obj.setObjectId(objectId);
      return obj;
    } catch (Exception ex) {
      throw new AVException(ex);
    }
  }

  @Override
  public String toString() {
    String serverDataStr = JSON.toJSONString(this.serverData);
    return this.getClass().getSimpleName() + "{" +
            "className='" + className + '\'' +
            ", serverData=" + serverDataStr +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AVObject)) return false;
    AVObject avObject = (AVObject) o;
    return isFetchWhenSave() == avObject.isFetchWhenSave() &&
            Objects.equals(getClassName(), avObject.getClassName()) &&
            Objects.equals(getObjectId(), avObject.getObjectId()) &&
            Objects.equals(getServerData(), avObject.getServerData()) &&
            Objects.equals(operations, avObject.operations) &&
            Objects.equals(acl, avObject.acl);
  }

  @Override
  public int hashCode() {

    return Objects.hash(getClassName(), getObjectId(), getServerData(), operations, acl, isFetchWhenSave());
  }
}
