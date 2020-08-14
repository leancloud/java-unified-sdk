package cn.leancloud.gson;

import cn.leancloud.json.JSONArray;
import cn.leancloud.json.JSONObject;
import cn.leancloud.json.JSONParser;
import cn.leancloud.json.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GSONParser implements JSONParser {
  public <T> T parseObject(String text, Class<T> clazz) {
    return GsonWrapper.parseObject(text, clazz);
  }

  public <T> T parseObject(String text, TypeReference<T> type) {
    return GsonWrapper.parseObject(text, type.getType());
  }

  public JSONObject parseObject(String text) {
    Map jsonObject = GsonWrapper.parseObject(text, Map.class);
    return new GsonObject(jsonObject);
  }

  public JSONArray parseArray(String text) {
    JsonArray jsonArray = GsonWrapper.parseObject(text, JsonArray.class);
    return new GsonArray(jsonArray);
  }

  public <T> List<T> parseArray(String text, Class<T> clazz) {
    JsonArray jsonArray = GsonWrapper.parseObject(text, JsonArray.class);
    List<T> list = new ArrayList<>(jsonArray.size());
    for (int i = 0;i < jsonArray.size(); i++) {
      list.add(GsonWrapper.toJavaObject(jsonArray.get(i), clazz));
    }
    return list;
  }

  public Object parse(String text) {
    return GsonWrapper.parseObject(text);
  }

  public JSONObject toJSONObject(Map<String, Object> param) {
    if (null == param) {
      return new GsonObject();
    }
    return new GsonObject(param);
  }

  public JSONArray toJSONArray(List<Object> list) {
    if (null == list) {
      return new GsonArray();
    }
    return new GsonArray(list);
  }
  public <T> T toJavaObject(JSONObject json, Class<T> clazz) {
    return GsonWrapper.toJavaObject(((GsonObject)json).getRawObject(), clazz);
  }

  public String toJSONString(Object object) {
    if (object instanceof String) {
      return (String) object;
    }
    return GsonWrapper.getGsonInstance().toJson(object);
  }
}
