package cn.leancloud.core;

import cn.leancloud.AVLogger;
import cn.leancloud.utils.LogUtil;
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
    AVObject obj;
    if (type.toString().endsWith(AVFile.class.getCanonicalName())) {
      obj = new AVFile();
    } else {
      obj = new AVObject("");
    }
    JSONObject jsonObject = parser.parseObject();
    if (jsonObject.containsKey(AVObject.KEY_CLASSNAME)) {
      String className = (String)jsonObject.get(AVObject.KEY_CLASSNAME);
      obj.className = className;
      if (jsonObject.containsKey("serverData")) {
        obj.serverData = jsonObject.getJSONObject("serverData");
      } else {
        obj.serverData = jsonObject;
      }
    } else {
      // server response.
      obj.serverData = jsonObject;
    }
    return (T) obj;
  }

  public int getFastMatchToken() {
    return JSONToken.LBRACKET;
  }
}
