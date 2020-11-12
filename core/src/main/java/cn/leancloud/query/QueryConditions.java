package cn.leancloud.query;

import cn.leancloud.AVObject;
import cn.leancloud.ops.Utils;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.StringUtil;

import java.util.*;

public class QueryConditions implements Cloneable {
  Map<String, List<QueryOperation>> where;
  private List<String> include;
  private Set<String> selectedKeys;
  private int limit;
  private boolean trace;
  private int skip = -1;
  private String order;
  private Map<String, String> parameters;
  private boolean includeACL = false;

  public QueryConditions() {
    where = new HashMap<String, List<QueryOperation>>();
    include = new LinkedList<String>();
    includeACL = false;
    parameters = new HashMap<String, String>();
  }

  // It’s also legal to implement clone() without using Object.clone(),
  // by manually constructing a new object and copying fields.
  public QueryConditions clone() {
    QueryConditions condition = new QueryConditions();
    condition.where.putAll(this.where);
    condition.include.addAll(this.include);
    condition.parameters.putAll(this.parameters);
    condition.selectKeys(this.selectedKeys);
    condition.setLimit(this.limit);
    condition.setTrace(this.trace);
    condition.setSkip(this.skip);
    condition.setOrder(this.order);
    condition.includeACL(this.includeACL);
    return condition;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getSkip() {
    return skip;
  }

  public void setSkip(int skip) {
    this.skip = skip;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public List<String> getInclude() {
    return include;
  }

  public void setInclude(List<String> include) {
    this.include = include;
  }

  public Set<String> getSelectedKeys() {
    return selectedKeys;
  }

  public void setSelectedKeys(Set<String> selectedKeys) {
    this.selectedKeys = selectedKeys;
  }

  public Map<String, List<QueryOperation>> getWhere() {
    return where;
  }

  public void setWhere(Map<String, List<QueryOperation>> where) {
    this.where = where;
  }

  public Map<String, String> getParameters() {
    return new HashMap<String, String>(parameters);
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public boolean isTrace() {
    return trace;
  }

  public void setTrace(boolean trace) {
    this.trace = trace;
  }

  public void addAscendingOrder(String key) {
    if (StringUtil.isEmpty(order)) {
      this.orderByAscending(key);
      return;
    }

    order = String.format("%s,%s", order, key);
  }

  public void orderByAscending(String key) {
    order = String.format("%s", key);
  }

  public void addDescendingOrder(String key) {
    if (StringUtil.isEmpty(order)) {
      orderByDescending(key);
      return;
    }

    order = String.format("%s,-%s", order, key);
  }

  public void orderByDescending(String key) {
    order = String.format("-%s", key);
  }

  public void include(String key) {
    include.add(key);
  }

  public void selectKeys(Collection<String> keys) {
    if (selectedKeys == null) {
      selectedKeys = new HashSet<String>();
    }
    if (null != keys) {
      selectedKeys.addAll(keys);
    }
  }

  /**
   * Flag to indicate need ACL returned in result.
   * @return include flag.
   */
  public boolean isIncludeACL() {
    return includeACL;
  }

  /**
   * set include ACL or not.
   * @param includeACL Flag to indicate need ACL returned in result.
   * @return this query.
   */
  public void includeACL(boolean includeACL) {
    this.includeACL = includeACL;
  }

  public Map<String, Object> compileWhereOperationMap() {
    Map<String, Object> result = new HashMap<String, Object>();
    for (Map.Entry<String, List<QueryOperation>> entry : where.entrySet()) {
      List<QueryOperation> ops = entry.getValue();
      final String key = entry.getKey();
      if (key.equals(QueryOperation.OR_OP)) {
        List<Object> opList = new ArrayList<Object>();
        for (QueryOperation op : ops) {
          opList.add(op.toResult());
        }
        List<Object> existsOr = (List<Object>) result.get(QueryOperation.OR_OP);
        if (existsOr != null) {
          existsOr.addAll(opList);
        } else {
          result.put(QueryOperation.OR_OP, opList);
        }
      } else if (key.equals(QueryOperation.AND_OP)) {
        List<Object> opList = new ArrayList<Object>();
        for (QueryOperation op : ops) {
          opList.add(op.getValue());
        }
        List<Object> existsAnd = (List<Object>) result.get(QueryOperation.AND_OP);
        if (existsAnd != null) {
          existsAnd.addAll(opList);
        } else {
          result.put(QueryOperation.AND_OP, opList);
        }
      } else {
        switch (ops.size()) {
          case 0:
            break;
          case 1:
            Iterator<QueryOperation> iterator = ops.iterator();
            while (iterator.hasNext()) {
              QueryOperation op = iterator.next();
              result.put(key, op.toResult());
            }
            break;
          default:
            List<Object> opList = new ArrayList<Object>();
            Map<String, Object> opMap = new HashMap<String, Object>();
            boolean hasEqual = false;
            for (QueryOperation op : ops) {
              opList.add(op.toResult(key));
              if (QueryOperation.EQUAL_OP.equals(op.getOp())) {
                hasEqual = true;
              }
              if (!hasEqual) {
                opMap.putAll((Map) op.toResult());
              }
            }
            if (hasEqual) {
              List<Object> existsAnd = (List<Object>) result.get("$and");
              if (existsAnd != null) {
                existsAnd.addAll(opList);
              } else {
                result.put("$and", opList);
              }
            } else {
              result.put(key, opMap);
            }
            break;
        }
      }

    }
    return result;
  }

  public Map<String, Object> assembleJsonParam() {
    Map<String, Object> result = new HashMap<>();
    if (where.keySet().size() > 0) {
      Map<String, Object> whereMaps = compileWhereOperationMap();
      result.put("where", whereMaps);
    }
    if (limit > 0) {
      result.put("limit", limit);
    }
    if (skip >= 0) {
      result.put("skip",  skip);
    }
    if (includeACL) {
      result.put("returnACL", "true");
    }
    if (!StringUtil.isEmpty(order)) {
      result.put("order", order);
    }
    if (null !=include && include.size() > 0) {
      String value = StringUtil.join(",", include);
      result.put("include", value);
    }
    if (selectedKeys != null && selectedKeys.size() > 0) {
      String keys = StringUtil.join(",", selectedKeys);
      result.put("keys", keys);
    }
    return result;
  }

  public Map<String, String> assembleParameters() {
    if (where.keySet().size() > 0) {
      Map<String, Object> whereMaps = compileWhereOperationMap();
      String whereValue = AVUtils.jsonStringFromMapWithNull(Utils.getParsedMap(whereMaps));
      parameters.put("where", whereValue);
    }
    if (limit > 0) {
      parameters.put("limit", Integer.toString(limit));
    }
    if (skip >= 0) {
      parameters.put("skip", Integer.toString(skip));
    }
    if (includeACL) {
      parameters.put("returnACL", "true");
    }
    if (!StringUtil.isEmpty(order)) {
      parameters.put("order", order);
    }
    if (null !=include && include.size() > 0) {
      String value = StringUtil.join(",", include);
      parameters.put("include", value);
    }
    if (selectedKeys != null && selectedKeys.size() > 0) {
      String keys = StringUtil.join(",", selectedKeys);
      parameters.put("keys", keys);
    }

    return parameters;
  }

  public void addWhereItem(QueryOperation op) {
    List<QueryOperation> ops = where.get(op.getKey());
    if (ops == null) {
      ops = new LinkedList<QueryOperation>();
      where.put(op.getKey(), ops);
    }
    removeDuplications(op, ops);
    ops.add(op);
  }

  public void addWhereItem(String key, String op, Object value) {
    addWhereItem(new QueryOperation(key, op, value));
  }

  private void removeDuplications(QueryOperation op, List<QueryOperation> ops) {
    Iterator<QueryOperation> it = ops.iterator();
    while (it.hasNext()) {
      QueryOperation o = it.next();
      if (o.sameOp(op)) {
        it.remove();
      }
    }
  }

  public void addOrItems(QueryOperation op) {
    List<QueryOperation> ops = where.get(QueryOperation.OR_OP);
    if (ops == null) {
      ops = new LinkedList<QueryOperation>();
      where.put(QueryOperation.OR_OP, ops);
    }

    Iterator<QueryOperation> it = ops.iterator();
    while (it.hasNext()) {
      QueryOperation o = it.next();
      if (o.equals(op)) {
        it.remove();
      }
    }

    ops.add(op);
  }

  public void addAndItems(QueryConditions conditions) {
    Map<String, Object> queryOperationMap = conditions
            .compileWhereOperationMap();
    QueryOperation op = new QueryOperation("$and", "$and", queryOperationMap);

    List<QueryOperation> ops = where.get(QueryOperation.AND_OP);
    if (ops == null) {
      ops = new LinkedList<QueryOperation>();
      where.put(QueryOperation.AND_OP, ops);
    }

    Iterator<QueryOperation> it = ops.iterator();
    while (it.hasNext()) {
      QueryOperation o = it.next();
      if (o.equals(op)) {
        it.remove();
      }
    }
    ops.add(op);
  }

  public void whereWithinRadians(String key, AVGeoPoint point, double maxDistance) {
    this.whereWithinRadians(key, point, maxDistance, -1);
  }

  public void whereWithinRadians(String key, AVGeoPoint point, double maxDistance,
                                 double minDistance) {
    Map<String, Object> map = AVUtils.createMap("$nearSphere", Utils.mapFromGeoPoint(point));
    if (maxDistance >= 0) {
      map.put("$maxDistanceInRadians", maxDistance);
    }
    if (minDistance >= 0) {
      map.put("$minDistanceInRadians", minDistance);
    }
    addWhereItem(new QueryOperation(key, null, map));
  }

  public void whereGreaterThanOrEqualTo(String key, Object value) {
    addWhereItem(new QueryOperation(key, "$gte", value));
  }

  public void whereContainedIn(String key, Collection<? extends Object> values) {
    this.addWhereItem(key, "$in", values);
  }

  public void whereExists(String key) {
    addWhereItem(key, "$exists", true);
  }

  public void whereGreaterThan(String key, Object value) {
    addWhereItem(key, "$gt", value);
  }

  public void whereLessThan(String key, Object value) {
    addWhereItem(key, "$lt", value);
  }

  public void whereLessThanOrEqualTo(String key, Object value) {
    addWhereItem(key, "$lte", value);
  }

  public void whereMatches(String key, String regex) {
    addWhereItem(key, "$regex", regex);
  }

  public void whereMatches(String key, String regex, String modifiers) {
    addWhereItem(key, "$regex", regex);
    addWhereItem(key, "$options", modifiers);
  }

  public void whereNear(String key, AVGeoPoint point) {
    this.addWhereItem(key, "$nearSphere", Utils.mapFromGeoPoint(point));
  }

  public void whereNotContainedIn(String key, Collection<? extends Object> values) {
    this.addWhereItem(key, "$nin", values);
  }

  public void whereNotEqualTo(String key, Object value) {
    this.addWhereItem(key, "$ne", value);
  }

  public void whereEqualTo(String key, Object value) {
    if (value instanceof AVObject) {
      addWhereItem(key, QueryOperation.EQUAL_OP, Utils.mapFromPointerObject((AVObject) value));
    } else {
      addWhereItem(key, QueryOperation.EQUAL_OP, value);
    }
  }

  public void whereStartsWith(String key, String prefix) {
    this.whereMatches(key, String.format("^%s.*", prefix));
  }

  public void whereWithinGeoBox(String key, AVGeoPoint southwest, AVGeoPoint northeast) {
    List<Map<String, Object>> box = new LinkedList<Map<String, Object>>();
    box.add(Utils.mapFromGeoPoint(southwest));
    box.add(Utils.mapFromGeoPoint(northeast));
    Map<String, Object> map = AVUtils.createMap("$box", box);
    this.addWhereItem(key, "$within", map);
  }

  public void whereWithinKilometers(String key, AVGeoPoint point, double maxDistance) {
    this.whereWithinKilometers(key, point, maxDistance, -1);
  }

  public void whereWithinKilometers(String key, AVGeoPoint point, double maxDistance,
                                    double minDistance) {
    Map<String, Object> map = AVUtils.createMap("$nearSphere", Utils.mapFromGeoPoint(point));
    if (maxDistance >= 0) {
      map.put("$maxDistanceInKilometers", maxDistance);
    }
    if (minDistance >= 0) {
      map.put("$minDistanceInKilometers", minDistance);
    }
    addWhereItem(key, null, map);
  }

  public void whereWithinMiles(String key, AVGeoPoint point, double maxDistance) {
    this.whereWithinMiles(key, point, maxDistance, -1);
  }


  public void whereWithinMiles(String key, AVGeoPoint point, double maxDistance,
                               double minDistance) {
    Map<String, Object> map = AVUtils.createMap("$nearSphere", Utils.mapFromGeoPoint(point));
    if (maxDistance >= 0) {
      map.put("$maxDistanceInMiles", maxDistance);
    }
    if (minDistance >= 0) {
      map.put("$minDistanceInMiles", minDistance);
    }
    addWhereItem(key, null, map);
  }

  public void whereEndsWith(String key, String suffix) {
    this.whereMatches(key, String.format(".*%s$", suffix));
  }

  public void whereContains(String key, String substring) {
    String regex = String.format(".*%s.*", substring);
    whereMatches(key, regex);
  }

  public void whereSizeEqual(String key, int size) {
    this.addWhereItem(key, "$size", size);
  }

  public void whereContainsAll(String key, Collection<?> values) {
    addWhereItem(key, "$all", values);
  }

  public void whereDoesNotExist(String key) {
    addWhereItem(key, "$exists", false);
  }
}
