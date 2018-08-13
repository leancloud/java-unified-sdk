package cn.leancloud;

import junit.framework.TestCase;

public class AVPushTest extends TestCase {
  public AVPushTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public void testSimplePush() throws Exception {
    AVPush push = new AVPush();
    push.setMessage("test from unittest");
    push.send();
  }
}
