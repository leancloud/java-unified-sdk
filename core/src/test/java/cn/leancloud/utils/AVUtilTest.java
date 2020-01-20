package cn.leancloud.utils;

import junit.framework.TestCase;

public class AVUtilTest extends TestCase {
  public void testDoubleNormalize() {
    Double d = 166.63999938964844;
    double dd = AVUtils.normalize2Double(2, d);
    System.out.println(dd);
    assertTrue(d != dd);
  }
}
