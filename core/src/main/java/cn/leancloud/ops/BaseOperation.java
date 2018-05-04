package cn.leancloud.ops;

import cn.leancloud.AVLogger;
import cn.leancloud.codec.Base64;
import cn.leancloud.AVACL;
import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

public abstract class BaseOperation implements ObjectFieldOperation {
  static final AVLogger LOGGER = LogUtil.getLogger(BaseOperation.class);
  static final String KEY_OP = "__op";
  static final String KEY_OBJECTS = "objects";
  static final String KEY_AMOUNT = "amount";
  static final String KEY_VALUE = "value";
  public static final String KEY_INTERNAL_ID = "__internalId";
  public static final String KEY_BODY = "body";
  public static final String KEY_PATH = "path";
  public static final String KEY_HTTP_METHOD = "method";

  protected String op = null;
  protected String field = null;
  protected Object value = null;
  protected boolean isFinal = false;

  public BaseOperation(String op, String field, Object value, boolean isFinal) {
    this.op = op;
    this.field = field;
    this.value = value;
    this.isFinal = isFinal;
  }

  public String getOperation() {
    return this.op;
  }
  public String getField() {
    return this.field;
  }
  public Object getValue() {
    return this.value;
  }
  public boolean checkCircleReference(Map<AVObject, Boolean> markMap) {
    if (null == markMap) {
      return false;
    }
    if (!(this.value instanceof AVObject)) {
      return false;
    }
    AVObject v = (AVObject)this.value;
    if (markMap.containsKey(v) && markMap.get(v) == true) {
      return true;
    }
    boolean rst = v.hasCircleReference(markMap);
    markMap.put(v, rst);
    return rst;
  }

  /**
   * apply operation to object, in order to generate new attribute value.
   *
   * @param obj
   * @return
   */
  public abstract Object apply(Object obj);

  /**
   * merge with previous operations.
   *
   * @param previous
   * @return
   */
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation previous) {
    return NullOperation.gInstance;
  }

  public ObjectFieldOperation merge(ObjectFieldOperation other) {
    if (null == other || other instanceof NullOperation) {
      return this;
    }
    if (isFinal) {
      // ignore all previous operations.
      return this;
    }
    return mergeWithPrevious(other);
  }

  protected Object concatCollections(Object left, Object right) {
    if (null == left || null == right) {
      return null == left? right : left;
    }
    List<Object> result = new ArrayList<Object>();
    if (left instanceof Collection) {
      result.addAll((Collection<?>) left);
    } else {
      result.add(left);
    }
    if (right instanceof Collection) {
      result.addAll((Collection<?>) right);
    } else {
      result.add(right);
    }
    try {
      if (null != result) {
        HashSet uniqueSet = new HashSet(result.size());
        for (Object o:result) {
          uniqueSet.add(o);
        }
        result = Arrays.asList(uniqueSet.toArray());
      }
    } catch (Exception ex) {
      LOGGER.w("failed to concat collections.", ex);
    }
    return result;
  }

  protected void reportIllegalOperations(ObjectFieldOperation current, ObjectFieldOperation prev) {
    LOGGER.w("illegal operations. current=" + current.getClass().getSimpleName() + ", prev=" + prev.getClass().getSimpleName());
  }

  public abstract Map<String, Object> encode();

  protected Object encodeObject(Object o) {
    return encodeObject(o, false);
  }

  protected Object encodeObject(Object o, boolean isTop) {
    if (null == o) {
      return null;
    } else if (o instanceof Map) {
      return encodeMap((Map<String, Object>)o, isTop);
    } else if (o instanceof Collection) {
      return encodeCollection((Collection)o, isTop);
    } else if (o instanceof AVObject) {
      return encodeAVObject((AVObject)o, isTop);
    } else if (o instanceof AVGeoPoint) {
      return encodeGeoPointer((AVGeoPoint) o);
    }else if (o instanceof AVACL) {
      return ((AVACL) o).toJSONObject();
    } else if (o instanceof AVFile) {
      return encodeAVFile((AVFile) o);
    } else if (o instanceof Date) {
      return encodeDate((Date) o);
    } else if (o instanceof byte[]) {
      return encodeByteArray((byte[])o);
    } else if (o instanceof JSONObject || o instanceof JSONArray) {
      return o;
    } else {
      ;
    }
    return o;
  }

  protected Object encodeMap(Map<String, Object> map, boolean isTop) {
    Map newMap = new HashMap<String, Object>(map.size());

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      final String key = entry.getKey();
      Object o = entry.getValue();
      newMap.put(key, encodeObject(o, isTop));
    }

    return newMap;
  }

  protected Object encodeCollection(Collection collection, boolean isTop) {
    List result = new ArrayList(collection.size());
    for (Object o: collection) {
      result.add(encodeObject(o, isTop));
    }
    return result;
  }

  protected Object encodeAVObject(AVObject o, boolean isTop) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("className", o.getClassName());
    if (!StringUtil.isEmpty(o.getObjectId())) {
      result.put("objectId", o.getObjectId());
    }
    if (isTop) {
      result.put("__type", "Object");
      Map<String, Object> serverData = (Map<String, Object>) encodeMap(o.getServerData(), false);
      if (serverData != null && !serverData.isEmpty()) {
        result.putAll(serverData);
      }
    } else {
      result.put("__type", "Pointer");
    }
    return result;
  }

  protected Object encodeGeoPointer(AVGeoPoint o) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("__type", "GeoPoint");
    result.put("latitude", o.getLatitude());
    result.put("longitude", o.getLongitude());
    return result;
  }

  protected Object encodeAVFile(AVFile o) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("__type", "_File");
    result.put("metaData", o.getMetaData());
    result.put("id", o.getName());
    return result;
  }

  protected Object encodeByteArray(byte[] o) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("__type", "Bytes");
    result.put("base64", Base64.encodeToString(o, Base64.NO_WRAP));
    return result;
  }

  public static Map<String, Object> encodeDate(Date date) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("__type", "Date");
    result.put("iso", StringUtil.stringFromDate(date));
    return result;
  }
}
