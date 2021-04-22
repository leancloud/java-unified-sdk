package cn.leancloud.ops;

import cn.leancloud.LCObject;

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

  public List<ObjectFieldOperation> getSubOperations() {
    return this.operations;
  }

  @Override
  public boolean checkCircleReference(Map<LCObject, Boolean> markMap) {
    boolean result = false;
    for (ObjectFieldOperation op : operations) {
      result = result || op.checkCircleReference(markMap);
    }
    return result;
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

  public List<Map<String, Object>> encodeRestOp(LCObject parent) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    if (null == parent) {
      return result;
    }
    String requestEndPoint = parent.getRequestRawEndpoint();
    String requestMethod = parent.getRequestMethod();
    for (int i = 1; i < this.operations.size(); i++) {
      ObjectFieldOperation tmp = this.operations.get(i);
      Map<String, Object> tmpOp = tmp.encode();

      Map<String, Object> tmpResult = Utils.makeCompletedRequest(parent.getObjectId(), requestEndPoint, requestMethod, tmpOp);
      if (null != tmpResult) {
        result.add(tmpResult);
      }
    }
    return result;
  }

  private Map<String, Object> encodeHeadOp() {
    if (this.operations.size() < 1) {
      return null;
    }
    // just return the first Operation.
    return this.operations.get(0).encode();
  }

  public Map<String, Object> encode() {
    return encodeHeadOp();
  }
}
