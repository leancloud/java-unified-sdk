package cn.leancloud.json;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.types.AVDate;

import java.lang.reflect.Type;
import java.util.List;

public class JSON {
  public static <T> T parseObject(String text, Class<T> clazz) {
    // return com.alibaba.fastjson.JSON.parseObject(text, clazz, Feature.SupportAutoType);
    return null;
  }

  public static <T> T parseObject(String text, TypeReference<T> type) {
    return null;
    //return com.alibaba.fastjson.JSON.parseObject(text, type.getType(), Feature.IgnoreNotMatch);
  }

  public static JSONObject parseObject(String text) {
    return null;
    //com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSON.parseObject(text);
    //return new JSONObject(object);
  }

  public static JSONArray parseArray(String text) {
    return null;
    //com.alibaba.fastjson.JSONArray result = com.alibaba.fastjson.JSON.parseArray(text);
    //return new JSONArray(result);
  }

  public static <T> List<T> parseArray(String text, Class<T> clazz) {
    return null;
    //return com.alibaba.fastjson.JSON.parseArray(text, clazz);
  }

  public static List<Object> parseArray(String text, Type[] types) {
    return null;
    //return com.alibaba.fastjson.JSON.parseArray(text, types);
  }

  public static Object parse(String text) {
    return null;
    //return com.alibaba.fastjson.JSON.parse(text);
  }

  public static <T> T toJavaObject(JSONObject json, Class<T> clazz) {
    return null;
    //return com.alibaba.fastjson.JSON.toJavaObject(json.getRawObject(), clazz);
  }

  public static String toJSONString(Object object) {
//    SerializeFilter[] filters = {ObjectValueFilter.instance};
//    if (AVOSCloud.isEnableCircularReferenceDetect()) {
//      return com.alibaba.fastjson.JSON.toJSONString(object, filters,
//              SerializerFeature.WriteClassName, SerializerFeature.QuoteFieldNames, SerializerFeature.WriteMapNullValue,
//              SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero);
//    } else {
//      return com.alibaba.fastjson.JSON.toJSONString(object, filters,
//              SerializerFeature.WriteClassName, SerializerFeature.QuoteFieldNames, SerializerFeature.WriteMapNullValue,
//              SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero,
//              SerializerFeature.DisableCircularReferenceDetect);
//    }
    return null;
  }
}
