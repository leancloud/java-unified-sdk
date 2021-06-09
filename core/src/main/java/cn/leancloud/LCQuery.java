package cn.leancloud;

import cn.leancloud.cache.QueryResultCache;
import cn.leancloud.core.PaasClient;
import cn.leancloud.query.LCCloudQueryResult;
import cn.leancloud.query.QueryConditions;
import cn.leancloud.query.QueryOperation;
import cn.leancloud.types.LCGeoPoint;
import cn.leancloud.types.LCNull;
import cn.leancloud.utils.LCUtils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

import java.util.*;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class LCQuery<T extends LCObject> implements Cloneable {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCQuery.class);

  public enum CachePolicy {
    CACHE_ELSE_NETWORK, CACHE_ONLY, CACHE_THEN_NETWORK, IGNORE_CACHE, NETWORK_ELSE_CACHE,
    NETWORK_ONLY;
  }

  private Class<T> clazz;
  private String className;
  private java.lang.Boolean isRunning;
  private CachePolicy cachePolicy = CachePolicy.IGNORE_CACHE;
  private long maxCacheAge = -1;

  QueryConditions conditions;

  /**
   * Constructs a query. A default query with no further parameters will retrieve all AVObjects of
   * the provided class.
   *
   * @param theClassName The name of the class to retrieve AVObjects for.
   */
  public LCQuery(String theClassName) {
    this(theClassName, null);
  }

  /**
   * Clone a new query object, which fully same to this.
   *
   * @return a new AVQuery object.
   */
  public LCQuery clone() throws CloneNotSupportedException {
    LCQuery query = (LCQuery) super.clone();

    query.isRunning = false;
    query.cachePolicy = this.cachePolicy;
    query.maxCacheAge = this.maxCacheAge;
    query.conditions = null != this.conditions? this.conditions.clone(): null;
    return query;
  }

  LCQuery(String theClassName, Class<T> clazz) {
    Transformer.checkClassName(theClassName);
    this.className = theClassName;
    this.clazz = clazz;
    this.conditions = new QueryConditions();
  }

  /**
   * Constructs a query. A default query with no further parameters will retrieve all AVObjects of
   * the provided class.
   *
   * @param theClassName The name of the class to retrieve AVObjects for.
   * @param <T> template type.
   * @return the query object.
   */
  public static <T extends LCObject> LCQuery<T> getQuery(String theClassName) {
    return new LCQuery<T>(theClassName);
  }

  /**
   * Create a AVQuery with special sub-class.
   *
   * @param clazz The AVObject subclass
   * @param <T> template type.
   * @return The AVQuery
   */
  public static <T extends LCObject> LCQuery<T> getQuery(Class<T> clazz) {
    return new LCQuery<T>(Transformer.getSubClassName(clazz), clazz);
  }

  Class<T> getClazz() {
    return clazz;
  }

  void setClazz(Class<T> clazz) {
    this.clazz = clazz;
  }

  List<String> getInclude() {
    return conditions.getInclude();
  }

  void setInclude(List<String> include) {
    conditions.setInclude(include);
  }

  Set<String> getSelectedKeys() {
    return conditions.getSelectedKeys();
  }

  void setSelectedKeys(Set<String> selectedKeys) {
    conditions.setSelectedKeys(selectedKeys);
  }

  Map<String, String> getParameters() {
    return conditions.getParameters();
  }

  void setParameters(Map<String, String> parameters) {
    conditions.setParameters(parameters);
  }

  Map<String, List<QueryOperation>> getWhere() {
    return conditions.getWhere();
  }

  /**
   * Get class name
   * @return class name.
   */
  public String getClassName() {
    return className;
  }

  /**
   * Set class name
   * @param className class name.
   * @return current instance.
   */
  public LCQuery<T> setClassName(String className) {
    this.className = className;
    return this;
  }

  /**
   * Accessor for the caching policy.
   * @return cache policy
   */
  public CachePolicy getCachePolicy() {
    return cachePolicy;
  }

  /**
   * Change the caching policy of this query.
   *
   * @param cachePolicy  cache policy.
   * @return this query.
   */
  public LCQuery<T> setCachePolicy(CachePolicy cachePolicy) {
    this.cachePolicy = cachePolicy;
    return this;
  }

  /**
   * Get cache policy.
   * @return cache policy.
   */
  public CachePolicy getPolicy() {
    return cachePolicy;
  }

  /**
   * Change the caching policy of this query.
   *
   * @param policy cache policy.
   * @return this query.
   */
  public LCQuery<T> setPolicy(CachePolicy policy) {
    this.cachePolicy = policy;
    return this;
  }

  /**
   * Gets the maximum age of cached data that will be considered in this query. The returned value
   * is in milliseconds
   * @return max cache age(milliseconds).
   */
  public long getMaxCacheAge() {
    return maxCacheAge;
  }

  /**
   * Sets the maximum age of cached data that will be considered in this query.
   *
   * @param maxCacheAge  mac cached age.
   * @return this query.
   */
  public LCQuery<T> setMaxCacheAge(long maxCacheAge) {
    this.maxCacheAge = maxCacheAge;
    return this;
  }

  /**
   * Clears the cached result for all queries.
   */
  public static void clearAllCachedResults() {
    QueryResultCache.getInstance().clearAllCachedFiles();
  }

  /**
   * Removes the previously cached result for this query, forcing the next find() to hit the
   * network. If there is no cached result for this query, then this is a no-op.
   */
  public void clearCachedResult() {
    Map<String, String> query = assembleParameters();
    String cacheKey = QueryResultCache.generateKeyForQueryCondition(getClassName(), query);
    QueryResultCache.getInstance().clearCachedFile(cacheKey);

    // clear result for getFirstInBackground().
    query.put("limit", "1");
    cacheKey = QueryResultCache.generateKeyForQueryCondition(getClassName(), query);
    QueryResultCache.getInstance().clearCachedFile(cacheKey);
  }

  /**
   * Accessor for the limit.
   * @return query limit.
   */
  public int getLimit() {
    return conditions.getLimit();
  }

  /**
   * Controls the maximum number of results that are returned. Setting a negative limit denotes
   * retrieval without a limit. The default limit is 100, with a maximum of 1000 results being
   * returned at a time.
   *
   * @param limit query limit.
   * @return this query.
   */
  public LCQuery<T> setLimit(int limit) {
    conditions.setLimit(limit);
    return this;
  }

  /**
   * @see #setLimit(int)
   * @param limit query limit
   * @return this query.
   */
  public LCQuery<T> limit(int limit) {
    this.setLimit(limit);
    return this;
  }

  /**
   * @see #setSkip(int)
   * @param skip qury skip
   * @return this query.
   */
  public LCQuery<T> skip(int skip) {
    setSkip(skip);
    return this;
  }

  /**
   * Accessor for the skip value.
   * @return current skip value.
   */
  public int getSkip() {
    return conditions.getSkip();
  }

  /**
   * Controls the number of results to skip before returning any results. This is useful for
   * pagination. Default is to skip zero results.
   *
   * @param skip query skip.
   * @return this query
   */
  public LCQuery<T> setSkip(int skip) {
    conditions.setSkip(skip);
    return this;
  }

  /**
   * Get order string.
   * @return order string.
   */
  public String getOrder() {
    return conditions.getOrder();
  }

  /**
   * Set query order fields.
   *
   * @param order order string.
   * @return this query.
   */
  public LCQuery<T> setOrder(String order) {
    conditions.setOrder(order);
    return this;
  }

  /**
   * @see #setOrder(String)
   * @param order order string.
   * @return this query.
   */
  public LCQuery<T> order(String order) {
    setOrder(order);
    return this;
  }

  /**
   * Also sorts the results in ascending order by the given key. The previous sort keys have
   * precedence over this key.
   *
   * @param key The key to order by
   * @return Returns the query so you can chain this call.
   */
  public LCQuery<T> addAscendingOrder(String key) {
    conditions.addAscendingOrder(key);
    return this;
  }

  /**
   * Also sorts the results in descending order by the given key. The previous sort keys have
   * precedence over this key.
   *
   * @param key The key to order by
   * @return Returns the query so you can chain this call.
   */
  public LCQuery<T> addDescendingOrder(String key) {
    conditions.addDescendingOrder(key);
    return this;
  }

  /**
   * Flag to indicate need ACL returned in result.
   * @return include flag.
   */
  public boolean isIncludeACL() {
    return conditions.isIncludeACL();
  }

  /**
   * set include ACL or not.
   * @param includeACL Flag to indicate need ACL returned in result.
   * @return this query.
   */
  public LCQuery<T> includeACL(boolean includeACL) {
    conditions.includeACL(includeACL);
    return this;
  }

  /**
   * Include nested AVObjects for the provided key. You can use dot notation to specify which fields
   * in the included object that are also fetched.
   *
   * @param key The key that should be included.
   * @return this query.
   */
  public LCQuery<T> include(String key) {
    conditions.include(key);
    return this;
  }

  /**
   * Restrict the fields of returned AVObjects to only include the provided keys. If this is called
   * multiple times, then all of the keys specified in each of the calls will be included.
   *
   * @param keys The set of keys to include in the result.
   * @return this query.
   */
  public LCQuery<T> selectKeys(Collection<String> keys) {
    conditions.selectKeys(keys);
    return this;
  }

  /**
   * Sorts the results in ascending order by the given key.
   *
   * @param key The key to order by.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> orderByAscending(String key) {
    conditions.orderByAscending(key);
    return this;
  }

  /**
   * Sorts the results in descending order by the given key.
   *
   * @param key The key to order by.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> orderByDescending(String key) {
    conditions.orderByDescending(key);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value to be contained in the
   * provided list of values.
   *
   * @param key The key to check.
   * @param values The values that will match.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereContainedIn(String key, Collection<? extends Object> values) {
    conditions.whereContainedIn(key, values);
    return this;
  }

  /**
   * Add a constraint for finding string values that contain a provided string. This will be slow
   * for large datasets.
   *
   * @param key The key that the string to match is stored in.
   * @param substring The substring that the value must contain.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereContains(String key, String substring) {
    conditions.whereContains(key, substring);
    return this;
  }

  /**
   * 添加查询约束条件，查找key类型是数组，该数组的长度匹配提供的数值。
   *
   * @since 2.0.2
   * @param key 查询的key
   * @param size 数组的长度
   * @return this query.
   */
  public LCQuery<T> whereSizeEqual(String key, int size) {
    conditions.whereSizeEqual(key, size);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value match another AVQuery.
   * This only works on keys whose values are AVObjects or lists of AVObjects. Add a constraint to
   * the query that requires a particular key's value to contain every one of the provided list of
   * values.
   *
   * @param key The key to check. This key's value must be an array.
   * @param values The values that will match.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereContainsAll(String key, Collection<?> values) {
    conditions.whereContainsAll(key, values);
    return this;
  }

  /**
   * Add a constraint for finding objects that do not contain a given key.
   *
   * @param key The key that should not exist
   * @return this query.
   */
  public LCQuery<T> whereDoesNotExist(String key) {
    conditions.whereDoesNotExist(key);
    return this;
  }



  /**
   * Add a constraint for finding string values that end with a provided string. This will be slow
   * for large datasets.
   *
   * @param key The key that the string to match is stored in.
   * @param suffix The substring that the value must end with.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereEndsWith(String key, String suffix) {
    conditions.whereEndsWith(key, suffix);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value to be equal to the
   * provided value.
   *
   * @param key The key to check.
   * @param value The value that the AVObject must contain.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereEqualTo(String key, Object value) {
    conditions.whereEqualTo(key, value);
    return this;
  }

  private LCQuery<T> addOrItems(QueryOperation op) {
    conditions.addOrItems(op);
    return this;
  }

  private LCQuery<T> addAndItems(LCQuery query) {
    conditions.addAndItems(query.conditions);
    return this;
  }

  protected LCQuery<T> addWhereItem(String key, String op, Object value) {
    conditions.addWhereItem(key, op, value);
    return this;
  }

  /**
   * Add a constraint for finding objects that contain the given key.
   *
   * @param key The key that should exist.
   * @return this query.
   */
  public LCQuery<T> whereExists(String key) {
    conditions.whereExists(key);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value to be greater than the
   * provided value.
   *w
   * @param key The key to check.
   * @param value The value that provides an lower bound.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereGreaterThan(String key, Object value) {
    conditions.whereGreaterThan(key, value);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value to be greater or equal to
   * than the provided value.
   *
   * @param key The key to check.
   * @param value The value that provides an lower bound.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereGreaterThanOrEqualTo(String key, Object value) {
    conditions.whereGreaterThanOrEqualTo(key, value);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value to be less than the
   * provided value.
   *
   * @param key The key to check.
   * @param value The value that provides an upper bound.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereLessThan(String key, Object value) {
    conditions.whereLessThan(key, value);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value to be less or equal to
   * than the provided value.
   *
   * @param key The key to check.
   * @param value The value that provides an lower bound.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereLessThanOrEqualTo(String key, Object value) {
    conditions.whereLessThanOrEqualTo(key, value);
    return this;
  }

  /**
   * Add a regular expression constraint for finding string values that match the provided regular
   * expression. This may be slow for large datasets.
   *
   * @param key The key that the string to match is stored in.
   * @param regex The regular expression pattern to match.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereMatches(String key, String regex) {
    conditions.whereMatches(key, regex);
    return this;
  }

  /**
   * Add a regular expression constraint for finding string values that match the provided regular
   * expression. This may be slow for large datasets.
   *
   * @param key The key that the string to match is stored in.
   * @param regex The regular expression pattern to match.
   * @param modifiers Any of the following supported PCRE modifiers: i - Case insensitive search m -
   *          Search across multiple lines of input
   * @return this query.
   */
  public LCQuery<T> whereMatches(String key, String regex, String modifiers) {
    conditions.whereMatches(key, regex, modifiers);
    return this;
  }

  /**
   * Add a proximity based constraint for finding objects with key point values near the point
   * given.
   *
   * @param key The key that the AVGeoPoint is stored in.
   * @param point The reference AVGeoPoint that is used.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereNear(String key, LCGeoPoint point) {
    conditions.whereNear(key, point);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value not be contained in the
   * provided list of values.
   *
   * @param key The key to check.
   * @param values The values that will not match.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereNotContainedIn(String key, Collection<? extends Object> values) {
    conditions.whereNotContainedIn(key, values);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value to be not equal to the
   * provided value.
   *
   * @param key The key to check.
   * @param value The value that must not be equalled.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereNotEqualTo(String key, Object value) {
    conditions.whereNotEqualTo(key, value);
    return this;
  }

  /**
   * Add a constraint for finding string values that start with a provided string. This query will
   * use the backend index, so it will be fast even for large datasets.
   *
   * @param key The key that the string to match is stored in.
   * @param prefix The substring that the value must start with.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereStartsWith(String key, String prefix) {
    conditions.whereStartsWith(key, prefix);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's coordinates be contained within
   * a given rectangular geographic bounding box.
   *
   * @param key The key to be constrained.
   * @param southwest The lower-left inclusive corner of the box.
   * @param northeast The upper-right inclusive corner of the box.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereWithinGeoBox(String key, LCGeoPoint southwest, LCGeoPoint northeast) {
    conditions.whereWithinGeoBox(key, southwest, northeast);
    return this;
  }

  /**
   * Add a proximity based constraint for finding objects with key point values near the point given
   * and within the maximum distance given. Radius of earth used is 6371.0 kilometers.
   *
   * @param key The key that the AVGeoPoint is stored in.
   * @param point The reference AVGeoPoint that is used.
   * @param maxDistance Maximum distance (in kilometers) of results to return.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereWithinKilometers(String key, LCGeoPoint point, double maxDistance) {
    conditions.whereWithinKilometers(key, point, maxDistance);
    return this;
  }

  /**
   * Add a proximity based constraint for finding objects with key point values near the point given
   * and within the given ring area
   *
   * Radius of earth used is 6371.0 kilometers.
   *
   * @param key The key that the AVGeoPoint is stored in.
   * @param point The reference AVGeoPoint that is used.
   * @param maxDistance outer radius of the given ring in kilometers
   * @param minDistance inner radius of the given ring in kilometers
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereWithinKilometers(String key, LCGeoPoint point, double maxDistance,
                                          double minDistance) {
    conditions.whereWithinKilometers(key, point, maxDistance, minDistance);
    return this;
  }

  /**
   * Add a proximity based constraint for finding objects with key point values near the point given
   * and within the maximum distance given. Radius of earth used is 3958.8 miles.
   * @param key The key that the AVGeoPoint is stored in.
   * @param maxDistance outer radius of the given ring in miles.
   * @param point The reference AVGeoPoint that is used.
   * @return  Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereWithinMiles(String key, LCGeoPoint point, double maxDistance) {
    conditions.whereWithinMiles(key, point, maxDistance);
    return this;
  }

  /**
   * Add a proximity based constraint for finding objects with key point values near the point
   * given and within the given ring.
   *
   * Radius of earth used is 3958.8 miles.
   *
   * @param key The key that the AVGeoPoint is stored in.
   * @param point The reference AVGeoPoint that is used.
   * @param maxDistance outer radius of the given ring in miles.
   * @param minDistance inner radius of the given ring in miles.
   * @return  Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereWithinMiles(String key, LCGeoPoint point, double maxDistance,
                                     double minDistance) {
    conditions.whereWithinMiles(key, point, maxDistance, minDistance);
    return this;
  }


  /**
   * Add a proximity based constraint for finding objects with key point values near the point given
   * and within the maximum distance given.
   *
   * @param key The key that the AVGeoPoint is stored in.
   * @param point The reference AVGeoPoint that is used.
   * @param maxDistance Maximum distance (in radians) of results to return.
   * @return Returns the query, so you can chain this call.
   */
  public LCQuery<T> whereWithinRadians(String key, LCGeoPoint point, double maxDistance) {
    conditions.whereWithinRadians(key, point, maxDistance);
    return this;
  }

  /**
   * Add a proximity based constraint for finding objects with key point values near the point given
   * and within the maximum distance given.
   *
   * @param key The key that the AVGeoPoint is stored in.
   * @param point The reference AVGeoPoint that is used.
   * @param maxDistance outer radius of the given radians.
   * @param minDistance inner radius of the given radians.
   * @return  Returns the query, so you can chain this call.
   */

  public LCQuery<T> whereWithinRadians(String key, LCGeoPoint point, double maxDistance,
                                       double minDistance) {
    conditions.whereWithinRadians(key, point, maxDistance, minDistance);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value matches a value for a key
   * in the results of another AVQuery
   *
   * @param key The key whose value is being checked
   * @param keyInQuery The key in the objects from the sub query to look in
   * @param query The sub query to run
   * @return Returns the query so you can chain this call.
   */
  public LCQuery<T> whereMatchesKeyInQuery(String key, String keyInQuery, LCQuery<?> query) {
    Map<String, Object> inner = new HashMap<String, Object>();
    inner.put("className", query.getClassName());
    inner.put("where", query.conditions.compileWhereOperationMap());
    if (query.conditions.getSkip() > 0) inner.put("skip", query.conditions.getSkip());
    if (query.conditions.getLimit() > 0) inner.put("limit", query.conditions.getLimit());
    if (!StringUtil.isEmpty(query.getOrder())) inner.put("order", query.getOrder());

    Map<String, Object> queryMap = new HashMap<String, Object>();
    queryMap.put("query", inner);
    queryMap.put("key", keyInQuery);
    return addWhereItem(key, "$select", queryMap);
  }

  /**
   * Add a constraint to the query that requires a particular key's value match another AVQuery.
   * This only works on keys whose values are AVObjects or lists of AVObjects.
   *
   * @param key The key to check.
   * @param query The query that the value should match
   * @return Returns the query so you can chain this call.
   */
  public LCQuery<T> whereMatchesQuery(String key, LCQuery<?> query) {
    Map<String, Object> map =
            LCUtils.createMap("where", query.conditions.compileWhereOperationMap());
    map.put("className", query.className);
    if (query.conditions.getSkip() > 0) map.put("skip", query.conditions.getSkip());
    if (query.conditions.getLimit() > 0) map.put("limit", query.conditions.getLimit());
    if (!StringUtil.isEmpty(query.getOrder())) map.put("order", query.getOrder());
    addWhereItem(key, "$inQuery", map);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value does not match any value
   * for a key in the results of another AVQuery.
   *
   * @param key The key whose value is being checked and excluded
   * @param keyInQuery The key in the objects from the sub query to look in
   * @param query The sub query to run
   * @return Returns the query so you can chain this call.
   */
  public LCQuery<T> whereDoesNotMatchKeyInQuery(String key, String keyInQuery, LCQuery<?> query) {
    Map<String, Object> map = LCUtils.createMap("className", query.className);
    map.put("where", query.conditions.compileWhereOperationMap());

    Map<String, Object> queryMap = LCUtils.createMap("query", map);
    queryMap.put("key", keyInQuery);

    addWhereItem(key, "$dontSelect", queryMap);
    return this;
  }

  /**
   * Add a constraint to the query that requires a particular key's value does not match another
   * AVQuery. This only works on keys whose values are AVObjects or lists of AVObjects.
   *
   * @param key The key to check.
   * @param query The query that the value should not match
   * @return Returns the query so you can chain this call.
   */
  public LCQuery<T> whereDoesNotMatchQuery(String key, LCQuery<?> query) {
    Map<String, Object> map = LCUtils.createMap("className", query.className);
    map.put("where", query.conditions.compileWhereOperationMap());

    addWhereItem(key, "$notInQuery", map);
    return this;
  }

  LCQuery<T> setWhere(Map<String, List<QueryOperation>> value) {
    conditions.setWhere(value);
    return this;
  }

  /**
   * Constructs a query that is the or of the given queries.
   *
   * @param queries The list of AVQueries to 'or' together
   * @param <T> template type.
   * @return A AVQuery that is the 'or' of the passed in queries
   */
  public static <T extends LCObject> LCQuery<T> or(List<LCQuery<T>> queries) {
    if (null == queries || queries.isEmpty()) {
      throw new IllegalArgumentException("queries must be non-empty.");
    }
    String className = queries.get(0).getClassName();

    LCQuery<T> result = new LCQuery<T>(className);
    if (queries.size() > 1) {
      for (LCQuery<T> query : queries) {
        if (!className.equals(query.getClassName())) {
          throw new IllegalArgumentException("All queries must be for the same class");
        }
        result.addOrItems(new QueryOperation("$or", "$or", query.conditions
                .compileWhereOperationMap()));
      }
    } else {
      result.setWhere(queries.get(0).conditions.getWhere());
    }

    return result;
  }

  /**
   * Constructs a query that is the and of the given queries.
   *
   * @param queries The list of AVQueries to 'and' together
   * @param <T> template type.
   * @return A AVQuery that is the 'and' of the passed in queries
   */
  public static <T extends LCObject> LCQuery<T> and(List<LCQuery<T>> queries) {
    if (null == queries || queries.isEmpty()) {
      throw new IllegalArgumentException("queries must be non-empty.");
    }
    String className = queries.get(0).getClassName();

    LCQuery<T> result = new LCQuery<T>(className);
    if (queries.size() > 1) {
      for (LCQuery<T> query : queries) {
        if (!className.equals(query.getClassName())) {
          throw new IllegalArgumentException("All queries must be for the same class");
        }
        result.addAndItems(query);
      }
    } else {
      result.setWhere(queries.get(0).conditions.getWhere());
    }

    return result;
  }

  /**
   * Has cached result or not.
   * @return cache result existed or not.
   */
  public boolean hasCachedResult() {
    Map<String, String> query = assembleParameters();
    return PaasClient.getStorageClient().hasCachedResult(getClassName(), query, this.getMaxCacheAge());
  }

  /**
   * Execute query in blocking mode.
   * @return result of list.
   */
  public List<T> find() {
    return findInBackground().blockingLast();
  }

  /**
   * Execute query in async mode.
   * @return observable instance.
   */
  public Observable<List<T>> findInBackground() {
    return findInBackground(null);
  }

  /**
   * Execute query in async mode.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public Observable<List<T>> findInBackground(LCUser asAuthenticatedUser) {
    return findInBackground(asAuthenticatedUser, 0);
  }

  protected Observable<List<T>> findInBackground(LCUser asAuthenticatedUser, int explicitLimit) {
    Map<String, String> query = assembleParameters();
    if (explicitLimit > 0) {
      query.put("limit", Integer.toString(explicitLimit));
    }
    LOGGER.d("Query: " + query);
    return PaasClient.getStorageClient().queryObjects(asAuthenticatedUser, getClassName(), query, this.cachePolicy, this.maxCacheAge)
            .map(new Function<List<LCObject>, List<T>>() {
              public List<T> apply(List<LCObject> var1) throws Exception {
                LOGGER.d("invoke within AVQuery.findInBackground(). resultSize=" + var1.size());
                List<T> result = new ArrayList<T>(var1.size());
                for (LCObject obj: var1) {
                  T tmp = Transformer.transform(obj, getClassName());
                  result.add(tmp);
                }
                return result;
              }
            });
  }

  /**
   * Get Object with specified objectId in blocking mode.
   * @param objectId object id.
   * @return object instance.
   */
  public T get(String objectId) {
    return getInBackground(objectId).blockingFirst();
  }

  /**
   * Get Object with specified objectId in async mode.
   * @param objectId object id.
   * @return observable instance.
   */
  public Observable<T> getInBackground(String objectId) {
    return getInBackground(null, objectId);
  }

  /**
   * Get Object with specified objectId in async mode.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param objectId object id.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public Observable<T> getInBackground(LCUser asAuthenticatedUser, String objectId) {
    List<String> include = getInclude();
    String includeKeys = null;
    if (null != include && include.size() > 0) {
      includeKeys = StringUtil.join(",", include);
    }
    return PaasClient.getStorageClient().fetchObject(asAuthenticatedUser, getClassName(), objectId, includeKeys)
            .map(new Function<LCObject, T>() {
      public T apply(LCObject LCObject) throws Exception {
        if (null == LCObject || StringUtil.isEmpty(LCObject.getObjectId())) {
          throw new LCException(LCException.OBJECT_NOT_FOUND, "Object is not found.");
        }
        return Transformer.transform(LCObject, getClassName());
      }
    });
  }

  /**
   * Get first result in blocking mode.
   * @return first result.
   */
  public T getFirst() {
    return getFirst(null);
  }

  /**
   * Get first result in blocking mode.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @return first result.
   *
   * in general, this method should be invoked in lean engine.
   */
  public T getFirst(LCUser asAuthenticatedUser) {
    try {
      return getFirstInBackground(asAuthenticatedUser).blockingFirst();
    } catch (NoSuchElementException ex) {
      return null;
    }
  }

  /**
   * Get first result in async mode.
   * @return observable instance.
   */
  public Observable<T> getFirstInBackground() {
    return getFirstInBackground(null);
  }

  /**
   * Get first result in async mode.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public Observable<T> getFirstInBackground(LCUser asAuthenticatedUser) {
    return findInBackground(asAuthenticatedUser,1).flatMap(new Function<List<T>, ObservableSource<T>>() {
      @Override
      public ObservableSource<T> apply(List<T> list) throws Exception {
        LOGGER.d("flatMap: " + list);
        return Observable.fromIterable(list);
      }
    });
  }

  /**
   * Get result count in blocking mode.
   * @return result count.
   */
  public int count() {
    return count(null);
  }

  /**
   * Get result count in blocking mode.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @return result count.
   *
   * in general, this method should be invoked in lean engine.
   */
  public int count(LCUser asAuthenticatedUser) {
    return countInBackground(asAuthenticatedUser).blockingFirst();
  }

  /**
   * Get result count in async mode.
   * @return observable instance.
   */
  public Observable<Integer> countInBackground() {
    return countInBackground(null);
  }

  /**
   * Get result count in async mode.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public Observable<Integer> countInBackground(LCUser asAuthenticatedUser) {
    Map<String, String> query = assembleParameters();
    query.put("count", "1");
    query.put("limit", "0");
    return PaasClient.getStorageClient().queryCount(asAuthenticatedUser, getClassName(), query);
  }

  /**
   * Delete all query result in blocking mode.
   */
  public void deleteAll() {
    deleteAll(null);
  }

  /**
   * Delete all query result in blocking mode.
   * @param asAuthenticatedUser explicit user for request authentication.
   *
   * in general, this method should be invoked in lean engine.
   */
  public void deleteAll(LCUser asAuthenticatedUser) {
    this.deleteAllInBackground(asAuthenticatedUser).blockingSubscribe();
  }

  /**
   * Delete all query result in async mode.
   * @return observable instance.
   */
  public Observable<LCNull> deleteAllInBackground() {
    return deleteAllInBackground(null);
  }

  /**
   * Delete all query result in async mode.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public Observable<LCNull> deleteAllInBackground(LCUser asAuthenticatedUser) {
    return findInBackground(asAuthenticatedUser).flatMap(new Function<List<T>, ObservableSource<LCNull>>() {
      @Override
      public ObservableSource<LCNull> apply(List<T> list) {
        return LCObject.deleteAllInBackground(list);
      }
    });
  }

  /**
   * Assemble query parameters.
   * @return map of query parameters.
   */
  public Map<String, String> assembleParameters() {
    conditions.assembleParameters();
    return conditions.getParameters();
  }

  protected Map<String, Object> assembleJsonParam() {
    Map<String, Object> result = conditions.assembleJsonParam();
    result.put("className", getClassName());
    return result;
  }
}
