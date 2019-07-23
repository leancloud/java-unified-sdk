package cn.leancloud;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommonTest extends TestCase {
  public CommonTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(CommonTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.out.println("exit setUp()");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    System.out.println("exit tearDown()");
  }

  public void testPutAllForMap() throws Exception {
    Map<String, Object> hashMap = new HashMap<>();
    try {
      hashMap.put("testKey", null);
      System.out.println("We could put null value to hash Map.");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    ConcurrentMap<String, Object> concurrentHashMap = new ConcurrentHashMap<>();
    try {
      concurrentHashMap.put("testKey", null);
    } catch (Exception ex) {
      System.out.println("We could not put null value to concurrent Map.");
      ex.printStackTrace();
    }

    Map<String, Object> synchronizedMap = Collections.synchronizedMap(new HashMap<String, Object>());
    synchronizedMap.put("testKey", null);
  }
}
