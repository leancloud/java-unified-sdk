package cn.leancloud.core.ops;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CompoundOp extends CollectionOp {
  public Map<String, Object> encodeOp() {
    throw new UnsupportedOperationException();
  }

  public CompoundOp() {
    super();
  }

  public CompoundOp(String key, AVOp... avOps) {
    super(key, OpType.Compound);
    this.ops = new LinkedList<AVOp>();
    if (avOps != null) {
      for (AVOp op : avOps) {
        this.ops.add(op);
      }
    }
  }

  public void addFirst(AVOp object) {
    ops.addFirst(object);
  }

  public void addLast(AVOp object) {
    ops.addLast(object);
  }

  public AVOp removeFirst() {
    return ops.removeFirst();
  }

  public AVOp removeLast() {
    return ops.removeLast();
  }

  public List<AVOp> getValues() {
    return ops;
  }

  public Object apply(Object oldValue) {
    for (AVOp op : ops) {
      oldValue = op.apply(oldValue);
    }
    return oldValue;
  }
}
