package cn.leancloud.core.ops;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import cn.leancloud.core.AVFile;
import cn.leancloud.core.AVObject;
import cn.leancloud.utils.AVUtils;

public abstract class CollectionOp extends BaseOp {
  public CollectionOp() {
    super();
  }

  public CollectionOp(String key, OpType type) {
    super(key, type);
  }

  public void setValues(Collection values) {
    this.getValues().clear();
    this.getValues().addAll(values);
  };

  public abstract Collection getValues();

  public List getParsedValues() {
    List<Object> result = new LinkedList<Object>();
    for (Object v : getValues()) {
      if (v instanceof AVObject) {
        Object realValue = Utils.mapFromPointerObject((AVObject) v);
        result.add(realValue);
      } else if (v instanceof AVFile) {
        Object realValue = Utils.mapFromFile((AVFile) v);
        result.add(realValue);
      } else {
        result.add(v);
      }
    }
    return result;
  }
}
