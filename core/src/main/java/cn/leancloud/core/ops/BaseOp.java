package cn.leancloud.core.ops;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseOp implements AVOp {
  protected String key;
  protected OpType type;

  protected LinkedList<AVOp> ops = null;

  public OpType getType() {
    return this.type;
  }
  public void setType(OpType type) {
    this.type = type;
  }

  public List<AVOp> getOps() {
    return ops;
  }
  public void setOps(LinkedList<AVOp> ops) {
    this.ops = ops;
  }

  public String getKey() {
    return this.key;
  }
  public void setKey(String key) {
    this.key = key;
  }

  public BaseOp() {
    super();
  }

  public BaseOp(String key, OpType type) {
    super();
    this.key = key;
    this.type = type;
  }

  public <T extends AVOp> T cast(Class<T> clazz) {
    return clazz.cast(this);
  }

  public AVOp merge(AVOp other) {
    assertKeyEquals(other);
    if (this.ops == null) {
      this.ops = new LinkedList<AVOp>();
    }
    if (other.getType() == OpType.Compound) {
      this.ops.addAll(other.cast(CompoundOp.class).ops);
    } else {
      this.ops.add(other);
    }
    return this;
  }

  public int size() {
    return ops == null ? 0 : ops.size();
  }

  public AVOp remove(int idx) {
    if (0 <= idx && this.ops != null && this.ops.size() > idx) {
      return this.ops.remove(idx);
    } else
      return NullOp.INSTANCE;
  }

  public Object apply(Object obj) {
    if (null == obj) {
      return null;
    }
    if (this.ops != null) {
      for (AVOp op : this.ops) {
        obj = op.apply(obj);
      }
    }
    return obj;

  }

  protected void assertKeyEquals(AVOp other) {
    if (other != NullOp.INSTANCE && !other.getKey().equals(this.key)) {
      throw new IllegalArgumentException("invalid key");
    }
  }

  public Iterator<AVOp> iterator() {
    if (this.ops != null)
      return this.ops.iterator();
    else
      throw new UnsupportedOperationException();
  }

  public AVOp get(int idx) {
    if (this.ops != null) {
      if (this.ops.size() > idx)
        return this.ops.get(idx);
      else
        return NullOp.INSTANCE;
    } else if (idx == 0) {
      return this;
    } else {
      return NullOp.INSTANCE;
    }
  }
}
