package cn.leancloud.ops;

import cn.leancloud.json.JSONArray;

import java.util.*;

/**
 * add element to relation attribute.
 */
public class AddRelationOperation extends BaseOperation {
  public AddRelationOperation(String key, Object value) {
    super("AddRelation", key, null, false);
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
      LOGGER.w("cannot apply AddRelationOperation on non list attribute. targetValueType=" + obj.getClass().getSimpleName());
    }
    return obj;
  }

  @Override
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    if (other instanceof SetOperation || other instanceof DeleteOperation) {
      return other;
    } else if (other instanceof AddRelationOperation) {
      this.value = concatCollections(this.value, ((AddRelationOperation) other).value);
      return this;
    } else if (other instanceof RemoveRelationOperation) {
      return new CompoundOperation(this.field, other, this);
    } else if (other instanceof CompoundOperation) {
      return ((CompoundOperation) other).mergeWithPrevious(this);
    } else {
      reportIllegalOperations(this, other);
      return NullOperation.gInstance;
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
