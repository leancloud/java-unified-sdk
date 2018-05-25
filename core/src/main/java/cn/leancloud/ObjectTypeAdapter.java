package cn.leancloud;

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
import java.util.List;
import java.util.Map;

public class ObjectTypeAdapter implements ObjectSerializer, ObjectDeserializer{
  private static AVLogger LOGGER = LogUtil.getLogger(ObjectTypeAdapter.class);

  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                    int features) throws IOException {
    AVObject avObject = (AVObject)object;
    SerializeWriter writer = serializer.getWriter();
    writer.write('{');
    writer.writeFieldValue(' ', AVObject.KEY_CLASSNAME, avObject.getClassName());
    writer.writeFieldValue(',', "serverData",
            JSON.toJSONString(avObject.serverData, ObjectValueFilter.instance, SerializerFeature.WriteClassName,
                    SerializerFeature.DisableCircularReferenceDetect));
    writer.write('}');
  }

  /**
   *
   * @param parser
   * @param type
   * @param fieldName
   * @return
   *
   * @since 1.8+
   */
  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
    String className = "";
    JSONObject serverJson = null;
    JSONObject jsonObject = parser.parseObject();
    if (jsonObject.containsKey(AVObject.KEY_CLASSNAME)) {
      className = (String)jsonObject.get(AVObject.KEY_CLASSNAME);
      if (jsonObject.containsKey("serverData")) {
        serverJson = jsonObject.getJSONObject("serverData");
      } else {
        serverJson = jsonObject;
      }
    } else {
      // server response.
      serverJson = jsonObject;
    }
    AVObject obj;
    if (type.toString().endsWith(AVFile.class.getCanonicalName())) {
      obj = new AVFile();
    } else if (type.toString().endsWith(AVUser.class.getCanonicalName())) {
      obj = new AVUser();
    } else if (!StringUtil.isEmpty(className)){
      obj = new AVObject(className);
    } else {
      obj = new AVObject();
    }
    Map<String, Object> innerMap = serverJson.getInnerMap();
    for (String k: innerMap.keySet()) {
      Object v = innerMap.get(k);
      if (v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Byte || v instanceof Character) {
        // primitive type
        obj.serverData.put(k, v);
      } else if (v instanceof Map) {
        obj.serverData.put(k, Utils.getObjectFrom(v));
      } else if (v instanceof Collection) {
        obj.serverData.put(k, Utils.getObjectFrom(v));
      } else {
        obj.serverData.put(k, v);
      }
    }
    return (T) obj;
  }

  public int getFastMatchToken() {
    return JSONToken.LBRACKET;
  }
}
