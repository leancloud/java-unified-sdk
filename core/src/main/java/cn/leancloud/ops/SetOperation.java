package cn.leancloud.ops;

import com.alibaba.fastjson.annotation.JSONType;

import java.util.HashMap;
import java.util.Map;

/**
 * set attribute to a definitive value.
 */
public class SetOperation extends BaseOperation {
  public SetOperation(String field, Object value) {
    super("Set", field, value, true);
  }

  public Object apply(Object obj) {
    return getValue();
  }

  public Map<String, Object> encode() {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(getField(), encodeObject(getValue()));
    return result;
  }
}
