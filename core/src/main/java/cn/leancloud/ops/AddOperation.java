package cn.leancloud.ops;

import cn.leancloud.json.JSONArray;

import java.util.*;

/**
 * add new elements to one array attribute.
 */
public class AddOperation extends BaseOperation {
  public AddOperation(String field, Object value) {
    super("Add", field, null, false);
    this.value = new ArrayList<>();
    if (null == value) {
      return;
    }
    if (!(value instanceof Collection)) {
      ((List)this.value).add(value);
    } else {
      ((List)this.value).addAll((Collection) value);
    }
  }

  public Object apply(Object obj) {
    if (null == obj) {
      return getValue();
    } else if (obj instanceof List || obj instanceof JSONArray) {
      if (this.value instanceof List) {
        ((Collection)obj).addAll((List)this.value);
      } else {
        ((Collection)obj).add(this.value);
      }
    } else {
      LOGGER.w("cannot apply AddOperation on non list attribute. targetValueType=" + obj.getClass().getSimpleName());
    }
    return obj;
  }

  @Override
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    if (other instanceof SetOperation || other instanceof DeleteOperation) {
      return other;
    } else if (other instanceof AddOperation) {
      this.value = concatCollections(this.value, ((AddOperation) other).value);
      return this;
    } else if (other instanceof AddUniqueOperation) {
      this.value = concatCollections(this.value, ((AddOperation) other).value);
      return this;
    } else if (other instanceof RemoveOperation) {
      return new CompoundOperation(this.field, other, this);
    } else if (other instanceof CompoundOperation) {
      return ((CompoundOperation) other).mergeWithPrevious(this);
    } else if (other instanceof AddRelationOperation || other instanceof RemoveRelationOperation
            || other instanceof BitAndOperation || other instanceof BitOrOperation || other instanceof BitXOROperation
            || other instanceof IncrementOperation || other instanceof DecrementOperation) {
      reportIllegalOperations(this, other);
    }

    return NullOperation.gInstance;
  }

  public Map<String, Object> encode() {
    //{"attr":{"__op":"Add", "objects":[obj1, obj2]}}
    Map<String, Object> opMap = new HashMap<String, Object>(2);
    opMap.put(BaseOperation.KEY_OP, this.getOperation());
    opMap.put(BaseOperation.KEY_OBJECTS, encodeObject(this.getValue()));
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(getField(), opMap);
    return result;
  }
}
