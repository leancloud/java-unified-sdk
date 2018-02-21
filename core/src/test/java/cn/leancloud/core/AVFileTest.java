package cn.leancloud.core;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AVFileTest extends TestCase {
  public AVFileTest(String name) {
    super(name);
  }
  public static Test suite() {
    return new TestSuite(AVFileTest.class);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }
}
