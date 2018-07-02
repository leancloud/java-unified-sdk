package cn.leancloud.core;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.service.RTMConnectionServerResponse;
import junit.framework.TestCase;

public class AppRouterTest extends TestCase {
  public AppRouterTest(String name) {
    super(name);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public void testFetchApiEndpoint() {
    AppRouter router = AppRouter.getInstance();
    String apiHost = router.getEndpoint(Configure.TEST_APP_ID, AVOSServices.API, false).blockingSingle();
    assertEquals("https://ohqhxu3m.api.lncld.net", apiHost);
    String pushHost = router.getEndpoint(Configure.TEST_APP_ID, AVOSServices.PUSH, true).blockingSingle();
    assertEquals("https://ohqhxu3m.push.lncld.net", apiHost);
  }

  public void testFetchRTMEndpoint() {
    AppRouter router = AppRouter.getInstance();
    String rtmRouterServer = router.getEndpoint(Configure.TEST_APP_ID, AVOSServices.RTM, false).blockingSingle();
    RTMConnectionServerResponse response = router.fetchRTMConnectionServer(rtmRouterServer, Configure.TEST_APP_ID,
            null, 1, false).blockingSingle();
    assertNotNull(response.getServer());
    assertNotNull(response.getGroupId());
    assertNotNull(response.getGroupUrl());
    assertNotNull(response.getSecondary());
    assertTrue(response.getTtl() > System.currentTimeMillis() / 1000);
  }
}
