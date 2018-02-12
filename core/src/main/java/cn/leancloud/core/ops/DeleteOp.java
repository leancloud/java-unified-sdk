package cn.leancloud.core.ops;

import cn.leancloud.utils.AVUtils;

import java.util.Map;

public class DeleteOp extends BaseOp {
  public DeleteOp() {
    super();
  }

  public DeleteOp(String key) {
    super(key, OpType.Delete);
  }

  public Map<String, Object> encodeOp() {
    return AVUtils.createDeleteOpMap(key);
  }

  @Override
  public AVOp merge(AVOp other) {
    assertKeyEquals(other);
    switch (other.type()) {
      case Compound:
        other.cast(CompoundOp.class).addFirst(this);
        return other;
      case Set:
      case Add:
      case AddUnique:
      case AddRelation:
      case Increment:
        return other;
      case Remove:
      case RemoveRelation:
      case Null:
      case Delete:
        return this;
      default:
        throw new IllegalStateException("Unknow op type " + other.type());
    }
  }

  public Object getValues() {
    return null;
  }

  public Object apply(Object value) {
    return null;
  }
}
