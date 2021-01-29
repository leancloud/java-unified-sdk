package cn.leancloud.gson;

import com.google.gson.*;

import java.lang.reflect.Type;

public class NumberDeserializerDoubleAsIntFix implements JsonDeserializer<Number> {
  @Override
  public Number deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    return (Number) read(jsonElement);
  }

  public static Object parsePrecisionNumber(Number num) {
    if (null == num) {
      return null;
    }
    // here you can handle double int/long values
    // and return any type you want
    // this solution will transform 3.0 float to long values
    double doubleValue = Math.ceil(num.doubleValue());
    if (doubleValue == num.intValue()) {
      return num.intValue();
    } else if(doubleValue  == num.longValue()) {
      return num.longValue();
    } else {
      return num.doubleValue();
    }
  }
  public Object read(JsonElement in) {
    if (in.isJsonPrimitive()) {
      JsonPrimitive prim = in.getAsJsonPrimitive();
      if(prim.isBoolean()){
        return prim.getAsBoolean();
      }else if(prim.isString()){
        return prim.getAsString();
      }else if(prim.isNumber()){

        Number num = prim.getAsNumber();
        return parsePrecisionNumber(num);
      }
    }
    return null;
  }
}
