package cn.leancloud.core.ops;

import com.alibaba.fastjson.annotation.JSONType;

import java.util.HashMap;
import java.util.Map;

@JSONType(ignores = {"amount"})
public class IncrementOp extends BaseOp implements SingleValueOp {

  protected Number amount;

  public IncrementOp() {
    super();
  }

  public void setValues(Object values) {
    this.amount = (Number) values;
  }

  public Number getAmount() {
    return amount;
  }

  public void setAmount(Number amount) {
    this.amount = amount;
  }

  public IncrementOp(String key, Number amount) {
    super(key, OpType.Increment);
    this.amount = amount;
  }

  public Map<String, Object> encodeOp() {
    HashMap<String, Object> subMap = new HashMap<String, Object>();
    subMap.put("__op", "Increment");
    subMap.put("amount", this.amount.longValue());
    HashMap<String, Object> hashMap = new HashMap<String, Object>();
    hashMap.put(key, subMap);
    return hashMap;
  }

  @Override
  public AVOp merge(AVOp other) {
    assertKeyEquals(other);
    switch (other.getType()) {
      case Set:
      case Delete:
        return other;
      case Increment:
        this.amount = this.amount.intValue() + other.cast(IncrementOp.class).amount.intValue();
        return this;
      case Compound:
        other.cast(CompoundOp.class).addFirst(this);
        return other;
      case Add:
      case AddUnique:
      case Remove:
        return new CompoundOp(key, this, other);
      case AddRelation:
      case RemoveRelation:
        throw new UnsupportedOperationException(
                "Could not add or remove relation on an numberic value.");
      case Null:
        return this;
      default:
        throw new IllegalStateException("Unknow op type " + other.getType());
    }
  }

  public Number getValues() {
    return amount;
  }

  public Number apply(Object value) {
    Number result = 0L;
    if (value == null) {
      return amount;
    } else if (value instanceof Double || value instanceof Float || amount instanceof Double
            || amount instanceof Float) {
      return ((Number) value).doubleValue() + amount.doubleValue();
    } else {
      return ((Number) value).longValue() + amount.longValue();

    }
  }
}
