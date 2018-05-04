package cn.leancloud.ops;

import java.util.Map;

public final class NullOperation extends BaseOperation {
  public static final NullOperation gInstance = new NullOperation("nothing", null);

  public NullOperation(String key, Object value) {
    super("Null", key, value, false);
  }
  public Object apply(Object obj) {
    return obj;
  }
  @Override
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    return other;
  }
  public Map<String, Object> encode() {
    return null;
  }
}
