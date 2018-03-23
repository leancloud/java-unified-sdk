package cn.leancloud.ops;

import com.alibaba.fastjson.JSONArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddOperation extends BaseOperation {
  public AddOperation(String field, Object value) {
    super("Add", field, value);
  }

  public Object apply(Object obj) {
    if (null == obj) {
      return getValue();
    } else if (obj instanceof List || obj instanceof JSONArray) {
      if (this.value instanceof List) {
        ((Collection)obj).addAll((List)this.value);
      } else {
        ((Collection)obj).add(this.value);
      }
    }
    return obj;
  }

  @Override
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    return null;
  }
  public Map<String, Object> encode() {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(BaseOperation.KEY_OP, this.getOperation());
    result.put(BaseOperation.KEY_OBJECTS, this.getValue());
    return result;
  }
}
