package cn.leancloud.ops;

import java.util.Map;

/**
 * add unique elements to array attribute.
 */
public class AddUniqueOperation extends AddOperation {
  public AddUniqueOperation(String key, Object value) {
    super(key, value);
    this.op = "AddUnique";
  }
}
