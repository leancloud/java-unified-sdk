package cn.leancloud.fastjson;

import cn.leancloud.json.JSONArray;
import cn.leancloud.json.JSONObject;
import cn.leancloud.json.JSONParser;
import cn.leancloud.json.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FastJsonParser implements JSONParser {
  public <T> T parseObject(String text, Class<T> clazz) {
    return null;
  }

  public <T> T parseObject(String text, TypeReference<T> type) {
    return null;
  }

  public JSONObject parseObject(String text) {
    return null;
  }

  public JSONArray parseArray(String text) {
    return null;
  }

  public <T> List<T> parseArray(String text, Class<T> clazz) {

    return null;
  }

  public Object parse(String text) {
    return null;
  }

  public JSONObject toJSONObject(Map<String, Object> param) {
    return null;
  }

  public JSONArray toJSONArray(List<Object> list) {
    return null;
  }
  public <T> T toJavaObject(JSONObject json, Class<T> clazz) {
    return null;
  }

  public String toJSONString(Object object) {
    return null;
  }
}
