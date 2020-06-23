package cn.leancloud.ops;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
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
    List<Object> listResult = null;
    if (null != result && (result instanceof List || result instanceof JSONArray)) {
      if (result instanceof List) {
        Object[] objects = ((List)result).toArray();
        listResult = new ArrayList<>();
        for (Object o : objects) {
          if (listResult.contains(o)) {
            continue;
          }
          listResult.add(o);
        }
      } else {
        Object[] objects = ((JSONArray)result).toArray();
        ((JSONArray)result).clear();
        listResult = new ArrayList<>();
        for (Object o : objects) {
          if (listResult.contains(o)) {
            continue;
          }
          listResult.add(o);
        }
      }
    }
    return listResult;
  }
}
