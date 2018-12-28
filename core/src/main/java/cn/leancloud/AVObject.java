package cn.leancloud;

import cn.leancloud.core.AppConfiguration;
import cn.leancloud.network.NetworkingDetector;
import cn.leancloud.ops.*;
import cn.leancloud.types.AVDate;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.core.PaasClient;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.alibaba.fastjson.parser.Feature;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

@JSONType(deserializer = ObjectTypeAdapter.class, serializer = ObjectTypeAdapter.class)
public class AVObject {
  public static final String KEY_CREATED_AT = "createdAt";
  public static final String KEY_UPDATED_AT = "updatedAt";
  public static final String KEY_OBJECT_ID = "objectId";
  public static final String KEY_ACL = "ACL";

  public static final String KEY_CLASSNAME = "className";

  private static final String INTERNAL_PATTERN = "^[\\da-z][\\d-a-z]*$";
  private static final Set<String> RESERVED_ATTRS = new HashSet<String>(
          Arrays.asList(KEY_CREATED_AT, KEY_UPDATED_AT, KEY_OBJECT_ID, KEY_ACL));

  protected static final AVLogger logger = LogUtil.getLogger(AVObject.class);
  protected static final int UUID_LEN = UUID.randomUUID().toString().length();

  protected String className;
  protected String endpointClassName = null;

  protected String objectId = "";
  protected Map<String, Object> serverData = new ConcurrentHashMap<String, Object>();
  protected Map<String, ObjectFieldOperation> operations = new ConcurrentHashMap<String, ObjectFieldOperation>();
  protected AVACL acl = null;
  private String uuid = null;

  @JSONField(serialize = false)
  private volatile boolean fetchWhenSave = false;
  protected volatile boolean totallyOverwrite = false;

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
    this.endpointClassName = other.endpointClassName;
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
    if (null != this.serverData) {
      this.serverData.put(KEY_OBJECT_ID, objectId);
    }
  }

  public boolean isFetchWhenSave() {
    return fetchWhenSave;
  }

  public void setFetchWhenSave(boolean fetchWhenSave) {
    this.fetchWhenSave = fetchWhenSave;
  }

  // Caution: public this method just for compatibility.
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
    return date.getDate();
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
    return (Number) get(key);
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
      return new JSONArray((List<Object>) list);
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
      logger.w("failed to convert Object.", ex);
      return null;
    }
  }

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

  void addRelation(final AVObject object, final String key) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.AddRelation, key, object);
    addNewOperation(op);
  }

  void removeRelation(final AVObject object, final String key) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.RemoveRelation, key, object);
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

  @JSONField(serialize = false)
  public boolean isDataAvailable() {
    return !StringUtil.isEmpty(this.objectId) && !this.serverData.isEmpty();
  }

  /**
   * changable operations.
   */
  public void add(String key, Object value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Add, key, value);
    addNewOperation(op);
  }
  public void addAll(String key, Collection<?> values) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Add, key, values);
    addNewOperation(op);
  }

  public void addUnique(String key, Object value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.AddUnique, key, value);
    addNewOperation(op);
  }
  public void addAllUnique(String key, Collection<?> values) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.AddUnique, key, values);
    addNewOperation(op);
  }

  public void put(String key, Object value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Set, key, value);
    addNewOperation(op);
  }

  public void remove(String key) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Delete, key, null);
    addNewOperation(op);
  }

  public void removeAll(String key, Collection<?> values) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Remove, key, values);
    addNewOperation(op);
  }

  public void increment(String key) {
    this.increment(key, 1);
  }
  public void increment(String key, Number value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Increment, key, value);
    addNewOperation(op);
  }

  public void decrement(String key) {
    decrement(key, 1);
  }
  public void decrement(String key, Number value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Decrement, key, value);
    addNewOperation(op);
  }

  public void bitAnd(String key, long value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.BitAnd, key, value);
    addNewOperation(op);
  }
  public void bitOr(String key, long value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.BitOr, key, value);
    addNewOperation(op);
  }
  public void bitXor(String key, long value) {
    validFieldName(key);
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.BitXor, key, value);
    addNewOperation(op);
  }

  protected void addNewOperation(ObjectFieldOperation op) {
    if (null == op) {
      return;
    }
    if (totallyOverwrite) {
      Object oldValue = this.serverData.get(op.getField());
      Object newValue = op.apply(oldValue);
      this.serverData.put(op.getField(), newValue);
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
   */
  protected JSONObject generateChangedParam() {
    if (totallyOverwrite) {
      HashMap<String, Object> tmp = new HashMap<>();
      tmp.putAll(this.serverData);

      // createdAt, updatedAt, objectId is unnecessary.
      tmp.remove(KEY_CREATED_AT);
      tmp.remove(KEY_UPDATED_AT);
      tmp.remove(KEY_OBJECT_ID);
      return new JSONObject(tmp);
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

    if (!needBatchMode()) {
      return new JSONObject(params);
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

    return new JSONObject(finalResult);
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

  protected Observable<List<AVObject>> getCascadingSaveObjects() {
    List<AVObject> result = new ArrayList<>();
    for (ObjectFieldOperation ofo: operations.values()) {
      List<AVObject> operationValues = extractCascadingObjects(ofo.getValue());
      if (null != operationValues && !operationValues.isEmpty()) {
        result.addAll(operationValues);
      }
    }
    return Observable.just(result).subscribeOn(Schedulers.io());
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
    logger.i("saveObject param: " + paramData.toJSONString());

    final String currentObjectId = getObjectId();

    if (needBatchMode()) {
      logger.w("Caution: batch mode will ignore fetchWhenSave flag and matchQuery.");
      if (StringUtil.isEmpty(currentObjectId)) {
        logger.d("request payload: " + paramData.toJSONString());
        return PaasClient.getStorageClient().batchSave(paramData).map(new Function<JSONArray, AVObject>() {
          public AVObject apply(JSONArray object) throws Exception {
            if (null != object && !object.isEmpty()) {
              logger.d("batchSave result: " + object.toJSONString());

              Map<String, Object> lastResult = object.getObject(object.size() - 1, Map.class);
              if (null != lastResult) {
                AVObject.this.serverData.putAll(lastResult);
                AVObject.this.operations.clear();
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
                AVObject.this.serverData.putAll(lastResult);
                AVObject.this.operations.clear();
              }
            }
            return AVObject.this;
          }
        });
      }
    } else {
      JSONObject whereCondition = null;
      if (null != option && null != option.matchQuery) {
        Map<String, Object> whereOperationMap = option.matchQuery.conditions.compileWhereOperationMap();
        whereCondition = new JSONObject(whereOperationMap);
      }
      if (totallyOverwrite) {
        return PaasClient.getStorageClient().saveWholeObject(this.getClass(), endpointClassName, currentObjectId,
                paramData, needFetch, whereCondition);
      } else if (StringUtil.isEmpty(currentObjectId)) {
        return PaasClient.getStorageClient().createObject(this.className, paramData, needFetch, whereCondition)
                .map(new Function<AVObject, AVObject>() {
                  @Override
                  public AVObject apply(AVObject avObject) throws Exception {
                    AVObject.this.mergeRawData(avObject);
                    return AVObject.this;
                  }
//                }).doOnError(new Consumer<Throwable>() {
//                  @Override
//                  public void accept(Throwable throwable) throws Exception {
//                    if (throwable instanceof HttpException) {
//                      HttpException httpException = (HttpException) throwable;
//                      throw new AVException(httpException.code(), httpException.message());
//                    } else {
//                      throw new AVException(throwable);
//                    }
//                  }
                });
      } else {
        return PaasClient.getStorageClient().saveObject(this.className, getObjectId(), paramData, needFetch, whereCondition)
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

  public Observable<? extends AVObject> saveInBackground() {
    return saveInBackground(null);
  }

  public Observable<? extends AVObject> saveInBackground(final AVSaveOption option) {
    Map<AVObject, Boolean> markMap = new HashMap<>();
    if (hasCircleReference(markMap)) {
      return Observable.error(new AVException(AVException.CIRCLE_REFERENCE, "Found a circular dependency when saving."));
    }

    Observable<List<AVObject>> needSaveFirstly = getCascadingSaveObjects();
    return needSaveFirstly.to(new Function<Observable<List<AVObject>>, Observable<? extends AVObject>>() {
      @Override
      public Observable<? extends AVObject> apply(Observable<List<AVObject>> avNullObservable) throws Exception {
        for (AVObject o: avNullObservable.blockingLast()) {
          o.save();
        }
        logger.d("secondly, save object itself...");
        return saveSelfOperations(option);
      }
    });
  }

  /**
   * judge operations' value include circle reference or not.
   *
   * notice: internal used, pls not invoke it.
   *
   * @param markMap
   * @return
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

  public void save() {
    saveInBackground().blockingSubscribe();
  }

  public static void saveAll(Collection<? extends AVObject> objects) throws AVException {
    saveAllInBackground(objects).blockingSubscribe();
  }

  public static Observable<JSONArray> saveAllInBackground(final Collection<? extends AVObject> objects) {
    if (null == objects || objects.isEmpty()) {
      JSONArray emptyResult = new JSONArray();
      return Observable.just(emptyResult);
    }
    JSONArray requests = new JSONArray();
    for (AVObject o : objects) {
      Map<AVObject, Boolean> markMap = new HashMap<>();
      if (o.hasCircleReference(markMap)) {
        return Observable.error(new AVException(AVException.CIRCLE_REFERENCE, "Found a circular dependency when saving."));
      }
      JSONObject requestBody = o.generateChangedParam();
      JSONObject objectRequest = new JSONObject();
      objectRequest.put("method", o.getRequestMethod());
      objectRequest.put("path", o.getRequestRawEndpoint());
      objectRequest.put("body", requestBody);
      requests.add(objectRequest);
    }

    JSONObject requestTotal = new JSONObject();
    requestTotal.put("requests", requests);
    return PaasClient.getStorageClient().batchSave(requestTotal).map(new Function<JSONArray, JSONArray>() {
      public JSONArray apply(JSONArray batchResults) throws Exception {

        if (null != batchResults && (objects.size() == batchResults.size())) {
          logger.d("batchSave result: " + batchResults.toJSONString());
          Iterator it = objects.iterator();

          for (int i = 0; i < batchResults.size() && it.hasNext(); i++) {
            JSONObject oneResult = batchResults.getJSONObject(i);
            AVObject originObject = (AVObject) it.next();
            if (oneResult.containsKey("success")) {
              originObject.serverData.putAll(oneResult.getJSONObject("success"));
              originObject.operations.clear();
            } else if (oneResult.containsKey("error")) {
              ;
            }
          }
        }
        return batchResults;
      }
    });
  }

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
  public Observable<AVNull> deleteInBackground() {
    if (totallyOverwrite) {
      return PaasClient.getStorageClient().deleteWholeObject(this.endpointClassName, getObjectId());
    }
    return PaasClient.getStorageClient().deleteObject(this.className, getObjectId());
  }

  public void delete() {
    deleteInBackground().blockingSubscribe();
  }

  public static void deleteAll(Collection<? extends AVObject> objects) throws AVException {
    deleteAllInBackground(objects).blockingSubscribe();
  }

  public static Observable<AVNull> deleteAllInBackground(Collection<? extends AVObject> objects) {
    if (null == objects || objects.isEmpty()) {
      return Observable.just(AVNull.getINSTANCE());
    }
    String className = null;
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
    return PaasClient.getStorageClient().deleteObject(className, sb.toString());
  }

  public void refresh() {
    refresh(null);
  }

  public void refresh(String includeKeys) {
    refreshInBackground(includeKeys).blockingSubscribe();
  }

  public Observable<AVObject> refreshInBackground() {
    return refreshInBackground(null);

  }

  public Observable<AVObject> refreshInBackground(String includeKeys) {
    if (totallyOverwrite) {
      return PaasClient.getStorageClient().getWholeObject(this.endpointClassName, getObjectId())
              .map(new Function<AVObject, AVObject>() {
                @Override
                public AVObject apply(AVObject avObject) throws Exception {
                  AVObject.this.serverData.clear();
                  AVObject.this.serverData.putAll(avObject.serverData);
                  return AVObject.this;
                }
              });
    }
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
  public Observable<AVObject> fetchInBackground() {
    return refreshInBackground();
  }
  public Observable<AVObject> fetchInBackground(String includeKyes) {
    return refreshInBackground(includeKyes);
  }

  public Observable<AVObject> fetchIfNeededInBackground() {
    if (!StringUtil.isEmpty(getObjectId()) && this.serverData.size() > 1) {
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
      this.operations.putAll(avObject.operations);
    }
  }

  void mergeRawData(AVObject avObject) {
    if (null != avObject) {
      this.serverData.putAll(avObject.serverData);
    }
    this.operations.clear();
  }

  public void resetServerData(Map data) {
    this.serverData.clear();
    this.serverData.putAll(data);
    this.operations.clear();
  }

  @JSONField(serialize = false)
  public String getRequestRawEndpoint() {
    if (StringUtil.isEmpty(getObjectId())) {
      return "/1.1/classes/" + this.getClassName();
    } else {
      return "/1.1/classes/" + this.getClassName() + "/" + getObjectId();
    }
  }

  @JSONField(serialize = false)
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
  public synchronized AVACL getACL() {
    if (null == this.acl) {
      this.acl = generateACLFromServerData();
    }
    return this.acl;
  }

  public synchronized void setACL(AVACL acl) {
    this.acl = acl;
  }

  private AVACL generateACLFromServerData() {
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

  public static <T extends AVObject> AVQuery<T> getQuery(Class<T> clazz) {
    return new AVQuery<T>(Transformer.getSubClassName(clazz), clazz);
  }

  /**
   * common methods.
   */
  /**
   * generate a new json object with server data.
   * @return
   */
  public JSONObject toJSONObject() {
    return new JSONObject(this.serverData);
  }

  /**
   * generate a json string.
   * @return
   */
  public String toJSONString() {return JSON.toJSONString(this);}

  /**
   * create AVObject instance from json string which generated by AVObject.toString or AVObject.toJSONString.
   *
   * @param objectString
   * @return null if objectString is null
   */
  public static AVObject parseAVObject(String objectString) {
    if (StringUtil.isEmpty(objectString)) {
      return null;
    }
    return JSON.parseObject(objectString, AVObject.class, Feature.SupportAutoType);
  }

  /**
   * create a new instance with particular classname and objectId.
   * @param className class name
   * @param objectId  object id
   * @return
   */
  public static AVObject createWithoutData(String className, String objectId) {
    AVObject object = new AVObject(className);
    object.setObjectId(objectId);
    return object;
  }

  /**
   * create a new instance with particular class and objectId.
   * @param clazz     class info
   * @param objectId  object id
   * @param <T>
   * @return
   * @throws AVException
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
