package cn.leancloud.json;

import cn.leancloud.AVObject;
import cn.leancloud.ops.BaseOperation;
import cn.leancloud.ops.CompoundOperation;
import cn.leancloud.ops.ObjectFieldOperation;
import cn.leancloud.ops.OperationBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BaseOperationAdapter extends TypeAdapter<BaseOperation> {
  private static final String ATTR_OP = "operation";
  private static final String ATTR_FIELD = "field";
  private static final String ATTR_FINAL = "final";
  private static final String ATTR_OBJECT = "value";
  private static final String ATTR_SUBOPS = "subOps";

  public void write(JsonWriter writer, BaseOperation op) throws IOException {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(ATTR_OP, op.getOperation());
    jsonObject.addProperty(ATTR_FIELD, op.getField());
    jsonObject.addProperty(ATTR_FINAL, op.isFinal());
    jsonObject.add(ATTR_OBJECT, ConverterUtils.toJsonElement(op.getValue()));
    if (op instanceof CompoundOperation) {
      List<ObjectFieldOperation> subOps = ((CompoundOperation)op).getSubOperations();
      jsonObject.add(ATTR_SUBOPS, ConverterUtils.toJsonElement(subOps));
    }
    TypeAdapters.JSON_ELEMENT.write(writer, jsonObject);
  }

  public BaseOperation read(JsonReader reader) throws IOException {
    JsonElement elem = TypeAdapters.JSON_ELEMENT.read(reader);
    if (elem.isJsonObject()) {
      JsonObject jsonObject = elem.getAsJsonObject();
      return parseJSONObject(new JSONObject(jsonObject));
    } else {
      return null;
    }
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

      BaseOperation result = OperationBuilder.gBuilder.create(opType, field, parsedObj);
      result.setFinal(isFinal);

      if (jsonObject.containsKey(ATTR_SUBOPS) && result instanceof CompoundOperation) {
        List<JSONObject> subOps = jsonObject.getJSONArray(ATTR_SUBOPS).toJavaList(JSONObject.class);
        for (JSONObject o : subOps) {
          result.merge((BaseOperation)parseJSONObject(o));
        }
      }

      return (T) result;
    }
    return null;
  }

}
