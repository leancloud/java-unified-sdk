package cn.leancloud.ops;

import cn.leancloud.AVObject;
import cn.leancloud.ObjectValueFilter;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BaseOperationAdapter implements ObjectSerializer, ObjectDeserializer {
  private static final String ATTR_OP = "operation";
  private static final String ATTR_FIELD = "field";
  private static final String ATTR_FINAL = "final";
  private static final String ATTR_OBJECT = "value";
  private static final String ATTR_SUBOPS = "subOps";

  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                    int features) throws IOException {
    BaseOperation op = (BaseOperation)object;
    SerializeWriter writer = serializer.getWriter();
    writer.write('{');
    writer.writeFieldValue(' ', ATTR_OP, op.getOperation());
    writer.writeFieldValue(' ', ATTR_FIELD, op.getField());
    writer.writeFieldValue(',', ATTR_FINAL, op.isFinal);
    writer.writeFieldValue(',', ATTR_OBJECT,
            JSON.toJSONString(op.getValue(), ObjectValueFilter.instance,
                    SerializerFeature.WriteClassName, SerializerFeature.DisableCircularReferenceDetect));
    if (object instanceof CompoundOperation) {
      writer.writeFieldValue(',', ATTR_SUBOPS,
              JSON.toJSONString(((CompoundOperation)op).getSubOperations(), ObjectValueFilter.instance,
                      SerializerFeature.WriteClassName, SerializerFeature.DisableCircularReferenceDetect));
    }
    writer.write('}');
  }

  private Object getParsedObject(Object obj) {
    if (obj instanceof JSONObject) {
      JSONObject jsonObj = (JSONObject) obj;
      if (jsonObj.containsKey(AVObject.KEY_CLASSNAME)) {
        try {
          return JSON.parseObject(JSON.toJSONString(jsonObj), AVObject.class);
        } catch (Exception ex){
          ex.printStackTrace();
          return obj;
        }
      } else {
        return obj;
      }
    } else if (obj instanceof Collection) {
      List<Object> result = new ArrayList<>();
      for (Object o: ((Collection) obj).toArray()) {
        result.add(getParsedObject(o));
      }
      return result;
    } else {
      return obj;
    }
  }

  private <T> T parseJSONObject(JSONObject jsonObject) {
    if (jsonObject.containsKey(ATTR_OP) && jsonObject.containsKey(ATTR_FIELD)) {
      String op = jsonObject.getString(ATTR_OP);
      String field = jsonObject.getString(ATTR_FIELD);
      boolean isFinal = jsonObject.containsKey(ATTR_FINAL)? jsonObject.getBoolean(ATTR_FINAL) : false;

      OperationBuilder.OperationType opType = OperationBuilder.OperationType.valueOf(op);

      Object obj = jsonObject.containsKey(ATTR_OBJECT) ? jsonObject.get(ATTR_OBJECT) : null;
      Object parsedObj = getParsedObject(obj);

      ObjectFieldOperation result = OperationBuilder.gBuilder.create(opType, field, parsedObj);
      ((BaseOperation)result).isFinal = isFinal;

      if (jsonObject.containsKey(ATTR_SUBOPS) && result instanceof CompoundOperation) {
        List<JSONObject> subOps = jsonObject.getObject(ATTR_SUBOPS, List.class);
        for (JSONObject o : subOps) {
          result.merge((BaseOperation)parseJSONObject(o));
        }
      }

      return (T) result;
    }
    return (T) new NullOperation("Null", null);
  }

  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
    JSONObject jsonObject = parser.parseObject();
    return parseJSONObject(jsonObject);
  }

  public int getFastMatchToken() {
    return JSONToken.LBRACKET;
  }
}
