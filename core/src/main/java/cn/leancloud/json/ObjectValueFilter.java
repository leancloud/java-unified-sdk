package cn.leancloud.json;

/**
 * 这个类主要是用来解决fastjson 遇到org.json.JSONObject与org.json.JSONArray没法正确序列化的问题
 * Created by lbt05 on 6/2/15.
 */
public class ObjectValueFilter {
  public static final ObjectValueFilter instance = new ObjectValueFilter();

  public Object process(Object object, String name, Object value) {
    if (value instanceof JSONObject || value instanceof JSONArray) {
      return JSON.parse(value.toString());
    }
    return value;
  }
}