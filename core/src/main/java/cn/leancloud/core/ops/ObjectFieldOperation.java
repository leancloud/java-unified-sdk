package cn.leancloud.core.ops;

import java.util.Map;

public interface ObjectFieldOperation {
  String getOperation();
  String getField();
  Object getValue();
  Object apply(Object obj);
  ObjectFieldOperation merge(ObjectFieldOperation other);
  Map<String, Object> encode();
}
