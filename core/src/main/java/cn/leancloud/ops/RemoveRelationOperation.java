package cn.leancloud.ops;

import com.alibaba.fastjson.JSONArray;

import java.util.*;

public class RemoveRelationOperation extends BaseOperation {
  public RemoveRelationOperation(String key, Object value) {
    super("RemoveRelation", key, value, false);
    if (!(value instanceof Collections)) {
      this.value = Arrays.asList(value);
    }
  }

  public Object apply(Object obj) {
    if (null == obj) {
      return getValue();
    } else if (obj instanceof List || obj instanceof JSONArray) {
      if (this.value instanceof List) {
        ((Collection)obj).removeAll((List)this.value);
      } else {
        ((Collection)obj).remove(this.value);
      }
    } else {
      LOGGER.w("cannot apply AddOperation on non list attribute. targetValueType=" + obj.getClass().getSimpleName());
    }
    return obj;
  }

  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    if (other instanceof SetOperation || other instanceof DeleteOperation) {
      return other;
    } else if (other instanceof RemoveRelationOperation) {
      this.value = concatCollections(this.value, ((AddRelationOperation) other).value);
      return this;
    } else if (other instanceof AddRelationOperation) {
      return new CompoundOperation(this.field, this, other);
    } else if (other instanceof CompoundOperation) {
      return ((CompoundOperation) other).mergeWithPrevious(this);
    } else {
      reportIllegalOperations(this, other);
      return NullOperation.INSTANCE;
    }
  }

  public Map<String, Object> encode() {
    // {"attr":{"__op":'AddRelation', 'objects':[pointer('_User','558e20cbe4b060308e3eb36c')]}}
    Map<String, Object> opMap = new HashMap<String, Object>();
    opMap.put(BaseOperation.KEY_OP, this.getOperation());
    opMap.put(BaseOperation.KEY_OBJECTS, encodeObject(this.getValue()));

    Map<String, Object> result = new HashMap<String, Object>();
    result.put(getField(), opMap);
    return result;
  }
}
