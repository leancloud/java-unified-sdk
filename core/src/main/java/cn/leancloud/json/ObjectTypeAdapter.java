package cn.leancloud.json;

import cn.leancloud.*;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.ops.Utils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectTypeAdapter implements ObjectSerializer, ObjectDeserializer{
  private static AVLogger LOGGER = LogUtil.getLogger(ObjectTypeAdapter.class);
  public static final String KEY_VERSION = "_version";
  private static final String DEFAULT_VERSION = "5";
  public static final String KEY_SERVERDATA = "serverData";


  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                    int features) throws IOException {
    this.write(serializer, object, fieldName, fieldType);
  }

  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
    System.out.println("Adapter write. object=" + object.getClass().getSimpleName() + ", field=" + fieldName);

    AVObject avObject = (AVObject)object;
    SerializeWriter writer = serializer.getWriter();
    writer.write('{');

    // for 1.1.70.android fastjson, we dont use writer.writeFieldValue(seperator, field, value) method.

//    writer.writeFieldName("@type", false);
//    writer.writeString(avObject.getClass().getName());
//    writer.write(',');
    writer.writeFieldName(KEY_VERSION, false);
    writer.writeString(DEFAULT_VERSION);
    writer.write(',');
    writer.writeFieldName(AVObject.KEY_CLASSNAME, false);
    writer.writeString(avObject.getClassName());
    writer.write(',');
    writer.writeFieldName(KEY_SERVERDATA, false);

    Map<String, Object> serverData = avObject.getServerData();
    if (object instanceof AVRole) {
      AVACL acl = ((AVRole)object).getACL();
      if (null != acl) {
        serverData.put(AVObject.KEY_ACL, acl);
      }
    }

    if (AVOSCloud.isEnableCircularReferenceDetect()) {
      writer.write(JSON.toJSONString(serverData, ObjectValueFilter.instance, SerializerFeature.WriteClassName));
    } else {
      writer.write(JSON.toJSONString(serverData, ObjectValueFilter.instance, SerializerFeature.WriteClassName,
              SerializerFeature.DisableCircularReferenceDetect));
    }

    writer.write('}');
  }

  /**
   * deserializer
   * @param parser json parser
   * @param type type
   * @param fieldName field name
   * @return object.
   *
   * @since 1.8+
   */
  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
    System.out.println("Adapter deserialze. object=" + type.toString() + ", field=" + fieldName);

    if (!AVObject.class.isAssignableFrom((Class) type)) {
      return (T) parser.parseObject();
    }

    String className = "";
    Map<String, Object> serverJson = null;
    Map<String, Object> objectMap = parser.parseObject(Map.class);
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
      // { "@type":"com.example.avoscloud_demo.Student","objectId":"5bff468944d904005f856849","updatedAt":"2018-12-08T09:53:05.008Z","createdAt":"2018-11-29T01:53:13.327Z","className":"Student","serverData":{"@type":"java.util.concurrent.ConcurrentHashMap","name":"Automatic Tester's Dad","course":["Math","Art"],"age":20}}
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
    if (type.toString().endsWith(AVFile.class.getCanonicalName())) {
      obj = new AVFile();
    } else if (type.toString().endsWith(AVUser.class.getCanonicalName())) {
      obj = new AVUser();
    } else if (type.toString().endsWith(AVInstallation.class.getCanonicalName())) {
      obj = new AVInstallation();
    } else if (type.toString().endsWith(AVStatus.class.getCanonicalName())) {
      obj = new AVStatus();
    } else if (type.toString().endsWith(AVRole.class.getCanonicalName())) {
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
    return (T) obj;

  }

  public int getFastMatchToken() {
    return JSONToken.LBRACKET;
  }
}
