package cn.leancloud.core.ops;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public final class NullOp implements AVOp {
  public static NullOp INSTANCE = new NullOp();

  public NullOp() {
    super();
  }

  public <T extends AVOp> T cast(Class<T> clazz) {
    throw new UnsupportedOperationException();
  }

  public Iterator<AVOp> iterator() {
    throw new UnsupportedOperationException();
  }

  public String getKey() {
    return "__ALL_POSSIABLE_KEYS";
  }

  public OpType getType() {
    return OpType.Null;
  }

  public Object apply(Object obj) {
    return obj;
  }

  public AVOp merge(AVOp other) {
    return other;
  }

  public Map<String, Object> encodeOp() {
    return Collections.emptyMap();
  }

  public int size() {
    return 0;
  }

  public AVOp get(int idx) {
    return null;
  }

  public AVOp remove(int idx) {
    return null;
  }

  public Object getValues(){
    return null;
  }
}
