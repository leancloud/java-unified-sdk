package cn.leancloud;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ResponseUtil {
  private static final String TYPE = "__type";

  public static String filterResponse(String response) {
    JSONObject resp = JSON.parseObject(response, JSONObject.class);
    Object result = resp.get("result");
    if (result instanceof JSONObject) {
      removeType((JSONObject) result);
    } else if (result instanceof JSONArray) {
      for (Object o : (JSONArray) result) {
        if (o instanceof JSONObject) {
          removeType((JSONObject) o);
        }
      }
    }
    return null;//AVUtils.restfulCloudData(resp);
  }

  private static void removeType(JSONObject object) {
    if (object.containsKey("className") && object.containsKey(TYPE)) {
      object.remove("className");
      object.remove(TYPE);
    }
  }
}
