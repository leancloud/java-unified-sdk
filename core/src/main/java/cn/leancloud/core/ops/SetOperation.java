package cn.leancloud.core.ops;

import java.util.HashMap;
import java.util.Map;

public class SetOperation extends BaseOperation {
  public SetOperation(String field, Object value) {
    super("", field, value);
  }

  public Object apply(Object obj) {
    return getValue();
  }

  @Override
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation previous) {
    if (previous instanceof DeleteOperation) {
      return this;
    }
    if (previous instanceof SetOperation) {
      return this;
    }
    if (previous instanceof AddOperation || previous instanceof AddUniqueOperation) {
      ;
    }
    if (previous instanceof AddRelationOperation || previous instanceof RemoveRelationOperation) {
      throw new UnsupportedOperationException("");
    }
    if (previous instanceof NumericOperation) {
      if (previous instanceof IncrementOperation) {
        ;
      }
    }
    return NullOperation.INSTANCE;
  }

  public Map<String, Object> encode() {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(getField(), encodeObject(getValue()));
    return result;
  }
}
