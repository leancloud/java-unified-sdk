package cn.leancloud.core.ops;

import java.util.*;

public class RemoveOp extends CollectionOp {

  private Set<Object> values = new HashSet<Object>();

  public RemoveOp(String key, Collection<?> values) {
    super(key, OpType.Remove);
    if (values != null) {
      for (Object obj : values) {
        this.values.add(obj);
      }
    }
  }

  public RemoveOp() {
    super();
  }

  public Set<Object> getValues() {
    return values;
  }

  public Map<String, Object> encodeOp() {
    return Utils.createArrayOpMap(key, "Remove", this.getParsedValues());
  }

  @Override
  public Object apply(Object oldValue) {
    List<Object> result = new LinkedList<Object>();
    if (oldValue != null) {
      result.addAll((Collection) oldValue);
    }
    if (getValues() != null) {
      result.removeAll(getValues());
    }
    return result;
  }

  @Override
  public AVOp merge(AVOp other) {
    assertKeyEquals(other);
    switch (other.type()) {
      case Null:
        return this;
      case Set:
        return other;
      case Remove:
        this.values.addAll(other.cast(RemoveOp.class).values);
        return this;
      case AddUnique:
      case Add:
      case AddRelation:
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
        throw new IllegalStateException("Unknow op type " + other.type());
    }
  }
}
