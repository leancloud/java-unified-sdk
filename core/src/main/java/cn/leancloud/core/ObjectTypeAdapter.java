package cn.leancloud.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class ObjectTypeAdapter implements ObjectSerializer, ObjectDeserializer{
  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                    int features) throws IOException {
    AVObject avObject = (AVObject)object;
    SerializeWriter writer = serializer.getWriter();
    writer.writeFieldNameDirect(JSON.toJSONString(avObject.serverData));
  }

  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
    AVObject obj = new AVObject("");
    obj.serverData = parser.parseObject();
    return (T) obj;
  }

  public int getFastMatchToken() {
    return JSONToken.LBRACKET;
  }
}
