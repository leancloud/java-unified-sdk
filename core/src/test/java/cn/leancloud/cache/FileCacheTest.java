package cn.leancloud.cache;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FileCacheTest extends TestCase {
  public FileCacheTest(String caseName) {
    super(caseName);
  }

  public static Test suite() {
    return new TestSuite(FileCacheTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testDummy() {
    ;
  }
}
