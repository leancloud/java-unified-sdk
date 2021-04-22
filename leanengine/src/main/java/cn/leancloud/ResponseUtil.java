package cn.leancloud;


import cn.leancloud.ops.Utils;
import cn.leancloud.types.LCGeoPoint;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONArray;
import cn.leancloud.json.JSONObject;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class ResponseUtil {
  private static final String TYPE = "__type";

  public static String filterResponse(String response) {
    JSONObject resp = JSON.parseObject(response, JSONObject.class);
    Object result = resp.get("result");
    if (result instanceof JSONObject) {
      removeType((JSONObject) result);
    } else if (result instanceof JSONArray) {
      for (Object o : ((JSONArray) result).toArray()) {
        if (o instanceof JSONObject) {
          removeType((JSONObject) o);
        }
      }
    }
    return restfulCloudData(resp);
  }

  private static void removeType(JSONObject object) {
    if (object.containsKey("className") && object.containsKey(TYPE)) {
      object.remove("className");
      object.remove(TYPE);
    }
  }

  static String restfulCloudData(Object object) {
    if (object == null)
      return "{}";
    if (object instanceof Map) {
      return jsonStringFromMapWithNull(Utils.getParsedMap((Map<String, Object>) object, true));
    } else if (object instanceof Collection) {
      return jsonStringFromObjectWithNull(Utils.getParsedList((Collection) object, true));
    } else if (object instanceof LCObject) {
      return jsonStringFromMapWithNull(Utils.mapFromAVObject((LCObject) object, true));
    } else if (object instanceof LCGeoPoint) {
      return jsonStringFromMapWithNull(Utils.mapFromGeoPoint((LCGeoPoint) object));
    } else if (object instanceof Date) {
      return jsonStringFromObjectWithNull(Utils.mapFromDate((Date) object));
    } else if (object instanceof byte[]) {
      return jsonStringFromMapWithNull(Utils.mapFromByteArray((byte[]) object));
    } else if (object instanceof LCFile) {
      return jsonStringFromMapWithNull(((LCFile) object).toMap());
    } else if (object instanceof JSONObject) {
      return jsonStringFromObjectWithNull(JSON.parse(object.toString()));
    } else if (object instanceof JSONArray) {
      return jsonStringFromObjectWithNull(JSON.parse(object.toString()));
    } else {
      return jsonStringFromObjectWithNull(object);
    }
  }
  static String jsonStringFromMapWithNull(Object map) {
    return JSON.toJSONString(map);
  }

  static String jsonStringFromObjectWithNull(Object map) {
    return JSON.toJSONString(map);
  }
}
