package cn.leancloud.core.ops;

import java.util.HashMap;
import java.util.Map;

import cn.leancloud.utils.AVUtils;

public class SetOp extends BaseOp implements SingleValueOp {
  private Object value;

  public SetOp() {
    super();
  }

  public Object getValues() {
    return value;
  }

  void setValue(Object value) {
    this.value = value;
  }

  public SetOp(String key, Object value) {
    super(key, OpType.Set);
    this.value = value;
  }

  @Override
  public Object apply(Object obj) {
    return getValues();
  }

  public Map<String, Object> encodeOp() {
    HashMap<String, Object> hashMap = new HashMap<String, Object>();
    hashMap.put(key, AVUtils.getParsedObject(value));
    return hashMap;
  }

  @Override
  public AVOp merge(AVOp other) {
    assertKeyEquals(other);
    switch (other.type()) {
      case Null:
        return this;
      case Set:
        this.value = other.cast(SetOp.class).value;
        return this;
      case Add:
      case AddUnique:
      case Remove:
      case AddRelation:
      case RemoveRelation:
        return new CompoundOp(key, this, other);
      case Increment:
        if (!(this.value instanceof Number)) {
          throw new IllegalArgumentException("Could not increment non-numberic value.");
        }
        long v = ((Number) value).longValue();
        v += other.cast(IncrementOp.class).amount.intValue();
        this.value = v;
        return this;
      case Delete:
        return other;
      case Compound:
        other.cast(CompoundOp.class).addFirst(this);
        return other;
      default:
        throw new IllegalStateException("Unknow op type " + other.type());
    }
  }

  public void setValues(Object values) {
    this.value = values;
  }
}
