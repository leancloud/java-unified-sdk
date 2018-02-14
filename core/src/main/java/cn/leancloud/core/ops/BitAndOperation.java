package cn.leancloud.core.ops;

import java.util.Map;

public class BitAndOperation extends BaseOperation {
  public BitAndOperation(String key, Object value) {
    super("", key, value);
  }
  public Object apply(Object obj) {
    return null;
  }
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    return null;
  }
  public Map<String, Object> encode() {
    return null;
  }
}
