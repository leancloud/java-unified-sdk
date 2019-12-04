package cn.leancloud.ops;

public class DecrementOperation extends NumericOperation {
  public DecrementOperation(String key, Object value) {
    super("Decrement", key, value);
  }
}
