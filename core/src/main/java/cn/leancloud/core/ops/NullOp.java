package cn.leancloud.core.ops;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class NullOp implements AVOp {
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

  public String key() {
    return "__ALL_POSSIABLE_KEYS";
  }

  public OpType type() {
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
    // TODO Auto-generated method stub
    return 0;
  }

  public AVOp get(int idx) {
    // TODO Auto-generated method stub
    return null;
  }

  public AVOp remove(int idx) {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getValues(){
    return null;
  }
}
