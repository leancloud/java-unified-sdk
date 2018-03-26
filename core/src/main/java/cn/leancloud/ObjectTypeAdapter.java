package cn.leancloud;

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

import java.io.IOException;
import java.lang.reflect.Type;

public class ObjectTypeAdapter implements ObjectSerializer, ObjectDeserializer{
  private static AVLogger LOGGER = LogUtil.getLogger(ObjectTypeAdapter.class);

  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                    int features) throws IOException {
    LOGGER.d("ObjectTypeAdapter.write for obj:" + object);
    // fixme: maybe it is necessary to serialize AVFile.
    AVObject avObject = (AVObject)object;
    SerializeWriter writer = serializer.getWriter();
    writer.write('{');
    writer.writeFieldValue(' ', AVObject.KEY_CLASSNAME, avObject.getClassName());
    writer.writeFieldValue(',', "serverData", JSON.toJSONString(avObject.serverData));
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
    obj.serverData = serverJson;
    LOGGER.d("deserialze: Type=" + type + ", fieldName=" + fieldName + ", result=" + obj.toString());
    return (T) obj;
  }

  public int getFastMatchToken() {
    return JSONToken.LBRACKET;
  }
}
