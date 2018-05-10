package cn.leancloud.network;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SimpleNetworkingDetectorTest extends TestCase {
  public SimpleNetworkingDetectorTest(String name) {
    super(name);
  }
  public static Test suite()
  {
    return new TestSuite( SimpleNetworkingDetectorTest.class );
  }

  public void testConnectionStatus() {
    SimpleNetworkingDetector detector = new SimpleNetworkingDetector();
    boolean statusUp = detector.isConnected();
    assertTrue(statusUp);
  }

  public void testConnectionType() {
    SimpleNetworkingDetector detector = new SimpleNetworkingDetector();
    assertEquals(NetworkingDetector.NetworkType.WIFI, detector.getNetworkType());
  }
}
