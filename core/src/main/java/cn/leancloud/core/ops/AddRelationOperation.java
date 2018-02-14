package cn.leancloud.core.ops;

import java.util.Map;

public class AddRelationOperation extends BaseOperation {
  public AddRelationOperation(String key, Object value) {
    super("", key, value);
  }
  public Object apply(Object obj) {
    return null;
  }
  @Override
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    return null;
  }
  public Map<String, Object> encode() {
    return null;
  }
}
