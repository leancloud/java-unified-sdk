package cn.leancloud.query;

import java.util.HashMap;
import java.util.Map;

public class QueryOperation {
  public static final String EQUAL_OP = "__eq";
  public static final String OR_OP = "$or";
  public static final String AND_OP = "$and";

  private String key;
  private Object value;
  private String op;

  public QueryOperation(String key, String op, Object value) {
    this.key = key;
    this.op = op;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  public String getOp() {
    return op;
  }

  public Object toResult() {
    /*
     * 当查询涉及到复合查询 比如 { "$nearSphere": { "__type":"GeoPoint", "latitude":41, "longitude":-31 }
     * "maxDistanceInRadians":1000 } 此时op为空，直接返回value即可
     */
    if (op == null || op.equals(EQUAL_OP) || op.equals(OR_OP)) {
      return value;
    } else {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put(op, value);
      return map;
    }
  }

  public Object toResult(String key) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(key, this.toResult());
    return map;
  }

  public boolean sameOp(QueryOperation other) {
    if (null == other) {
      return false;
    }
    if (null != this.key && null != other.key) {
      if (!this.key.equals(other.key)) {
        return false;
      }
    } else if (null != this.key || null != other.key) {
      return false;
    }
    if (null != this.op && null != other.op) {
      if (!this.op.equals(other.op)) {
        return false;
      }
    } else if (null != this.op || null != other.op) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((op == null) ? 0 : op.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    QueryOperation other = (QueryOperation) obj;
    if (key == null) {
      if (other.key != null) return false;
    } else if (!key.equals(other.key)) return false;
    if (op == null) {
      if (other.op != null) return false;
    } else if (!op.equals(other.op)) return false;
    if (value == null) {
      if (other.value != null) return false;
    } else if (!value.equals(other.value)) return false;
    return true;
  }
}
