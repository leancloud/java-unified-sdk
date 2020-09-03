package cn.leancloud;

import cn.leancloud.core.AppConfiguration;
import cn.leancloud.network.NetworkingDetector;
import cn.leancloud.ops.*;
import cn.leancloud.types.AVDate;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.core.PaasClient;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import cn.leancloud.json.JSONArray;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AVObject {
  public static final String KEY_CREATED_AT = "createdAt";
  public static final String KEY_UPDATED_AT = "updatedAt";
  public static final String KEY_OBJECT_ID = "objectId";
  public static final String KEY_ACL = "ACL";

  public static final String KEY_CLASSNAME = "className";

  public static final String KEY_IGNORE_HOOKS = "__ignore_hooks";

  private static final String INTERNAL_PATTERN = "^[\\da-z][\\d-a-z]*$";
  private static final Set<String> RESERVED_ATTRS = new HashSet<String>(
          Arrays.asList(KEY_CREATED_AT, KEY_UPDATED_AT, KEY_OBJECT_ID, KEY_ACL));

  protected static final AVLogger logger = LogUtil.getLogger(AVObject.class);
  protected static final int UUID_LEN = UUID.randomUUID().toString().length();

  protected String className;
  protected transient String endpointClassName = null;

  protected transient String objectId = "";
  protected ConcurrentMap<String, Object> serverData = new ConcurrentHashMap<String, Object>();
  protected transient ConcurrentMap<String, ObjectFieldOperation> operations = new ConcurrentHashMap<String, ObjectFieldOperation>();
  protected transient AVACL acl = null;
  private transient String uuid = null;

  private volatile boolean fetchWhenSave = false;
  protected volatile boolean totallyOverwrite = false;

  public enum Hook {
    beforeSave, afterSave, beforeUpdate, afterUpdate, beforeDelete, afterDelete,
  }

  private transient Set<Hook> ignoreHooks = new TreeSet<Hook>();

  /**
   * Default constructor.
   */
  public AVObject() {
    this.className = Transformer.getSubClassName(this.getClass());
  }

  /**
   * Constructor with class name.
   * @param className class name.
   */
  public AVObject(String className) {
    Transformer.checkClassName(className);
    this.className = className;
  }

  /**
   * Copy constructor.
   * @param other other instance.
   */
  public AVObject(AVObject other) {
    this.className = other.className;
    this.objectId = other.objectId;
    this.serverData.putAll(other.serverData);
    this.operations.putAll(other.operations);
    this.acl = other.acl;
    this.endpointClassName = other.endpointClassName;
  }

  /**
   * Get class name.
   * @return class name.
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Get internal class name.
   * @return internal class name.
   */
  public String internalClassName() {
    return this.getClassName();
  }

  /**
   * Set class name.
   * @param name class name.
   */
  public void setClassName(String name) {
    Transformer.checkClassName(name);
    this.className = name;
  }

  /**
   * Get createdAt date.
   * @return createdAt date.
   */
  public Date getCreatedAt() {
    String value = getCreatedAtString();
    return StringUtil.dateFromString(value);
  }

  /**
   * Get createdAt string.
   * @return createdAt string.
   */
  public String getCreatedAtString() {
    return (String) this.serverData.get(KEY_CREATED_AT);
  }

  /**
   * Get updatedAt date.
   * @return updatedAt date.
   */
  public Date getUpdatedAt() {
    String value = getUpdatedAtString();
    return StringUtil.dateFromString(value);
  }

  /**
   * Get updatedAt string.
   * @return updatedAt string.
   */
  public String getUpdatedAtString() {
    return (String) this.serverData.get(KEY_UPDATED_AT);
  }

  /**
   * Get objectId.
   * @return objectId.
   */
  public String getObjectId() {
    if (this.serverData.containsKey(KEY_OBJECT_ID)) {
      return (String) this.serverData.get(KEY_OBJECT_ID);
    } else {
      return this.objectId;
    }
  }

  /**
   * Set objectId.
   * @param objectId object id.
   */
  public void setObjectId(String objectId) {
    this.objectId = objectId;
    if (null != this.serverData && !StringUtil.isEmpty(objectId)) {
      this.serverData.put(KEY_OBJECT_ID, objectId);
    }
  }

  /**
   * Flag to fetchWhenSave.
   * @return flag for fetchWhenSave.
   */
  public boolean isFetchWhenSave() {
    return fetchWhenSave;
  }

  /**
   * Set fetchWhenSave flag.
   * @param fetchWhenSave flag.
   */
  public void setFetchWhenSave(boolean fetchWhenSave) {
    this.fetchWhenSave = fetchWhenSave;
  }

  /**
   * Get UUID.
   * @return UUID.
   * Caution: public this method just for compatibility.
   */
  public String getUuid() {
    if (StringUtil.isEmpty(this.uuid)) {
      this.uuid = UUID.randomUUID().toString().toLowerCase();
    }
    return this.uuid;
  }

  void setUuid(String uuid) {
    this.uuid = uuid;
  }

  protected static boolean verifyInternalId(String internalId) {
    return Pattern.matches(INTERNAL_PATTERN, internalId);
  }

  protected String internalId() {
    return StringUtil.isEmpty(getObjectId()) ? getUuid() : getObjectId();
  }

  /**
   * Contain specified key.
   * @param key key
   * @return flag to indicate current object contains the specified key or not.
   */
  public boolean containsKey(String key) {
    return serverData.containsKey(key);
  }

  /**
   * Contain specified key.
   * @param key key
   * @return flag to indicate current object contains the specified key or not.
   */
  public boolean has(String key) {
    return (this.get(key) != null);
  }

  /**
   * Get value of specified key.
   * @param key specified key.
   * @return the value associated with specified key.
   */
  public Object get(String key) {
    Object value = serverData.get(key);
    ObjectFieldOperation op = operations.get(key);
    if (null != op) {
      value = op.apply(value);
    }
    return value;
  }

  /**
   * Get boolean value of specified key.
   * @param key specified key.
   * @return the boolean value associated with specified key.
   */
  public boolean getBoolean(String key) {
    Boolean b = (Boolean) get(key);
    return b == null ? false : b;
  }

  /**
   * Get bytes value of specified key.
   * @param key specified key.
   * @return the bytes value associated with specified key.
   */
  public byte[] getBytes(String key) {
    return (byte[]) (get(key));
  }

  /**
   * Get Date value of specified key.
   * @param key specified key.
   * @return the Date value associated with specified key.
   */
  public Date getDate(String key) {
    Object res = get(key);
    if (res instanceof Date) {
      return (Date)res;
    }
    if (res instanceof Long) {
      return new Date((Long) res);
    }
    if (res instanceof String) {
      return StringUtil.dateFromString((String) res);
    }
    if (res instanceof JSONObject) {
      return new AVDate((JSONObject) res).getDate();
    }
    if (res instanceof Map) {
      JSONObject json = JSONObject.Builder.create((Map) res);
      return new AVDate(json).getDate();
    }
    return null;
  }

  /**
   * Get string value of specified key.
   * @param key specified key.
   * @return the string value associated with specified key.
   */
  public String getString(String key) {
    Object obj = get(key);
    if (obj instanceof String)
      return (String) obj;
    else
      return null;
  }

  /**
   * Get int value of specified key.
   * @param key specified key.
   * @return the int value associated with specified key.
   */
  public int getInt(String key) {
    Number v = (Number) get(key);
    if (v != null) return v.intValue();
    return 0;
  }

  /**
   * Get long value of specified key.
   * @param key specified key.
   * @return the long value associated with specified key.
   */
  public long getLong(String key) {
    Number v = (Number) get(key);
    if (v != null) return v.longValue();
    return 0l;
  }

  /**
   * Get double value of specified key.
   * @param key specified key.
   * @return the double value associated with specified key.
   */
  public double getDouble(String key) {
    Number number = (Number) get(key);
    if (number != null) return number.doubleValue();
    return 0f;
  }

  /**
   * Get numeric value of specified key.
   * @param key specified key.
   * @return the numeric value associated with specified key.
   */
  public Number getNumber(String key) {
    return (Number) get(key);
  }

  /**
   * Get list value of specified key.
   * @param key specified key.
   * @return the list value associated with specified key.
   */
  public List getList(String key) {
    return (List) get(key);
  }

  /**
   * Get jsonarray value of specified key.
   * @param key specified key.
   * @return the jsonarray value associated with specified key.
   */
  public JSONArray getJSONArray(String key) {
    Object list = get(key);
    if (list == null) {
      return null;
    }
    if (list instanceof JSONArray) {
      return (JSONArray) list;
    }
    if (list instanceof List<?>) {
      return JSONArray.Builder.create((List<Object>) list);
    }
    if (list instanceof Object[]) {
      JSONArray array = JSONArray.Builder.create(null);
      for (Object obj : (Object[]) list) {
        array.add(obj);
      }
      return array;
    }
    return null;
  }

  /**
   * Get jsonobject value of specified key.
   * @param key specified key.
   * @return the jsonobject value associated with specified key.
   */
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

  /**
   * Get AVGeoPoint value of specified key.
   * @param key specified key.
   * @return the AVGeoPoint value associated with specified key.
   */
  public AVGeoPoint getAVGeoPoint(String key) {
    return (AVGeoPoint) get(key);
  }

  /**
   * Get AVFile value of specified key.
   * @param key specified key.
   * @return the AVFile value associated with specified key.
   */
  public AVFile getAVFile(String key) {
    return (AVFile) get(key);
  }

  /**
   * Get AVObject value of specified key.
   * @param key specified key.
   * @param <T> template type
   * @return the AVObject value associated with specified key.
   */
  public <T extends AVObject> T getAVObject(String key) {
    try {
      return (T) get(key);
    } catch (Exception ex) {
      logger.w("failed to convert Object.", ex);
      return null;
    }
  }

  /**
   * Get AVRelation value of specified key.
   * @param key specified key.
   * @param <T> template type
   * @return the AVRelation value associated with specified key.
   */
  public <T extends AVObject> AVRelation<T> getRelation(String key) {
    validFieldName(key);
    Object object = get(key);
    if (object instanceof AVRelation) {
      ((AVRelation)object).setParent(this);
      ((AVRelation)object).setKey(key);
      return (AVRelation)object;
    } else {
      return new AVRelation<>(this, key);
    }
  }

  /**
   * Add Relation.
   * @param object target object.
   * @param key specified key.
   */
  void addRelation(final AVObject object, final String key) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.AddRelation, key, object);
    addNewOperation(op);
  }

  /**
   * Remove Relation.
   * @param object target object.
   * @param key specified key.
   */
  void removeRelation(final AVObject object, final String key) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.RemoveRelation, key, object);
    addNewOperation(op);
  }

  /**
   * Get server data.
   * @return map of server data.
   */
  public ConcurrentMap<String, Object> getServerData() {
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

  /**
   * Flag to indicate data is available or not.
   * @return available flag.
   */
  //@JSONField(serialize = false)
  public boolean isDataAvailable() {
    return !StringUtil.isEmpty(this.objectId) && !this.serverData.isEmpty();
  }

  /**
   * changable operations.
   */
  /**
   * Add attribute.
   * @param key target key.
   * @param value value object.
   */
  public void add(String key, Object value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Add, key, value);
    addNewOperation(op);
  }

  /**
   * Add collection attribute.
   * @param key target key.
   * @param values values collection.
   */
  public void addAll(String key, Collection<?> values) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Add, key, values);
    addNewOperation(op);
  }

  /**
   * Add unique attribute.
   * @param key target key.
   * @param value value object.
   */
  public void addUnique(String key, Object value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.AddUnique, key, value);
    addNewOperation(op);
  }

  /**
   * Add unique collection attribute.
   * @param key target key.
   * @param values value collection.
   */
  public void addAllUnique(String key, Collection<?> values) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.AddUnique, key, values);
    addNewOperation(op);
  }

  /**
   * Set attribute.
   * @param key target key.
   * @param value value object.
   */
  public void put(String key, Object value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Set, key, value);
    addNewOperation(op);
  }

  /**
   * Remove attribute.
   * @param key target key.
   */
  public void remove(String key) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Delete, key, null);
    addNewOperation(op);
  }

  /**
   * Remove all collection.
   * @param key target keys.
   * @param values value collection.
   */
  public void removeAll(String key, Collection<?> values) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Remove, key, values);
    addNewOperation(op);
  }

  /**
   * Increment one attribute.
   * @param key target key.
   */
  public void increment(String key) {
    this.increment(key, 1);
  }

  /**
   * Increment one attribute.
   * @param key target key.
   * @param value value object.
   */
  public void increment(String key, Number value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Increment, key, value);
    addNewOperation(op);
  }

  /**
   * Decrement one attribute.
   * @param key target key.
   */
  public void decrement(String key) {
    decrement(key, 1);
  }

  /**
   * Decrement one attribute.
   * @param key target key.
   * @param value value object.
   */
  public void decrement(String key, Number value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Decrement, key, value);
    addNewOperation(op);
  }

  /**
   * Modify integer attribute.
   * @param key target key.
   * @param value value object.
   */
  public void bitAnd(String key, long value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.BitAnd, key, value);
    addNewOperation(op);
  }

  /**
   * Modify integer attribute.
   * @param key target key.
   * @param value value object.
   */
  public void bitOr(String key, long value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.BitOr, key, value);
    addNewOperation(op);
  }

  /**
   * Modify integer attribute.
   * @param key target key.
   * @param value value object.
   */
  public void bitXor(String key, long value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.BitXor, key, value);
    addNewOperation(op);
  }

  /**
   * abort all modify operations.
   * Notice: this method doesn't work for AVInstallation.
   */
  public void abortOperations() {
    if (totallyOverwrite) {
      logger.w("Can't abort modify operations under TotalOverWrite mode.");
    }
    this.operations.clear();
  }

  protected void removeOperationForKey(String key) {
    this.operations.remove(key);
  }

  protected void addNewOperation(ObjectFieldOperation op) {
    if (null == op) {
      return;
    }
    if (totallyOverwrite) {
      if ("Delete".equalsIgnoreCase(op.getOperation())) {
        this.serverData.remove(op.getField());
      } else {
        Object oldValue = this.serverData.get(op.getField());
        Object newValue = op.apply(oldValue);
        if (null == newValue) {
          this.serverData.remove(op.getField());
        } else {
          this.serverData.put(op.getField(), newValue);
        }
      }
    } else {
      ObjectFieldOperation previous = null;
      if (this.operations.containsKey(op.getField())) {
        previous = this.operations.get(op.getField());
      }
      this.operations.put(op.getField(), op.merge(previous));
    }
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
   * @return json object.
   */
  protected JSONObject generateChangedParam() {
    if (totallyOverwrite) {
      HashMap<String, Object> tmp = new HashMap<>();
      for (Map.Entry<String, Object> entry : this.serverData.entrySet()) {
        String key = entry.getKey();
        Object val = entry.getValue();
        tmp.put(key, BaseOperation.encodeObject(val));
      }

      // createdAt, updatedAt, objectId is immutable.
      tmp.remove(KEY_CREATED_AT);
      tmp.remove(KEY_UPDATED_AT);
      tmp.remove(KEY_OBJECT_ID);

      if (ignoreHooks.size() > 0) {
        tmp.put(KEY_IGNORE_HOOKS, ignoreHooks);
      }
 
      return JSONObject.Builder.create(tmp);
    }

    Map<String, Object> params = new HashMap<String, Object>();
    Set<Map.Entry<String, ObjectFieldOperation>> entries = operations.entrySet();
    for (Map.Entry<String, ObjectFieldOperation> entry: entries) {
      //{"attr":{"__op":"Add", "objects":[obj1, obj2]}}
      Map<String, Object> oneOp = entry.getValue().encode();
      params.putAll(oneOp);
    }

    if (null != this.acl) {
      AVACL serverACL = generateACLFromServerData();
      if (!this.acl.equals(serverACL)) {
        // only append acl request when modified.
        ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Set, KEY_ACL, acl);
        params.putAll(op.encode());
      }
    }

    if (ignoreHooks.size() > 0) {
      params.put(KEY_IGNORE_HOOKS, ignoreHooks);
    }

    if (!needBatchMode()) {
      return JSONObject.Builder.create(params);
    }

    List<Map<String, Object>> finalParams = new ArrayList<Map<String, Object>>();
    Map<String, Object> topParams = Utils.makeCompletedRequest(getObjectId(), getRequestRawEndpoint(), getRequestMethod(), params);
    if (null != topParams) {
      finalParams.add(topParams);
    }

    for (ObjectFieldOperation ops : this.operations.values()) {
      if (ops instanceof CompoundOperation) {
        List<Map<String, Object>> restParams = ((CompoundOperation)ops).encodeRestOp(this);
        if (null != restParams && !restParams.isEmpty()) {
          finalParams.addAll(restParams);
        }
      }
    }
    Map<String, Object> finalResult = new HashMap<String, Object>(1);
    finalResult.put("requests", finalParams);

    return JSONObject.Builder.create(finalResult);
  }

  protected List<AVObject> extractCascadingObjects(Object o) {
    List<AVObject> result = new ArrayList<>();
    if (o instanceof AVObject && StringUtil.isEmpty(((AVObject)o).getObjectId())) {
      result.add((AVObject) o);
    } else if (o instanceof Collection) {
      for (Object secondO: ((Collection)o).toArray()) {
        List<AVObject> tmp = extractCascadingObjects(secondO);
        if (null != tmp && !tmp.isEmpty()) {
          result.addAll(tmp);
        }
      }
    }
    return result;
  }

  protected Observable<List<AVObject>> generateCascadingSaveObjects() {
    List<AVObject> result = new ArrayList<>();
    for (ObjectFieldOperation ofo: operations.values()) {
      List<AVObject> operationValues = extractCascadingObjects(ofo.getValue());
      if (null != operationValues && !operationValues.isEmpty()) {
        result.addAll(operationValues);
      }
    }
    return Observable.just(result).subscribeOn(Schedulers.io());
  }

  protected List<AVFile> extractUnsavedFiles(Object o) {
    List<AVFile> result = new ArrayList<>();
    if (o instanceof AVFile && StringUtil.isEmpty(((AVFile) o).getObjectId())) {
      result.add((AVFile) o);
    } else if (o instanceof Collection) {
      for (Object secondTmp: ((Collection)o).toArray()) {
        List<AVFile> tmp = extractUnsavedFiles(secondTmp);
        if (null != tmp && !tmp.isEmpty()) {
          result.addAll(tmp);
        }
      }
    }
    return result;
  }

  protected List<AVFile> getUnsavedFiles() {
    List<AVFile> result = new ArrayList<>();
    for (ObjectFieldOperation ofo: operations.values()) {
      List<AVFile> unsavedFiles = extractUnsavedFiles(ofo.getValue());
      if (null != unsavedFiles && !unsavedFiles.isEmpty()) {
        result.addAll(unsavedFiles);
      }
    }
    return result;
  }

  protected void onSaveSuccess() {
    this.operations.clear();
  }

  protected void onSaveFailure() {
  }

  protected void onDataSynchronized() {
  }

  private Observable<? extends AVObject> saveSelfOperations(AVSaveOption option) {
    final boolean needFetch = (null != option) ? option.fetchWhenSave : isFetchWhenSave();

    if (null != option && null != option.matchQuery) {
      String currentClass = getClassName();
      if (!StringUtil.isEmpty(currentClass) && !currentClass.equals(option.matchQuery.getClassName())) {
        return Observable.error(new AVException(0, "AVObject class inconsistant with AVQuery in AVSaveOption"));
      }
    }

    final JSONObject paramData = generateChangedParam();
    logger.d("saveObject param: " + paramData.toJSONString());

    final String currentObjectId = getObjectId();

    if (needBatchMode()) {
      logger.w("Caution: batch mode will ignore fetchWhenSave flag and matchQuery.");
      if (StringUtil.isEmpty(currentObjectId)) {
        logger.d("request payload: " + paramData.toJSONString());
        return PaasClient.getStorageClient().batchSave(paramData).map(new Function<List<Map<String, Object>>, AVObject>() {
          public AVObject apply(List<Map<String, Object>> object) throws Exception {
            if (null != object && !object.isEmpty()) {
              logger.d("batchSave result: " + object.toString());

              Map<String, Object> lastResult = object.get(object.size() - 1);
              if (null != lastResult) {
                AVUtils.mergeConcurrentMap(serverData, lastResult);
                AVObject.this.onSaveSuccess();
              }
            }
            return AVObject.this;
          }
        });
      } else {
        return PaasClient.getStorageClient().batchUpdate(paramData).map(new Function<JSONObject, AVObject>() {
          public AVObject apply(JSONObject object) throws Exception {
            if (null != object) {
              logger.d("batchUpdate result: " + object.toJSONString());
              Map<String, Object> lastResult = object.getObject(currentObjectId, Map.class);
              if (null != lastResult) {
                AVUtils.mergeConcurrentMap(serverData, lastResult);
                AVObject.this.onSaveSuccess();
              }
            }
            return AVObject.this;
          }
        });
      }
    } else {
      JSONObject whereCondition = null;
      if (null != option && null != option.matchQuery) {
        Map<String, Object> whereConditionMap = option.matchQuery.conditions.compileWhereOperationMap();
        whereCondition = JSONObject.Builder.create(whereConditionMap);
      }
      if (totallyOverwrite) {
        return PaasClient.getStorageClient().saveWholeObject(this.getClass(), endpointClassName, currentObjectId,
                paramData, needFetch, whereCondition)
                .map(new Function<AVObject, AVObject>() {
          @Override
          public AVObject apply(AVObject avObject) throws Exception {
            AVObject.this.mergeRawData(avObject, needFetch);
            AVObject.this.onSaveSuccess();
            return AVObject.this;
          }
        });
      } else if (StringUtil.isEmpty(currentObjectId)) {
        return PaasClient.getStorageClient().createObject(this.className, paramData, needFetch, whereCondition)
                .map(new Function<AVObject, AVObject>() {
                  @Override
                  public AVObject apply(AVObject avObject) throws Exception {
                    AVObject.this.mergeRawData(avObject, needFetch);
                    AVObject.this.onSaveSuccess();
                    return AVObject.this;
                  }
                });
      } else {
        return PaasClient.getStorageClient().saveObject(this.className, getObjectId(), paramData, needFetch, whereCondition)
                .map(new Function<AVObject, AVObject>() {
                  @Override
                  public AVObject apply(AVObject avObject) throws Exception {
                    AVObject.this.mergeRawData(avObject, needFetch);
                    AVObject.this.onSaveSuccess();
                    return AVObject.this;
                  }
                });
      }
    }
  }

  /**
   * Save object in background.
   * @return observable instance.
   */
  public Observable<? extends AVObject> saveInBackground() {
    AVSaveOption option = null;
    if (totallyOverwrite) {
      option = new AVSaveOption();
      option.setFetchWhenSave(true);
    }
    return saveInBackground(option);
  }

  /**
   * Save object in background.
   * @param option save option.
   * @return observable instance.
   */
  public Observable<? extends AVObject> saveInBackground(final AVSaveOption option) {
    Map<AVObject, Boolean> markMap = new HashMap<>();
    if (hasCircleReference(markMap)) {
      return Observable.error(new AVException(AVException.CIRCLE_REFERENCE, "Found a circular dependency when saving."));
    }

    Observable<List<AVObject>> needSaveFirstly = generateCascadingSaveObjects();
    return needSaveFirstly.flatMap(new Function<List<AVObject>, Observable<? extends AVObject>>() {
      @Override
      public Observable<? extends AVObject> apply(List<AVObject> objects) throws Exception {
        logger.d("First, try to execute save operations in thread: " + Thread.currentThread());
        for (AVObject o: objects) {
          o.save();
        }
        logger.d("Second, save object itself...");
        return saveSelfOperations(option);
      }
    });
  }

  /**
   * judge operations' value include circle reference or not.
   *
   * notice: internal used, pls not invoke it.
   *
   * @param markMap markup map.
   * @return flag to indicate there is circle reference or not.
   */
  public boolean hasCircleReference(Map<AVObject, Boolean> markMap) {
    if (null == markMap) {
      return false;
    }
    markMap.put(this, true);
    boolean rst = false;
    for (ObjectFieldOperation op: operations.values()) {
      rst = rst || op.checkCircleReference(markMap);
    }
    return rst;
  }

  /**
   * Save in blocking mode.
   */
  public void save() {
    saveInBackground().blockingSubscribe();
  }

  /**
   * Save All objects in blocking mode.
   * @param objects object collection.
   * @throws AVException error happened.
   */
  public static void saveAll(Collection<? extends AVObject> objects) throws AVException {
    saveAllInBackground(objects).blockingSubscribe();
  }

  private static Observable<List<AVFile>> extractSaveAheadFiles(Collection<? extends AVObject> objects) {
    List<AVFile> needSaveAheadFiles = new ArrayList<>();
    for (AVObject o: objects) {
      List<AVFile> cascadingSaveFiles = o.getUnsavedFiles();
      if (null != cascadingSaveFiles && !cascadingSaveFiles.isEmpty()) {
        needSaveAheadFiles.addAll(cascadingSaveFiles);
      }
    }
    return Observable.just(needSaveAheadFiles).subscribeOn(Schedulers.io());
  }

  /**
   * Save all objects in async mode.
   * @param objects object collection.
   * @return observable instance.
   */
  public static Observable<JSONArray> saveAllInBackground(final Collection<? extends AVObject> objects) {
    if (null == objects || objects.isEmpty()) {
      JSONArray emptyResult = JSONArray.Builder.create(null);
      return Observable.just(emptyResult);
    }
    for (AVObject o: objects) {
      Map<AVObject, Boolean> markMap = new HashMap<>();
      if (o.hasCircleReference(markMap)) {
        return Observable.error(new AVException(AVException.CIRCLE_REFERENCE, "Found a circular dependency when saving."));
      }
    }
    Observable<List<AVFile>> aHeadStage = extractSaveAheadFiles(objects);
    return aHeadStage.flatMap(new Function<List<AVFile>, ObservableSource<JSONArray>>() {
      @Override
      public ObservableSource<JSONArray> apply(List<AVFile> avFiles) throws Exception {
        logger.d("begin to save objects with batch mode...");
        if (null != avFiles && !avFiles.isEmpty()) {
          for (AVFile file : avFiles) {
            file.save();
          }
        }
        JSONArray requests = JSONArray.Builder.create(null);
        for (AVObject o : objects) {
          JSONObject requestBody = o.generateChangedParam();
          JSONObject objectRequest = JSONObject.Builder.create(null);
          objectRequest.put("method", o.getRequestMethod());
          objectRequest.put("path", o.getRequestRawEndpoint());
          objectRequest.put("body", requestBody);
          requests.add(objectRequest);
        }

        JSONObject requestTotal = JSONObject.Builder.create(null);
        requestTotal.put("requests", requests);
        return PaasClient.getStorageClient().batchSave(requestTotal).map(new Function<List<Map<String, Object>>, JSONArray>() {
          public JSONArray apply(List<Map<String, Object>> batchResults) throws Exception {

            JSONArray result = JSONArray.Builder.create(null);
            if (null != batchResults && (objects.size() == batchResults.size())) {
              logger.d("batchSave result: " + batchResults.toString());
              Iterator it = objects.iterator();

              for (int i = 0; i < batchResults.size() && it.hasNext(); i++) {
                JSONObject oneResult = JSONObject.Builder.create(batchResults.get(i));
                AVObject originObject = (AVObject) it.next();
                if (oneResult.containsKey("success")) {
                  AVUtils.mergeConcurrentMap(originObject.serverData, oneResult.getJSONObject("success").getInnerMap());
                  originObject.onSaveSuccess();
                } else if (oneResult.containsKey("error")) {
                  originObject.onSaveFailure();
                }
                result.add(oneResult);
              }
            }
            return result;
          }
        });
      }
    });
  }

  /**
   * Save eventually.
   * @throws AVException error happened.
   */
  public void saveEventually() throws AVException {
    if (operations.isEmpty()) {
      return;
    }
    Map<AVObject, Boolean> markMap = new HashMap<>();
    if (hasCircleReference(markMap)) {
      throw new AVException(AVException.CIRCLE_REFERENCE, "Found a circular dependency when saving.");
    }

    NetworkingDetector detector = AppConfiguration.getGlobalNetworkingDetector();
    if (null != detector && detector.isConnected()) {
      // network is fine, try to save object;
      this.saveInBackground().subscribe(new Observer<AVObject>() {
        @Override
        public void onSubscribe(Disposable disposable) {

        }

        @Override
        public void onNext(AVObject avObject) {
          logger.d("succeed to save directly");
        }

        @Override
        public void onError(Throwable throwable) {
          // failed, save data to local file first;
          add2ArchivedRequest(false);
        }

        @Override
        public void onComplete() {

        }
      });
    } else {
      // network down, save data to local file first;
      add2ArchivedRequest(false);
    }
  }

  private void add2ArchivedRequest(boolean isDelete) {
    ArchivedRequests requests = ArchivedRequests.getInstance();
    if (isDelete) {
      requests.deleteEventually(this);
    } else {
      requests.saveEventually(this);
    }
  }

  /**
   * Delete current object eventually.
   */
  public void deleteEventually() {
    String objectId  = getObjectId();
    if (StringUtil.isEmpty(objectId)) {
      logger.w("objectId is empty, you couldn't delete a persistent object.");
      return;
    }
    NetworkingDetector detector = AppConfiguration.getGlobalNetworkingDetector();
    if (null != detector && detector.isConnected()) {
      this.deleteInBackground().subscribe(new Observer<AVNull>() {
        @Override
        public void onSubscribe(Disposable disposable) {

        }

        @Override
        public void onNext(AVNull avNull) {
          logger.d("succeed to delete directly.");
        }

        @Override
        public void onError(Throwable throwable) {
          add2ArchivedRequest(true);
        }

        @Override
        public void onComplete() {

        }
      });
    } else {
      add2ArchivedRequest(true);
    }
  }

  /**
   * Delete current object in async mode.
   * @return observable instance.
   */
  public Observable<AVNull> deleteInBackground() {
    Map<String, Object> ignoreParam = new HashMap<>();
    if (ignoreHooks.size() > 0) {
      ignoreParam.put(KEY_IGNORE_HOOKS, ignoreHooks);
    }
    if (totallyOverwrite) {
      return PaasClient.getStorageClient().deleteWholeObject(this.endpointClassName, getObjectId(), ignoreParam);
    }
    return PaasClient.getStorageClient().deleteObject(this.className, getObjectId(), ignoreParam);
  }

  /**
   * Delete current object in blocking mode.
   */
  public void delete() {
    deleteInBackground().blockingSubscribe();
  }

  /**
   * Delete all objects in blocking mode.
   * @param objects object collection.
   * @throws AVException error happened.
   */
  public static void deleteAll(Collection<? extends AVObject> objects) throws AVException {
    deleteAllInBackground(objects).blockingSubscribe();
  }

  /**
   * Delete all objects in async mode.
   * @param objects object collection.
   * @return observable instance.
   */
  public static Observable<AVNull> deleteAllInBackground(Collection<? extends AVObject> objects) {
    if (null == objects || objects.isEmpty()) {
      return Observable.just(AVNull.getINSTANCE());
    }
    String className = null;
    Map<String, Object> ignoreParams = new HashMap<>();
    StringBuilder sb = new StringBuilder();
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
    return PaasClient.getStorageClient().deleteObject(className, sb.toString(), ignoreParams);
  }

  /**
   * Refresh current object in blocking mode.
   */
  public void refresh() {
    refresh(null);
  }

  /**
   * Refresh current object in blocking mode.
   * @param includeKeys include keys, which object will be return together.
   */
  public void refresh(String includeKeys) {
    refreshInBackground(includeKeys).blockingSubscribe();
  }

  /**
   * Refresh current object in async mode.
   * @return observable instance.
   */
  public Observable<AVObject> refreshInBackground() {
    return refreshInBackground(null);
  }

  /**
   * Refresh current object in async mode.
   * @param includeKeys include keys, which object will be return together.
   * @return observable instance.
   */
  public Observable<AVObject> refreshInBackground(final String includeKeys) {
    if (totallyOverwrite) {
      return PaasClient.getStorageClient().getWholeObject(this.endpointClassName, getObjectId(), includeKeys)
              .map(new Function<AVObject, AVObject>() {
                @Override
                public AVObject apply(AVObject avObject) throws Exception {
                  AVObject.this.serverData.clear();
                  AVObject.this.serverData.putAll(avObject.serverData);
                  AVObject.this.onDataSynchronized();
                  return AVObject.this;
                }
              });
    }
    return PaasClient.getStorageClient().fetchObject(this.className, getObjectId(), includeKeys)
            .map(new Function<AVObject, AVObject>() {
              public AVObject apply(AVObject avObject) throws Exception {
                if (StringUtil.isEmpty(includeKeys)) {
                  if (className.equals(AVUser.CLASS_NAME) || AVObject.this instanceof AVUser) {
                    Object userSessionToken = AVObject.this.serverData.get(AVUser.ATTR_SESSION_TOKEN);
                    AVObject.this.serverData.clear();
                    if (null != userSessionToken){
                      AVObject.this.serverData.put(AVUser.ATTR_SESSION_TOKEN, userSessionToken);
                    }
                  } else {
                    AVObject.this.serverData.clear();
                  }
                }
                AVObject.this.serverData.putAll(avObject.serverData);
                AVObject.this.onDataSynchronized();
                return AVObject.this;
              }
            });
  }

  /**
   * Fetch current object in blocking mode.
   * @return current object.
   */
  public AVObject fetch() {
    return fetch(null);
  }

  /**
   * Fetch current object in blocking mode.
   * @param includeKeys include keys, which object will be return together.
   * @return current object.
   */
  public AVObject fetch(String includeKeys) {
    refresh(includeKeys);
    return this;
  }

  /**
   * Fetch current object in async mode.
   * @return observable instance.
   */
  public Observable<AVObject> fetchInBackground() {
    return refreshInBackground();
  }

  /**
   * Fetch current object in async mode.
   * @param includeKeys include keys, which object will be return together.
   * @return observable instance.
   */
  public Observable<AVObject> fetchInBackground(String includeKeys) {
    return refreshInBackground(includeKeys);
  }

  /**
   * Fetch current object if needed in async mode.
   * @return observable instance.
   */
  public Observable<AVObject> fetchIfNeededInBackground() {
    if (!StringUtil.isEmpty(getObjectId()) && this.serverData.size() > 1) {
      return Observable.just(this);
    } else {
      return refreshInBackground();
    }
  }

  /**
   * Fetch current object if needed in async mode.
   * @param includeKeys include keys, which object will be return together.
   * @return observable instance.
   */
  public Observable<AVObject> fetchIfNeededInBackground(String includeKeys) {
    if (!StringUtil.isEmpty(getObjectId()) && this.serverData.size() > 1) {
      return Observable.just(this);
    } else {
      return refreshInBackground(includeKeys);
    }
  }

  /**
   * Fetch current object in blocking mode.
   * @return current object.
   */
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
      this.operations.putAll(avObject.operations);
    }
  }

  void mergeRawData(AVObject avObject, boolean fetchServerData) {
    if (null != avObject) {
      this.serverData.putAll(avObject.serverData);
    }
    if (!fetchServerData && AppConfiguration.isAutoMergeOperationDataWhenSave()) {
      for (Map.Entry<String, ObjectFieldOperation> entry: operations.entrySet()) {
        String attribute = entry.getKey();
        Object value = this.get(attribute);
        if (null == value) {
          this.serverData.remove(attribute);
        } else {
          this.serverData.put(attribute, value);
        }
      }
    }
  }

  /**
   * Reset server data with new data.
   * @param data new data.
   */
  public void resetServerData(Map<String, Object> data) {
    this.serverData.clear();
    AVUtils.mergeConcurrentMap(this.serverData, data);
    this.operations.clear();
  }

  /**
   * Get request endpoint.
   * @return endpoint.
   */
  //@JSONField(serialize = false)
  public String getRequestRawEndpoint() {
    if (StringUtil.isEmpty(getObjectId())) {
      return "/1.1/classes/" + this.getClassName();
    } else {
      return "/1.1/classes/" + this.getClassName() + "/" + getObjectId();
    }
  }

  /**
   * Get request method.
   * @return http method.
   */
  //@JSONField(serialize = false)
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
   * @param <T> template type.
   * @since 1.3.6
   */
  public static <T extends AVObject> void registerSubclass(Class<T> clazz) {
    Transformer.registerClass(clazz);
  }

  /**
   * get ACL
   * @return acl object.
   */
  public synchronized AVACL getACL() {
    if (null == this.acl) {
      this.acl = generateACLFromServerData();
    }
    return this.acl;
  }

  /**
   * Set ACL
   * @param acl acl object.
   */
  public synchronized void setACL(AVACL acl) {
    this.acl = acl;
  }

  protected AVACL generateACLFromServerData() {
    if (!this.serverData.containsKey(KEY_ACL)) {
      return new AVACL();
    } else {
      Object aclMap = this.serverData.get(KEY_ACL);
      if (aclMap instanceof HashMap) {
        return new AVACL((HashMap) aclMap);
      } else {
        return new AVACL();
      }
    }
  }

  /**
   * Get query for class.
   * @param clazz target class.
   * @param <T> result type.
   * @return query instance.
   */

  public static <T extends AVObject> AVQuery<T> getQuery(Class<T> clazz) {
    return new AVQuery<T>(Transformer.getSubClassName(clazz), clazz);
  }

  /**
   * common methods.
   */
  /**
   * Generate a new json object with server data.
   * @return json object.
   */
  public JSONObject toJSONObject() {
    return JSONObject.Builder.create(this.serverData);
  }

  /**
   * Generate a json string.
   * @return json string.
   */
  public String toJSONString() {
    return JSON.toJSONString(this);
  }

  /**
   * Create AVObject instance from json string which generated by AVObject.toString or AVObject.toJSONString.
   *
   * @param objectString json string.
   * @return AVObject instance, null if objectString is null
   */
  public static AVObject parseAVObject(String objectString) {
    if (StringUtil.isEmpty(objectString)) {
      return null;
    }
    // replace leading type name to compatible with v4.x android sdk serialized json string.
    objectString = objectString.replaceAll("^\\{\\s*\"@type\":\\s*\"[A-Za-z\\.]+\",", "{");
//    objectString = objectString.replaceAll("^\\{\\s*\"@type\":\\s*\"cn.leancloud.AV(Object|Installation|User|Status|Role|File)\",", "{");

    // replace old AVObject type name.
    objectString = objectString.replaceAll("\"@type\":\\s*\"com.avos.avoscloud.AVObject\",", "\"@type\":\"cn.leancloud.AVObject\",");
    objectString = objectString.replaceAll("\"@type\":\\s*\"com.avos.avoscloud.AVInstallation\",", "\"@type\":\"cn.leancloud.AVInstallation\",");
    objectString = objectString.replaceAll("\"@type\":\\s*\"com.avos.avoscloud.AVUser\",", "\"@type\":\"cn.leancloud.AVUser\",");
    objectString = objectString.replaceAll("\"@type\":\\s*\"com.avos.avoscloud.AVStatus\",", "\"@type\":\"cn.leancloud.AVStatus\",");
    objectString = objectString.replaceAll("\"@type\":\\s*\"com.avos.avoscloud.AVRole\",", "\"@type\":\"cn.leancloud.AVRole\",");
    objectString = objectString.replaceAll("\"@type\":\\s*\"com.avos.avoscloud.AVFile\",", "\"@type\":\"cn.leancloud.AVFile\",");

    objectString = objectString.replaceAll("\"@type\":\\s*\"com.avos.avoscloud.ops.[A-Za-z]+Op\",", "");

    objectString = StringUtil.replaceFastjsonDateForm(objectString);
    return JSON.parseObject(objectString, AVObject.class);
  }

  /**
   * Create a new instance with particular classname and objectId.
   * @param className class name
   * @param objectId  object id
   * @return AVObject instance
   */
  public static AVObject createWithoutData(String className, String objectId) {
    AVObject object = new AVObject(className);
    object.setObjectId(objectId);
    return object;
  }

  /**
   * Create a new instance with particular class and objectId.
   * @param clazz     class info
   * @param objectId  object id
   * @param <T> template type.
   * @return AVObject instance
   * @throws AVException error happened.
   */
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

  /**
   * disable beforeXXX Hooks
   */
  public void disableBeforeHook() {
    Collections.addAll(ignoreHooks, Hook.beforeSave, Hook.beforeUpdate, Hook.beforeDelete);
  }

  /**
   * disable afterXXX Hooks
   */
  public void disableAfterHook() {
    Collections.addAll(ignoreHooks, Hook.afterSave, Hook.afterUpdate, Hook.afterDelete);
  }

  /**
   * ignore specified Hook
   * @param hook target Hook.
   */
  public void ignoreHook(Hook hook) {
    ignoreHooks.add(hook);
  }

  protected static <T extends AVObject> T cast(AVObject object, Class<T> clazz) throws Exception {
    if (clazz.getClass().isAssignableFrom(object.getClass())) {
      return (T) object;
    } else {
      T newItem = clazz.newInstance();
      newItem.className = object.className;
      newItem.objectId = object.objectId;
      newItem.serverData.putAll(object.serverData);
      newItem.operations.putAll(object.operations);
      newItem.acl = object.acl;
      newItem.endpointClassName = object.endpointClassName;
      return newItem;
    }
  }

  @Override
  public String toString() {
    return toJSONString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AVObject)) return false;
    AVObject avObject = (AVObject) o;
    return isFetchWhenSave() == avObject.isFetchWhenSave() &&
            Objects.equals(getClassName(), avObject.getClassName()) &&
            Objects.equals(getServerData(), avObject.getServerData()) &&
            Objects.equals(operations, avObject.operations) &&
            Objects.equals(acl, avObject.acl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClassName(), getServerData(), operations, acl, isFetchWhenSave());
  }
}
