package cn.leancloud.utils;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.json.JSON;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class AVUtils {
  public static final double earthMeanRadiusInKM = 6378.140;

  public static double distance(double lat1, double lat2, double lon1,
                                double lon2, double el1, double el2) {
    final double R = earthMeanRadiusInKM; // Radius of the earth

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters

    double height = el1 - el2;

    distance = Math.pow(distance, 2) + Math.pow(height, 2);

    return Math.sqrt(distance);
  }

  public static String getJSONString(Map<String, Object> parameters) {
    return JSON.toJSONString(parameters);
  }

  public static Map<String, Object> createMap(String cmp, Object value) {
    Map<String, Object> dict = new HashMap<String, Object>();
    dict.put(cmp, value);
    return dict;
  }

  public static Map<String, Object> createStringObjectMap(String key, Object value) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(key, value);
    return map;
  }

  public static String jsonStringFromMapWithNull(Object map) {
    return JSON.toJSONString(map);
  }

  public static String jsonStringFromObjectWithNull(Object map) {
    return JSON.toJSONString(map);
  }

  public static long getCurrentTimestamp() {
    return System.currentTimeMillis();
  }

  public static boolean equals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
  }

  public static int hash(Object... values) {
    return Arrays.hashCode(values);
  }

  public static void ensureElementsNotNull(List<String> e, String errorLog) {
    for (String i : e) {
      if (i == null) {
        throw new NullPointerException(errorLog);
      }
    }
  }

  public static double normalize2Double(int n, Double value) {
    BigDecimal b = new BigDecimal(value);
    return normalize2Double(n, b);
  }

  public static double normalize2Double(int n, BigDecimal bigDecimal) {
    return bigDecimal.setScale(n, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  public static void mergeConcurrentMap(ConcurrentMap<String, Object> left, Map<String, Object> right) {
    if (null == left || null == right) {
      return;
    }
    for (Map.Entry<String, Object> entry : right.entrySet()) {
      if (null == entry.getKey() || null == entry.getValue()) {
        continue;
      }
      left.put(entry.getKey(), entry.getValue());
    }
  }
}
