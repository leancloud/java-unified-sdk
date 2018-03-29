package cn.leancloud.ops;

import java.util.Map;

public class IncrementOperation extends NumericOperation {
  public IncrementOperation(String key, Object value) {
    super("Increment", key, value);
  }
}
