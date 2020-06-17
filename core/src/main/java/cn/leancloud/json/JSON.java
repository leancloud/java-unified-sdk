package cn.leancloud.json;

import cn.leancloud.core.AppConfiguration;
import java.util.List;

public class JSON {
  public static <T> T parseObject(String text, Class<T> clazz) {
    return AppConfiguration.getJsonParser().parseObject(text, clazz);
  }

  public static <T> T parseObject(String text, TypeReference<T> type) {
    return AppConfiguration.getJsonParser().parseObject(text, type);
  }

  public static JSONObject parseObject(String text) {
    return AppConfiguration.getJsonParser().parseObject(text);
  }

  public static JSONArray parseArray(String text) {
    return AppConfiguration.getJsonParser().parseArray(text);
  }

  public static <T> List<T> parseArray(String text, Class<T> clazz) {
    return AppConfiguration.getJsonParser().parseArray(text, clazz);
  }

  public static Object parse(String text) {
    return AppConfiguration.getJsonParser().parse(text);
  }

  public static <T> T toJavaObject(JSONObject json, Class<T> clazz) {
    return AppConfiguration.getJsonParser().toJavaObject(json, clazz);
  }

  public static String toJSONString(Object object) {
    return AppConfiguration.getJsonParser().toJSONString(object);
  }
}
