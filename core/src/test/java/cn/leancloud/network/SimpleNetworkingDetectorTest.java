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
    System.out.println("connection#isConnected? " + statusUp);
  }

  public void testConnectionType() {
    SimpleNetworkingDetector detector = new SimpleNetworkingDetector();
    System.out.println("NetworkType: " + detector.getNetworkType());
  }
}
