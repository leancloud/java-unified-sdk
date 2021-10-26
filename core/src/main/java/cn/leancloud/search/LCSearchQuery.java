package cn.leancloud.search;

import cn.leancloud.*;
import cn.leancloud.LCUser;
import cn.leancloud.LCObject;
import cn.leancloud.core.PaasClient;
import cn.leancloud.utils.LCUtils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import java.util.*;

public class LCSearchQuery<T extends LCObject> {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCSearchQuery.class);

  public static final String AVSEARCH_HIGHTLIGHT = "highlight_avoscloud_";
  public static final String AVSEARCH_APP_URL = "app_url_avoscloud_";
  public static final String AVSEARCH_DEEP_LINK = "deep_link_avoscloud_";
  public static final String DATA_EXTRA_SEARCH_KEY = "com.avos.avoscloud.search.key";

  private String sid;
  private int limit = 100;
  private int skip = 0;
  private String hightlights = null;
  private static final String URL = "search/select";
  private List<String> fields;
  private String queryString;
  private String titleAttribute;
  private String className;
  private int hits;
  private String order;
  private LCSearchSortBuilder sortBuilder;
  private List<String> include;
  Class<T> clazz;

  /**
   * 获取当前的AVSearchSortBuilder对象
   *
   * @return sort builder.
   */
  public LCSearchSortBuilder getSortBuilder() {
    return sortBuilder;
  }

  /**
   * 设置查询的AVSearchSortBuilder，使用更丰富的排序选项。
   *
   * @param sortBuilder sort builder.
   */
  public void setSortBuilder(LCSearchSortBuilder sortBuilder) {
    this.sortBuilder = sortBuilder;
  }

  public LCSearchQuery() {
    this(null);
  }

  public LCSearchQuery(String queryString) {
    this(queryString, null);
  }

  public LCSearchQuery(String queryString, Class<T> clazz) {
    this.queryString = queryString;
    this.clazz = clazz;
    this.include = new LinkedList<String>();
    if (clazz == null) {
      this.className = Transformer.getSubClassName(LCObject.class);
    } else {
      this.className = Transformer.getSubClassName(clazz);
    }
  }

  /**
   * 获取查询的className，默认为null，即包括所有启用了应用内搜索的class
   *
   * @return class name.
   */
  public String getClassName() {
    return className;
  }


  /**
   * 设置查询字段列表，以逗号隔开的字符串，例如"a,b,c"，表示按照a,b,c三个字段的顺序排序，如果字段前面有负号，表示倒序，例如"a,-b"
   *
   * @param order order string.
   * @return Returns the query, so you can chain this call.
   */
  public LCSearchQuery order(String order) {
    this.order = order;
    return this;
  }

  /**
   * 根据提供的key进行升序排序
   *
   * @param key 需要排序的key
   * @return  Returns the query, so you can chain this call.
   */
  public LCSearchQuery orderByAscending(String key) {
    if (StringUtil.isEmpty(order)) {
      order = String.format("%s", key);
    } else {
      order = String.format("%s,%s", order, key);
    }
    return this;
  }

  /**
   * 根据提供的key进行降序排序
   *
   * @param key The key to order by.
   * @return Returns the query, so you can chain this call.
   */
  public LCSearchQuery orderByDescending(String key) {
    if (StringUtil.isEmpty(order)) {
      order = String.format("-%s", key);
    } else {
      order = String.format("%s,-%s", order, key);
    }
    return this;
  }

  /**
   * Also sorts the results in ascending order by the given key. The previous sort keys have
   * precedence over this key.
   *
   * @param key The key to order by
   * @return Returns the query so you can chain this call.
   */
  public LCSearchQuery addAscendingOrder(String key) {
    if (StringUtil.isEmpty(order)) {
      return this.orderByAscending(key);
    }

    order = String.format("%s,%s", order, key);
    return this;
  }

  /**
   * Also sorts the results in descending order by the given key. The previous sort keys have
   * precedence over this key.
   *
   * @param key The key to order by
   * @return Returns the query so you can chain this call.
   */
  public LCSearchQuery addDescendingOrder(String key) {
    if (StringUtil.isEmpty(order)) {
      return orderByDescending(key);
    }

    order = String.format("%s,-%s", order, key);
    return this;
  }


  /**
   * 设置查询的className，否则将包括所有启用了应用内搜索的class
   *
   * @param className class name
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * 设置搜索的结果单页大小限制,默认值为100，最大为1000
   *
   * @param limit pagination limit.
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * 获得搜索结果的单页大小限制
   *
   * @return pagination limit.
   */
  public int getLimit() {
    return this.limit;
  }

  /**
   * 返回当前返回集合的其实位置
   *
   * @return current skip.
   */
  public int getSkip() {
    return skip;
  }

  /**
   * 设置返回集合的起始位置，一般用于分页
   *
   * @param skip skip value.
   */
  public void setSkip(int skip) {
    this.skip = skip;
  }

  /**
   * 设置返回的高亮语法，默认为"*"
   * 语法规则可以参考　　http://www.elasticsearch.org/guide/en/elasticsearch/reference/current
   * /search-request-highlighting.html#highlighting-settings
   *
   * @param hightlights highlight setting.
   */
  public void setHightLights(String hightlights) {
    this.hightlights = hightlights;
  }

  /**
   * 获取当前设定的语法高亮
   *
   * @return highlight setting.
   */

  public String getHightLights() {
    return this.hightlights;
  }

  public void setFields(List<String> fields) {
    this.fields = fields;
  }

  public List<String> getFields() {
    return this.fields;
  }

  /**
   * 设置查询id，通常您都不需要调用这个方法来设置，只要不停调用find就可以实现分页。
   * 不过如果需要将查询分页传递到其他Activity，则可能需要通过传递sid来实现。
   *
   * @param sid search session id.
   */
  public void setSid(String sid) {
    this.sid = sid;
  }

  /**
   * 获取本次查询的id，注意，它不是返回结果中对象的objectId，而是表示本次AVSearchQuery查询的id
   *
   * @return search session id.
   */
  public String getSid() {
    return this.sid;
  }

  /**
   * 此选项为AVOSCloud SearchActivity使用 指定Title所对应的Field
   *
   * @param titleAttribute title setting.
   */
  public void setTitleAttribute(String titleAttribute) {
    this.titleAttribute = titleAttribute;
  }

  /**
   * 获取当前指定的title 对应的Field
   *
   * @return title setting.
   */
  public String getTitleAttribute() {
    return titleAttribute;
  }

  /**
   * 设置搜索的查询语句。
   * 详细语法可以参考http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl
   * -query-string-query.html#query-string-syntax
   *
   * @param query query string.
   */
  public void setQueryString(String query) {
    if (!((this.queryString == null && query == null) || (this.queryString != null && this.queryString
            .equals(query)))) {
      this.sid = null;
    }
    this.queryString = query;

  }

  public String getQueryString() {
    return queryString;
  }

  public int getHits() {
    return this.hits;
  }

  public void include(String key) {
    this.include.add(key);
  }

  private Map<String, String> getParameters(String query) {
    Map<String, String> params = new HashMap<>();
    params.put("q", query);
    if (!StringUtil.isEmpty(sid)) {
      params.put("sid", sid);
    }
    if (!StringUtil.isEmpty(hightlights)) {
      params.put("highlights", hightlights);
    } else {
      params.put("highlights", "*");
    }
    if (fields != null && fields.size() > 0) {
      params.put("fields", StringUtil.join(",", fields));
    }
    if (limit > 0) {
      params.put("limit", String.valueOf(limit));
    }
    if (skip > 0) {
      params.put("skip", String.valueOf(skip));
    }
    if (!StringUtil.isEmpty(order)) {
      params.put("order", order);
    }
    if (sortBuilder != null) {
      params.put("sort", LCUtils.jsonStringFromObjectWithNull(sortBuilder.getSortFields()));
    }
    if (!include.isEmpty()) {
      String value = StringUtil.join(",", include);
      params.put("include", value);
    }
    if (!StringUtil.isEmpty(className)) {
      params.put("clazz", className);
    }
    return params;
  }

  /**
   * execute query in background.
   * @return observable instance.
   */
  public Observable<List<T>> findInBackground() {
    return this.findInBackground(null);
  }

  /**
   * execute query in background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public Observable<List<T>> findInBackground(LCUser asAuthenticatedUser) {
    return getSearchResult(asAuthenticatedUser, getParameters(queryString));
  }

  protected Observable<List<T>> getSearchResult(LCUser asAuthenticatedUser, Map<String, String> params) {
    return PaasClient.getStorageClient().search(asAuthenticatedUser, params).map(new Function<LCSearchResponse, List<T>>() {
      @Override
      public List<T> apply(LCSearchResponse result) throws Exception {
        return processContent(result);
      }
    });
  }

  private List<T> processContent(LCSearchResponse resp) throws Exception {
    if (null == resp) {
      return Collections.emptyList();
    }
    this.sid = resp.sid;
    this.hits = resp.hits;
    List<T> result = new LinkedList<T>();
    for (Map item : resp.results) {
      if (item != null && !item.isEmpty()) {
        LCObject object;
        if (clazz == null) {
          object = new LCObject(StringUtil.isEmpty(className) ? (String) item.get("className") : className);
        } else {
          object = clazz.newInstance();
        }
        if(item.containsKey("_highlight")) {
          item.put(AVSEARCH_HIGHTLIGHT, item.get("_highlight"));
          item.remove("_highlight");
        }
        if(item.containsKey("_app_url")) {
          item.put(AVSEARCH_APP_URL, item.get("_app_url"));
          item.remove("_app_url");
        }
        if(item.containsKey("_deeplink")) {
          item.put(AVSEARCH_DEEP_LINK, item.get("_deeplink"));
          item.remove("_deeplink");
        }
        object.resetServerData(item);
        result.add((T) object);
      }
    }
    return result;
  }
}
