package cn.leancloud.core.ops;

import java.util.Map;

public interface AVOp extends Iterable<AVOp> {
  static enum OpType {
    Null, Set, Increment, AddUnique, Add, Remove, AddRelation, RemoveRelation, Delete, Compound
  }

  <T extends AVOp> T cast(Class<T> clazz);
  String getKey();
  OpType getType();
  Object getValues();

  Object apply(Object obj);

  int size();
  AVOp get(int idx);
  AVOp merge(AVOp other);
  AVOp remove(int idx);

  Map<String, Object> encodeOp();
}
