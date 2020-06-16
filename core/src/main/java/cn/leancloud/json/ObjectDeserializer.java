package cn.leancloud.json;

import cn.leancloud.*;
import cn.leancloud.ops.Utils;
import cn.leancloud.utils.StringUtil;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectDeserializer implements JsonDeserializer<AVObject> {
  public static final String KEY_VERSION = "_version";
  private static final String DEFAULT_VERSION = "5";
  public static final String KEY_SERVERDATA = "serverData";

  private AVObject generateObject(Map<String, Object> objectMap) {
    String className = "";
    Map<String, Object> serverJson = null;
    if (objectMap.containsKey(KEY_VERSION)) {
      // 5.x version
      className = (String) objectMap.get(AVObject.KEY_CLASSNAME);
      if (objectMap.containsKey(KEY_SERVERDATA)) {
        serverJson = (Map<String, Object>) objectMap.get(KEY_SERVERDATA);
      } else {
        serverJson = objectMap;
      }
    } else if (objectMap.containsKey(AVObject.KEY_CLASSNAME)) {
      // android sdk output
      // {
      // "@type":"com.example.avoscloud_demo.Student","objectId":"5bff468944d904005f856849",
      // "updatedAt":"2018-12-08T09:53:05.008Z","createdAt":"2018-11-29T01:53:13.327Z",
      // "className":"Student",
      // "serverData":{"@type":"java.util.concurrent.ConcurrentHashMap",
      //               "name":"Automatic Tester's Dad","course":["Math","Art"],"age":20}}
      className = (String) objectMap.get(AVObject.KEY_CLASSNAME);
      objectMap.remove(AVObject.KEY_CLASSNAME);
      if (objectMap.containsKey(KEY_SERVERDATA)) {
        ConcurrentHashMap<String, Object> serverData = (ConcurrentHashMap<String, Object>) objectMap.get(KEY_SERVERDATA);//
        objectMap.remove(KEY_SERVERDATA);
        objectMap.putAll(serverData);
      }
      objectMap.remove("operationQueue");
      serverJson = objectMap;
    } else {
      // leancloud server response.
      serverJson = objectMap;
    }
    AVObject obj;
    if (className.endsWith(AVFile.class.getCanonicalName())) {
      obj = new AVFile();
    } else if (className.endsWith(AVUser.class.getCanonicalName())) {
      obj = new AVUser();
    } else if (className.endsWith(AVInstallation.class.getCanonicalName())) {
      obj = new AVInstallation();
    } else if (className.endsWith(AVStatus.class.getCanonicalName())) {
      obj = new AVStatus();
    } else if (className.endsWith(AVRole.class.getCanonicalName())) {
      obj = new AVRole();
    } else if (!StringUtil.isEmpty(className)) {
      obj = Transformer.objectFromClassName(className);
    } else {
      obj = new AVObject();
    }
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

  public AVObject deserialize(JsonElement elem, Type type, JsonDeserializationContext ctx) throws JsonParseException {
    if (null == elem || !elem.isJsonObject()) {
      return null;
    }
    JsonObject json = elem.getAsJsonObject();
    Map<String, Object> mapData = new HashMap<String, Object>();
    for (Map.Entry<String, JsonElement> entry: json.entrySet()) {
      mapData.put(entry.getKey(), ConverterUtils.toJavaObject(entry.getValue()));
    }

    return generateObject(mapData);
  }
}
