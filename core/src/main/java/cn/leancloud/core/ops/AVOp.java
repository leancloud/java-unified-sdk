package cn.leancloud.core.ops;

import java.util.Map;

public interface AVOp extends Iterable<AVOp> {
  static enum OpType {
    Null, Set, Increment, AddUnique, Add, Remove, AddRelation, RemoveRelation, Delete, Compound
  }
  <T extends AVOp> T cast(Class<T> clazz);
  String key();
  OpType type();
  Object apply(Object obj);
  AVOp merge(AVOp other);
  int size();
  AVOp get(int idx);
  AVOp remove(int idx);
  Map<String, Object> encodeOp();
  Object getValues();
}
