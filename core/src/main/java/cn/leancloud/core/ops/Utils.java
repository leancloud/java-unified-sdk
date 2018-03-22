package cn.leancloud.core.ops;

import java.util.*;

import cn.leancloud.AVFile;
import cn.leancloud.core.AVObject;
import cn.leancloud.core.types.AVGeoPoint;
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

  private static Map<String, Object> mapFromAVObject(AVObject object, boolean topObject) {
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

  private static Map<String, Object> getParsedMap(Map<String, Object> object, boolean topObject) {
    Map newMap = new HashMap<String, Object>(object.size());

    for (Map.Entry<String, Object> entry : object.entrySet()) {
      final String key = entry.getKey();
      Object o = entry.getValue();
      newMap.put(key, getParsedObject(o, topObject));
    }

    return newMap;
  }

  private static List getParsedList(Collection list) {
    List newList = new ArrayList(list.size());

    for (Object o : list) {
      newList.add(getParsedObject(o));
    }

    return newList;
  }

  private static List getParsedList(Collection object, boolean topObject) {
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
}
