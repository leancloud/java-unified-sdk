package cn.leancloud.utils;

import cn.leancloud.core.AVOSCloud;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AVUtils {
  public static final double EARTH_MEAN_RADIUS_KM = 6378.140;

  public static double distance(double lat1, double lat2, double lon1,
                                double lon2, double el1, double el2) {
    final double R = EARTH_MEAN_RADIUS_KM; // Radius of the earth

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

  public static String jsonStringFromMapWithNull(Object map) {
    if (AVOSCloud.isDebugEnable()) {
      return JSON.toJSONString(map, SerializerFeature.WriteMapNullValue,
              SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero,
              SerializerFeature.PrettyFormat);
    } else {
      return JSON.toJSONString(map, SerializerFeature.WriteMapNullValue,
              SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero);
    }
  }

  public static String jsonStringFromObjectWithNull(Object map) {
    if (AVOSCloud.isDebugEnable()) {
      return JSON.toJSONString(map, SerializerFeature.WriteMapNullValue,
              SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero,
              SerializerFeature.PrettyFormat);
    } else {
      return JSON.toJSONString(map, SerializerFeature.WriteMapNullValue,
              SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero);
    }
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
}
