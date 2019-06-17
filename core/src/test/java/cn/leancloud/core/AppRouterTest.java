package cn.leancloud.core;

import cn.leancloud.AVUser;
import cn.leancloud.Configure;
import cn.leancloud.service.RTMConnectionServerResponse;
import junit.framework.TestCase;

public class AppRouterTest extends TestCase {
  public AppRouterTest(String name) {
    super(name);
    Configure.initializeRuntime();
    AVUser currentUser = AVUser.getCurrentUser();
    System.out.println("currentUser = " + currentUser);
  }

  public void testFetchApiEndpoint() {
    AppRouter router = AppRouter.getInstance();
    String apiHost = router.getEndpoint(Configure.TEST_APP_ID, AVOSService.API, false).blockingSingle();
    assertEquals("https://ohqhxu3m.api.lncld.net", apiHost);
    String pushHost = router.getEndpoint(Configure.TEST_APP_ID, AVOSService.PUSH, true).blockingSingle();
    assertEquals("https://ohqhxu3m.push.lncld.net", pushHost);
  }

  public void testFetchRTMEndpoint() {
    AppRouter router = AppRouter.getInstance();
    String rtmRouterServer = router.getEndpoint(Configure.TEST_APP_ID, AVOSService.RTM, false).blockingSingle();
    RTMConnectionServerResponse response = router.fetchRTMConnectionServer(rtmRouterServer, Configure.TEST_APP_ID,
            null, 1, false).blockingSingle();
    assertNotNull(response.getServer());
    assertNotNull(response.getGroupId());
    assertNotNull(response.getGroupUrl());
    assertNotNull(response.getSecondary());
    assertTrue(response.getTtl() > System.currentTimeMillis() / 1000);
  }


}
