package cn.leancloud.core.ops;

import cn.leancloud.codec.Base64;
import cn.leancloud.core.AVACL;
import cn.leancloud.core.AVFile;
import cn.leancloud.core.AVObject;
import cn.leancloud.core.types.AVGeoPoint;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

public abstract class BaseOperation implements ObjectFieldOperation {
  static final String KEY_OP = "__op";
  static final String KEY_OBJECTS = "objects";
  static final String KEY_AMOUNT = "amount";
  static final String KEY_VALUE = "value";

  protected String op = null;
  protected String field = null;
  protected Object value = null;
  public BaseOperation(String op, String field, Object value) {
    this.op = op;
    this.field = field;
    this.value = value;
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
  public abstract Object apply(Object obj);
  protected abstract ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation previous);
  public ObjectFieldOperation merge(ObjectFieldOperation other) {
    if (null == other || other instanceof NullOperation) {
      return this;
    }
    return mergeWithPrevious(other);
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
      ;
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
    result.put("__type", AVFile.getClassName());
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
