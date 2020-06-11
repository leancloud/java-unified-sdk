package cn.leancloud.json;

import cn.leancloud.core.AVOSCloud;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Type;
import java.util.List;

import static com.alibaba.fastjson.parser.Feature.IgnoreNotMatch;

public class JSON {
  public static <T> T parseObject(String text, Class<T> clazz) {
    return com.alibaba.fastjson.JSON.parseObject(text, clazz, Feature.SupportAutoType);
  }

  public static <T> T parseObject(String text, TypeReference<T> type) {
    return com.alibaba.fastjson.JSON.parseObject(text, type.getType(), IgnoreNotMatch);
  }

  public static JSONObject parseObject(String text) {
    com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSON.parseObject(text);
    return new JSONObject(object);
  }

  public static JSONArray parseArray(String text) {
    return com.alibaba.fastjson.JSON.parseArray(text);
  }

  public static <T> List<T> parseArray(String text, Class<T> clazz) {
    return com.alibaba.fastjson.JSON.parseArray(text, clazz);
  }

  public static List<Object> parseArray(String text, Type[] types) {
    return com.alibaba.fastjson.JSON.parseArray(text, types);
  }

  public static Object parse(String text) {
    return com.alibaba.fastjson.JSON.parse(text);
  }

  public static String toJSONString(Object object) {
    if (AVOSCloud.isEnableCircularReferenceDetect()) {
      return com.alibaba.fastjson.JSON.toJSONString(object, ObjectValueFilter.instance,
              SerializerFeature.WriteClassName, SerializerFeature.QuoteFieldNames, SerializerFeature.WriteMapNullValue,
              SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero);
    } else {
      return com.alibaba.fastjson.JSON.toJSONString(object, ObjectValueFilter.instance,
              SerializerFeature.WriteClassName, SerializerFeature.QuoteFieldNames, SerializerFeature.WriteMapNullValue,
              SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero,
              SerializerFeature.DisableCircularReferenceDetect);
    }
  }
}
