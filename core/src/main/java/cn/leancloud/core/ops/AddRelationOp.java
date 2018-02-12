package cn.leancloud.core.ops;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.leancloud.core.AVObject;

public class AddRelationOp extends CollectionAddOp {

  private Set<AVObject> values = new HashSet<AVObject>();

  public AddRelationOp() {
    super();
  }

  public Set<AVObject> getValues() {
    return values;
  }

  public AddRelationOp(String key, AVObject... values) {
    super(key, OpType.AddRelation);
    if (values != null) {
      for (AVObject obj : values) {
        this.values.add(obj);
      }
    }
  }

  @Override
  public Object apply(Object oldValue) {
    // 这个从理论上存在一个问题是，AVRelation永远没法被从无到有创建出来.
    // 但是由于可以依赖parentTargetClass，所以并没有影响
    return oldValue;
  }

  public Map<String, Object> encodeOp() {
    return Utils.createPointerArrayOpMap(key, this.type.name(), getValues());
  }

  @Override
  public AVOp merge(AVOp other) {
    assertKeyEquals(other);
    switch (other.getType()) {
      case Null:
        return this;
      case Set:
        return other;
      case AddRelation:
        this.values.addAll(other.cast(AddRelationOp.class).values);
        return this;
      case AddUnique:
      case Remove:
      case Add:
      case RemoveRelation:
        return new CompoundOp(key, this, other);
      case Increment:
        throw new UnsupportedOperationException("Could not increment an non-numberic value.");
      case Delete:
        return other;
      case Compound:
        other.cast(CompoundOp.class).addFirst(this);
        return other;
      default:
        throw new IllegalStateException("Unknow op type " + other.getType());
    }
  }
}
