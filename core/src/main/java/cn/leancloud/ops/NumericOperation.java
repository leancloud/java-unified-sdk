package cn.leancloud.ops;

public abstract class NumericOperation extends BaseOperation {
  public NumericOperation(String op, String field, Object value) {
    super(op, field, value);
    if (getValue() instanceof Number) {
      // ok
    } else {
      throw new IllegalArgumentException("");
    }
  }
}
