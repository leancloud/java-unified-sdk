package cn.leancloud.search;

import cn.leancloud.types.AVGeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AVSearchSortBuilder {
  private final List<Object> sortFields = new ArrayList<Object>();

  public static AVSearchSortBuilder newBuilder() {
    return new AVSearchSortBuilder();
  }

  /**
   * 按照key升序排序
   *
   * @param key     排序的key
   * @param mode    数组或者多值字段的排序模式，min表示取最小值，max取最大值，sum取综合，avg取平均值，默认值是avg。
   * @param missing 当搜索匹配的文档没有排序的key的时候，设置本选项决定文档放在开头还是末尾，取值是"last"或者"first"，
   *                默认是"last"表示在末尾。
   * @return
   */
  public AVSearchSortBuilder orderByAscending(String key, String mode, String missing) {
    return addField(key, "asc", mode, missing);
  }

  /**
   * @param key
   * @param mode
   * @return
   * @see #orderByAscending(String, String, String)
   */
  public AVSearchSortBuilder orderByAscending(String key, String mode) {
    return orderByAscending(key, mode, "last");
  }

  /**
   * @param key
   * @return
   * @see #orderByAscending(String, String, String)
   */
  public AVSearchSortBuilder orderByAscending(String key) {
    return orderByAscending(key, "avg");
  }

  /**
   * 按照key降序排序
   *
   * @param key     排序的key
   * @param mode    数组或者多值字段的排序模式，min表示取最小值，max取最大值，sum取综合，avg取平均值，默认值是avg。
   * @param missing 当搜索匹配的文档没有排序的key的时候，设置本选项决定文档放在开头还是末尾，取值是"last"或者"first"，
   *                默认是"last"表示在末尾。
   * @return
   */
  public AVSearchSortBuilder orderByDescending(String key, String mode, String missing) {
    return addField(key, "desc", mode, missing);
  }

  /**
   * @param key
   * @param mode
   * @return
   * @see #orderByDescending(String, String, String)
   */
  public AVSearchSortBuilder orderByDescending(String key, String mode) {
    return orderByDescending(key, mode, "last");
  }

  /**
   * @param key
   * @return
   * @see #orderByDescending(String, String, String)
   */
  public AVSearchSortBuilder orderByDescending(String key) {
    return orderByDescending(key, "avg");
  }

  private AVSearchSortBuilder addField(String key, String order, String mode, String missing) {
    Map<String, Map<String, String>> field = new HashMap<String, Map<String, String>>();
    Map<String, String> map = new HashMap<String, String>();
    map.put("order", order);
    map.put("mode", mode);
    map.put("missing", "_" + missing);
    field.put(key, map);
    sortFields.add(field);
    return this;
  }

  public AVSearchSortBuilder whereNear(String key, AVGeoPoint point) {
    return this.whereNear(key, point, "asc");
  }

  public AVSearchSortBuilder whereNear(String key, AVGeoPoint point, String order) {
    return this.whereNear(key, point, order, "avg", "km");
  }

  /**
   * 按照地理位置信息远近排序,key对应的字段类型必须是GeoPoint。
   *
   * @param key   排序的字段key
   * @param point GeoPoint经纬度对象
   * @param order 排序顺序，升序"asc"，降序"desc"，默认升序，也就是从近到远。
   * @param mode  数组或者多值字段的排序模式，min表示取最小值，max取最大值，avg取平均值，默认值是avg。
   * @param unit  距离单位，"m"表示米，"cm"表示厘米，"mm"表示毫米，"km"表示公里，"mi"表示英里，"in"表示英寸，"yd"表示英亩，默认"km"。
   * @return
   */
  public AVSearchSortBuilder whereNear(String key, AVGeoPoint point, String order, String mode,
                                       String unit) {
    Map<String, Map<String, Object>> field = new HashMap<String, Map<String, Object>>();
    Map<String, Object> map = new HashMap<String, Object>();
    Map<String, Double> geoMap = new HashMap<String, Double>();
    geoMap.put("lat", point.getLatitude());
    geoMap.put("lon", point.getLongitude());
    map.put(key, geoMap);
    map.put("unit", unit);
    map.put("mode", mode);
    map.put("order", order);
    field.put("_geo_distance", map);
    sortFields.add(field);
    return this;
  }

  public List<Object> getSortFields() {
    return sortFields;
  }

}
