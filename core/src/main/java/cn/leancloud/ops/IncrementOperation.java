package cn.leancloud.ops;

public class IncrementOperation extends NumericOperation {
  public IncrementOperation(String key, Object value) {
    super("Increment", key, value);
  }
}
