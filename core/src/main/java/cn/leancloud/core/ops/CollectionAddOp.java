package cn.leancloud.core.ops;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class CollectionAddOp extends CollectionOp {
  public CollectionAddOp() {
    super();
  }

  public CollectionAddOp(String key, OpType type) {
    super(key, type);
  }

  public Object apply(Object oldValue) {
    List<Object> result = new LinkedList<Object>();
    if (oldValue != null) {
      result.addAll((Collection) oldValue);
    }
    if (getValues() != null) {
      result.addAll(getValues());
    }
    return result;
  }
}
