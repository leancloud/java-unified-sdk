package cn.leancloud.utils;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public class AVUtils {
  public static double EARTH_MEAN_RADIUS_KM = 6378.140;

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
}
