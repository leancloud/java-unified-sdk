package cn.leancloud.json;

import java.util.List;

public interface JSONParser {
  <T> T parseObject(String text, Class<T> clazz);
  <T> T parseObject(String text, TypeReference<T> type);
  JSONObject parseObject(String text);
  JSONArray parseArray(String text);
  <T> List<T> parseArray(String text, Class<T> clazz);
  Object parse(String text);
  <T> T toJavaObject(JSONObject json, Class<T> clazz);
  String toJSONString(Object object);
}
