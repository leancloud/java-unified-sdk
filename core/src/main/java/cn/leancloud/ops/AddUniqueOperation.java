package cn.leancloud.ops;

import com.alibaba.fastjson.JSONArray;

import java.util.List;

/**
 * add unique elements to array attribute.
 */
public class AddUniqueOperation extends AddOperation {
  public AddUniqueOperation(String key, Object value) {
    super(key, value);
    this.op = "AddUnique";
  }

  @Override
  public Object apply(Object obj) {
    Object result = super.apply(obj);
    if (null != result && (result instanceof List || result instanceof JSONArray)) {
      if (result instanceof List) {
        Object[] objects = ((List)result).toArray();
        ((List)result).clear();
        for (Object o : objects) {
          if (!((List)result).contains(o)) {
            ((List)result).add(o);
          }
        }
      } else {
        Object[] objects = ((JSONArray)result).toArray();
        ((JSONArray)result).clear();
        for (Object o : objects) {
          if (!((JSONArray)result).contains(o)) {
            ((JSONArray)result).add(o);
          }
        }
      }
    }
    return result;
  }
}
