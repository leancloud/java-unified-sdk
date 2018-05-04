package cn.leancloud.types;

import cn.leancloud.utils.AVUtils;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType
public class AVGeoPoint {
  static double ONE_KM_TO_MILES = 1.609344;
  private double latitude;
  private double longitude;

  public AVGeoPoint() {
    latitude = 0.0;
    longitude = 0.0;
  }

  public AVGeoPoint(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double distanceInKilometersTo(AVGeoPoint point) {
    if (null == point) {
      return 0.0f;
    }
    return AVUtils.distance(latitude, point.latitude, longitude, point.longitude, 0, 0);
  }

  /**
   * Get distance between this point and another geopoint in kilometers.
   *
   * @param point GeoPoint describing the other point being measured against.
   */
  public double distanceInMilesTo(AVGeoPoint point) {
    return this.distanceInKilometersTo(point) / ONE_KM_TO_MILES;
  }

  /**
   * Get distance in radians between this point and another GeoPoint. This is the smallest angular
   * distance between the two points.
   *
   * @param point GeoPoint describing the other point being measured against.
   */
  public double distanceInRadiansTo(AVGeoPoint point) {
    return this.distanceInKilometersTo(point) / AVUtils.earthMeanRadiusInKM;
  }

}
