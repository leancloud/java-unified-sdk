package cn.leancloud.ops;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CompoundOperation extends BaseOperation {
  private List<ObjectFieldOperation> operations = new LinkedList<ObjectFieldOperation>();
  public CompoundOperation(String field) {
    super("", field, null);
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
    JSONArray array = null;
    JSONObject result = null;
    return null;
  }
}
