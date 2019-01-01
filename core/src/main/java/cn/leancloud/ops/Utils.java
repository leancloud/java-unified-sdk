package cn.leancloud.ops;

import java.util.*;

import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.AVRelation;
import cn.leancloud.Transformer;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.utils.StringUtil;

import cn.leancloud.codec.Base64;

public class Utils {
  private static final String typeTag = "__type";

  public static Map<String, Object> createPointerArrayOpMap(String key, String op,
                                                            Collection<AVObject> objects) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("__op", op);
    List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
    for (AVObject obj : objects) {
      list.add(mapFromPointerObject(obj));
    }
    map.put("objects", list);
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(key, map);
    return result;
  }

  public static Map<String, Object> mapFromPointerObject(AVObject object) {
    return mapFromAVObject(object, false);
  }

  public static Map<String, Object> mapFromGeoPoint(AVGeoPoint point) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(typeTag, "GeoPoint");
    result.put("latitude", point.getLatitude());
    result.put("longitude", point.getLongitude());
    return result;
  }

  public static AVGeoPoint geoPointFromMap(Map<String, Object> map) {
    double la = ((Number) map.get("latitude")).doubleValue();
    double lo = ((Number) map.get("longitude")).doubleValue();
    AVGeoPoint point = new AVGeoPoint(la, lo);
    return point;
  }

  public static AVObject parseObjectFromMap(Map<String, Object> map) {
    AVObject avObject = Transformer.objectFromClassName((String) map.get("className"));
    map.remove("__type");
    avObject.resetServerData(map);
    return avObject;
  }

  public static byte[] dataFromMap(Map<String, Object> map) {
    String value = (String) map.get("base64");
    return Base64.decode(value, Base64.NO_WRAP);
  }
  public static Date dateFromMap(Map<String, Object> map) {
    String value = (String) map.get("iso");
    return StringUtil.dateFromString(value);
  }


  public static Map<String, Object> mapFromDate(Date date) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(typeTag, "Date");
    result.put("iso", StringUtil.stringFromDate(date));
    return result;
  }

  public static Map<String, Object> mapFromByteArray(byte[] data) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(typeTag, "Bytes");
    result.put("base64", Base64.encodeToString(data, Base64.NO_WRAP));
    return result;
  }

  public static Map<String, Object> mapFromFile(AVFile file) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("__type", "_File");
    result.put("metaData", file.getMetaData());
    result.put("id", file.getName());
    return result;
  }

  public static Map<String, Object> mapFromAVObject(AVObject object, boolean topObject) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("className", object.internalClassName());

    if (!StringUtil.isEmpty(object.getObjectId())) {
      result.put("objectId", object.getObjectId());
    }
    if (!topObject) {
      result.put("__type", "Pointer");
    } else {
      result.put("__type", "Object");

      Map<String, Object> serverData = getParsedMap(object.getServerData(), false);
      if (serverData != null && !serverData.isEmpty()) {
        result.putAll(serverData);
      }
    }
    return result;
  }

  public static Map<String, Object> getParsedMap(Map<String, Object> map) {
    return getParsedMap(map, false);
  }

  public static Map<String, Object> getParsedMap(Map<String, Object> object, boolean topObject) {
    Map newMap = new HashMap<String, Object>(object.size());

    for (Map.Entry<String, Object> entry : object.entrySet()) {
      final String key = entry.getKey();
      Object o = entry.getValue();
      newMap.put(key, getParsedObject(o, topObject));
    }

    return newMap;
  }

  public static List getParsedList(Collection list) {
    List newList = new ArrayList(list.size());

    for (Object o : list) {
      newList.add(getParsedObject(o));
    }

    return newList;
  }

  public static List getParsedList(Collection object, boolean topObject) {
    if (!topObject) {
      return getParsedList(object);
    } else {
      List newList = new ArrayList(object.size());

      for (Object o : object) {
        newList.add(getParsedObject(o, true));
      }

      return newList;
    }
  }

  public static Object getParsedObject(Object object) {
    return getParsedObject(object, false);
  }

  public static Object getParsedObject(Object object, boolean topObject) {
    if (object == null) {
      return null;
    } else if (object instanceof Map) {
      return getParsedMap((Map<String, Object>) object, topObject);
    } else if (object instanceof Collection) {
      return getParsedList((Collection) object, topObject);
    } else if (object instanceof AVObject) {
      if (!topObject) {
        return mapFromPointerObject((AVObject) object);
      } else {
        return mapFromAVObject((AVObject) object, true);
      }
    } else if (object instanceof AVGeoPoint) {
      return mapFromGeoPoint((AVGeoPoint) object);
    } else if (object instanceof Date) {
      return mapFromDate((Date) object);
    } else if (object instanceof byte[]) {
      return mapFromByteArray((byte[]) object);
    } else if (object instanceof AVFile) {
      return mapFromFile((AVFile) object);
    } else {
      return object;
    }
  }

  public static Map<String, Object> createArrayOpMap(String key, String op, Collection<?> objects) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("__op", op);
    List<Object> array = new ArrayList<Object>();
    for (Object obj : objects) {
      array.add(getParsedObject(obj));
    }
    map.put("objects", array);
    Map<String, Object> ops = new HashMap<String, Object>();
    ops.put(key, map);
    return ops;
  }

  public static AVRelation objectFromRelationMap(Map<String, Object> map) {
    String className = (String) map.get("className");
    return new AVRelation(className);
  }

  public static AVFile fileFromMap(Map<String, Object> map) {
    AVFile file = new AVFile("", "");
    file.resetServerData(map);
    Object metadata = map.get("metaData");
    if (metadata != null && metadata instanceof Map) {
      file.getMetaData().putAll((Map) metadata);
    }
    return file;
  }

  public static List getObjectFrom(Collection list) {
    List newList = new ArrayList();

    for (Object obj : list) {
      newList.add(getObjectFrom(obj));
    }

    return newList;
  }

  public static Object getObjectFrom(Map<String, Object> map) {
    Object type = map.get("__type");
    if (type == null || !(type instanceof String)) {
      Map<String, Object> newMap = new HashMap<String, Object>(map.size());

      for (Map.Entry<String, Object> entry : map.entrySet()) {
        final String key = entry.getKey();
        Object o = entry.getValue();
        newMap.put(key, getObjectFrom(o));
      }
      return newMap;
    }
    map.remove("__type");
    if (type.equals("Pointer") || type.equals("Object")) {
      AVObject avObject = Transformer.objectFromClassName((String) map.get("className"));
      avObject.resetServerData(map);
      return avObject;
    } else if (type.equals("GeoPoint")) {
      return geoPointFromMap(map);
    } else if (type.equals("Bytes")) {
      return dataFromMap(map);
    } else if (type.equals("Date")) {
      return dateFromMap(map);
    } else if (type.equals("Relation")) {
      return objectFromRelationMap(map);
    } else if (type.equals("File")) {
      return fileFromMap(map);
    }
    return map;
  }

  public static Object getObjectFrom(Object obj) {
    if (obj instanceof Collection) {
      return getObjectFrom((Collection) obj);
    } else if (obj instanceof Map) {
      return getObjectFrom((Map<String, Object>) obj);
    }

    return obj;
  }

  public static Map<String, Object> makeCompletedRequest(String internalId, String path, String method, Map<String, Object> param) {
    if (null == param || StringUtil.isEmpty(path) || StringUtil.isEmpty(method)) {
      return null;
    }
    param.put(BaseOperation.KEY_INTERNAL_ID, internalId);

    Map<String, Object> topParams = new HashMap<String, Object>();
    topParams.put(BaseOperation.KEY_BODY, param);
    topParams.put(BaseOperation.KEY_PATH, path);
    topParams.put(BaseOperation.KEY_HTTP_METHOD, method);
    return topParams;
  }

}
