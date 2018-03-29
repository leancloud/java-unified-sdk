package cn.leancloud.ops;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class CompoundOperation extends BaseOperation {
  private List<ObjectFieldOperation> operations = new LinkedList<ObjectFieldOperation>();
  public CompoundOperation(String field) {
    super("Compound", field, null, false);
  }
  public CompoundOperation(String field, ObjectFieldOperation... ops) {
    this(field);
    operations.addAll(Arrays.asList(ops));
  }

  public Object apply(Object obj) {
    for (ObjectFieldOperation op: operations) {
      obj = op.apply(obj);
    }
    return obj;
  }

  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation previous) {
    operations.add(previous);
    return this;
  }

  public Map<String, Object> encode() {
    // FIXME: bug for encode.
    Map<String, Object> params = new HashMap<String, Object>();
    List<Map<String, Object>> operationList = new ArrayList<Map<String, Object>>(operations.size());
    for (ObjectFieldOperation op : operations) {
      Map<String, Object> opMap = new HashMap<String, Object>(2);
      opMap.put(BaseOperation.KEY_OP, op.getOperation());
      opMap.put(BaseOperation.KEY_OBJECTS, encodeObject(op.getValue()));
      operationList.add(opMap);
    }

    Map<String, Object> result = new HashMap<String, Object>(1);
    result.put(getField(), operationList);
    return result;
  }
}
