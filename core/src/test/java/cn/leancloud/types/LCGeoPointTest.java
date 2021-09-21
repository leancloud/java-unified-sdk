package cn.leancloud.types;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LCGeoPointTest extends TestCase {
    public LCGeoPointTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(LCGeoPointTest.class);
    }

    public void testDistanceInKilometers() {
        LCGeoPoint pt1 = new LCGeoPoint(23, 23);
        LCGeoPoint pt2 = new LCGeoPoint(24, 24);
        double distance = pt1.distanceInKilometersTo(pt2);
        System.out.println("distance(in Kilometers): " + distance);
        assertTrue(distance < 152.0);
        assertTrue(distance > 151.0);
    }

    public void testDistanceInMiles() {
        LCGeoPoint pt1 = new LCGeoPoint(23, 23);
        LCGeoPoint pt2 = new LCGeoPoint(24, 24);
        double distance = pt1.distanceInMilesTo(pt2);
        System.out.println("distance(in Miles): " + distance);
        assertTrue(distance < 100.0);
    }

    public void testDistanceInRadians() {
        LCGeoPoint pt1 = new LCGeoPoint(2, 2);
        LCGeoPoint pt2 = new LCGeoPoint(24, 24);
        double distance = pt1.distanceInRadiansTo(pt2);
        System.out.println("distance(in Kilometers): " + pt1.distanceInKilometersTo(pt2));
        System.out.println("distance(in Miles): " + pt1.distanceInMilesTo(pt2));
        System.out.println("distance(in Radians): " + distance);
        assertTrue(distance < 1.0);
    }
}
