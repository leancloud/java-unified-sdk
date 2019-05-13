package cn.leancloud.push.lite;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;

import org.json.JSONArray;
import org.json.JSONObject;

public class ObjectValueFilter implements ValueFilter {
  public static final ObjectValueFilter instance = new ObjectValueFilter();

  @Override
  public Object process(Object object, String name, Object value) {
    if (value instanceof JSONObject || value instanceof JSONArray) {
      return JSON.parse(value.toString());
    }
    return value;
  }
}
