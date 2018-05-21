package cn.leancloud.ops;

import com.alibaba.fastjson.JSONArray;

import java.util.*;

public class RemoveOperation extends BaseOperation {
  public RemoveOperation(String key, Object value) {
    super("Remove", key, value, false);
    if (!(value instanceof Collection)) {
      this.value = Arrays.asList(value);
    }
  }

  public Object apply(Object obj) {
    if (null == obj) {
      return null;
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
    } else if (other instanceof AddOperation || other instanceof AddUniqueOperation) {
      return new CompoundOperation(this.field, other, this);
    } else if (other instanceof RemoveOperation) {
      this.value = concatCollections(this.value, ((RemoveOperation) other).value);
      return this;
    } else if (other instanceof CompoundOperation) {
      return ((CompoundOperation) other).mergeWithPrevious(this);
    } else {
      reportIllegalOperations(this, other);
    }

    return NullOperation.gInstance;
  }
  public Map<String, Object> encode() {
    //{"attr":{"__op":"Remove", "objects":[obj1, obj2]}}
    Map<String, Object> opMap = new HashMap<String, Object>(2);
    opMap.put(BaseOperation.KEY_OP, this.getOperation());
    opMap.put(BaseOperation.KEY_OBJECTS, encodeObject(this.getValue()));

    Map<String, Object> result = new HashMap<String, Object>();
    result.put(getField(), opMap);
    return result;
  }
}
