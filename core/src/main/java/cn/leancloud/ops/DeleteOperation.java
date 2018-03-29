package cn.leancloud.ops;

import java.util.HashMap;
import java.util.Map;

public class DeleteOperation extends BaseOperation {
  DeleteOperation(String key) {
    super("Delete", key, null, true);
  }

  public Object apply(Object obj) {
    return null;
  }

  public Map<String, Object> encode() {
    // {"downvotes":{"__op":"Delete"}}

    Map<String, Object> opMap = new HashMap<String, Object>();
    opMap.put(BaseOperation.KEY_OP, this.getOperation());

    Map<String, Object> result = new HashMap<String, Object>();
    result.put(this.getField(), opMap);
    return result;
  }
}
