package cn.leancloud.gson;

import cn.leancloud.*;
import cn.leancloud.json.JSONObject;
import cn.leancloud.ops.Utils;
import cn.leancloud.utils.StringUtil;
import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class ObjectDeserializer implements JsonDeserializer<LCObject> {
  public static final String KEY_VERSION = "_version";
  public static final String KEY_SERVERDATA = "serverData";
  private MapDeserializerDoubleAsIntFix mapDeserializer = new MapDeserializerDoubleAsIntFix();

  private LCObject generateObject(Map<String, Object> objectMap, String className) {
    Map<String, Object> serverJson = null;
    if (objectMap.containsKey(KEY_VERSION)) {
      // 5.x version
      className = (String) objectMap.get(LCObject.KEY_CLASSNAME);
      if (objectMap.containsKey(KEY_SERVERDATA)) {
        serverJson = (Map<String, Object>) objectMap.get(KEY_SERVERDATA);
      } else {
        serverJson = objectMap;
      }
    } else if (objectMap.containsKey(LCObject.KEY_CLASSNAME)) {
      // android sdk output
      // {
      // "@type":"com.example.avoscloud_demo.Student","objectId":"5bff468944d904005f856849",
      // "updatedAt":"2018-12-08T09:53:05.008Z","createdAt":"2018-11-29T01:53:13.327Z",
      // "className":"Student",
      // "serverData":{"@type":"java.util.concurrent.ConcurrentHashMap",
      //               "name":"Automatic Tester's Dad","course":["Math","Art"],"age":20}}
      className = (String) objectMap.get(LCObject.KEY_CLASSNAME);
      objectMap.remove(LCObject.KEY_CLASSNAME);
      if (objectMap.containsKey(KEY_SERVERDATA)) {
        LinkedTreeMap<String, Object> serverData = (LinkedTreeMap<String, Object>) objectMap.get(KEY_SERVERDATA);//
        objectMap.remove(KEY_SERVERDATA);
        objectMap.putAll(serverData);
      }
      objectMap.remove("operationQueue");
      serverJson = objectMap;
    } else {
      // leancloud server response.
      serverJson = objectMap;
    }
    LCObject obj;
    if (className.endsWith(LCFile.class.getCanonicalName())) {
      obj = new LCFile();
    } else if (className.endsWith(LCUser.class.getCanonicalName())) {
      obj = new LCUser();
    } else if (className.endsWith(LCInstallation.class.getCanonicalName())) {
      obj = new LCInstallation();
    } else if (className.endsWith(LCStatus.class.getCanonicalName())) {
      obj = new LCStatus();
    } else if (className.endsWith(LCRole.class.getCanonicalName())) {
      obj = new LCRole();
    } else if (!StringUtil.isEmpty(className) && className.indexOf(".") < 0) {
      obj = Transformer.objectFromClassName(className);
    } else {
      obj = new LCObject();
    }
    serverJson.remove("@type");
    for (Map.Entry<String, Object> entry: serverJson.entrySet()) {
      String k = entry.getKey();
      Object v = entry.getValue();
      if (v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Byte || v instanceof Character) {
        // primitive type
        obj.getServerData().put(k, v);
      } else if (v instanceof Map || v instanceof JSONObject) {
        obj.getServerData().put(k, Utils.getObjectFrom(v));
      } else if (v instanceof Collection) {
        obj.getServerData().put(k, Utils.getObjectFrom(v));
      } else if (null != v) {
        obj.getServerData().put(k, v);
      }
    }
    return obj;
  }

  public LCObject deserialize(JsonElement elem, Type type, JsonDeserializationContext ctx) throws JsonParseException {
    if (null == elem || !elem.isJsonObject()) {
      return null;
    }
//    JsonObject json = elem.getAsJsonObject();
    Map<String, Object> mapData = mapDeserializer.deserialize(elem, type, ctx);
//    for (Map.Entry<String, JsonElement> entry: json.entrySet()) {
//      mapData.put(entry.getKey(), GsonWrapper.toJavaObject(entry.getValue()));
//    }

    return generateObject(mapData, ((Class)type).getCanonicalName());
  }
}
