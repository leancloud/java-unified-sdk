package cn.leancloud.ops;

import java.util.Map;

public class DecrementOperation extends NumericOperation {
  public DecrementOperation(String key, Object value) {
    super("Decrement", key, value);
  }
}
