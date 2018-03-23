package cn.leancloud.ops;

import java.util.Map;

public class AddUniqueOperation extends BaseOperation {
  public AddUniqueOperation(String key, Object value) {
    super("AddUnique", key, value);
  }
  public Object apply(Object obj) {
    return null;
  }
  public ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    return null;
  }
  public Map<String, Object> encode() {
    return null;
  }
}
