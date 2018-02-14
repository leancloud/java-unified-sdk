package cn.leancloud.core.ops;

import java.util.Map;

public abstract class BaseOperation implements ObjectFieldOperation {
  static final String KEY_OP = "__op";
  static final String KEY_OBJECTS = "objects";
  static final String KEY_AMOUNT = "amount";
  static final String KEY_VALUE = "value";

  protected String op = null;
  protected String field = null;
  protected Object value = null;
  public BaseOperation(String op, String field, Object value) {
    this.op = op;
    this.field = field;
    this.value = value;
  }

  public String getOperation() {
    return this.op;
  }
  public String getField() {
    return this.field;
  }
  public Object getValue() {
    return this.value;
  }
  public abstract Object apply(Object obj);
  protected abstract ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation previous);
  public ObjectFieldOperation merge(ObjectFieldOperation other) {
    if (null == other || other instanceof NullOperation) {
      return this;
    }
    return mergeWithPrevious(other);
  }

  public abstract Map<String, Object> encode();
}
