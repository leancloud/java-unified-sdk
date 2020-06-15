package cn.leancloud.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class JSON {
  public static <T> T parseObject(String text, Class<T> clazz) {
    return ConverterUtils.parseObject(text, clazz);
  }

  public static <T> T parseObject(String text, TypeReference<T> type) {
    return ConverterUtils.parseObject(text, type.getType());
  }

  public static JSONObject parseObject(String text) {
    JsonObject jsonObject = ConverterUtils.parseObject(text, JsonObject.class);
    return new JSONObject(jsonObject);
  }

  public static JSONArray parseArray(String text) {
    JsonArray jsonArray = ConverterUtils.parseObject(text, JsonArray.class);
    return new JSONArray(jsonArray);
  }

  public static <T> List<T> parseArray(String text, Class<T> clazz) {
    JsonArray jsonArray = ConverterUtils.parseObject(text, JsonArray.class);
    List<T> list = new ArrayList<>(jsonArray.size());
    for (int i = 0;i < jsonArray.size(); i++) {
      list.add(ConverterUtils.toJavaObject(jsonArray.get(i), clazz));
    }
    return list;
  }

  public static Object parse(String text) {
    return ConverterUtils.parseObject(text);
  }

  public static <T> T toJavaObject(JSONObject json, Class<T> clazz) {
    return ConverterUtils.toJavaObject(json.getRawObject(), clazz);
  }

  public static String toJSONString(Object object) {
    return ConverterUtils.getGsonInstance().toJson(object);
  }
}
