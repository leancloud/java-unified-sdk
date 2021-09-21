package cn.leancloud.types;

import cn.leancloud.utils.LCUtils;

public class LCGeoPoint {
  static double ONE_KM_TO_MILES = 1.609344;
  private double latitude;
  private double longitude;

  public LCGeoPoint() {
    latitude = 0.0;
    longitude = 0.0;
  }

  public LCGeoPoint(double latitude, double longitude) {
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

  /**
   * Get distance bw this point and another point in kilometers.
   * @param point GeoPoint describing the other point being measured against.
   * @return distance result.
   */
  public double distanceInKilometersTo(LCGeoPoint point) {
    if (null == point) {
      return 0.0f;
    }
    return LCUtils.distance(latitude, point.latitude, longitude, point.longitude, 0, 0)/1000;
  }

  /**
   * Get distance between this point and another geopoint in miles.
   *
   * @param point GeoPoint describing the other point being measured against.
   * @return distance
   */
  public double distanceInMilesTo(LCGeoPoint point) {
    return this.distanceInKilometersTo(point) / ONE_KM_TO_MILES;
  }

  /**
   * Get distance in radians between this point and another GeoPoint. This is the smallest angular
   * distance between the two points.
   *
   * @param point GeoPoint describing the other point being measured against.
   * @return distance
   */
  public double distanceInRadiansTo(LCGeoPoint point) {
    return this.distanceInKilometersTo(point) / LCUtils.earthMeanRadiusInKM;
  }

}
