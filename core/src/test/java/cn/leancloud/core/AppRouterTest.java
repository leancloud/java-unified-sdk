package cn.leancloud.core;

import cn.leancloud.LCUser;
import cn.leancloud.Configure;
import cn.leancloud.service.RTMConnectionServerResponse;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

public class AppRouterTest extends TestCase {
  public AppRouterTest(String name) {
    super(name);
    Configure.initializeRuntime();
    LCUser currentUser = LCUser.getCurrentUser();
    System.out.println("currentUser = " + currentUser);
  }

  public void testAndroidRequestSign() {
    System.out.println();
    String sign = GeneralRequestSignature.requestSign("c7906aff685238709c47416140534f3e", 1585203244677l,
            "ax-sig-1");
    System.out.println(sign);
    System.out.println("a0002d2a403f35f1fdff02bec956567e,1585203244677,ax-sig-1");
  }

  public void testRequestSign() {
    System.out.println();
    String sign = GeneralRequestSignature.requestSign("4aaaa58afe61ac6b1ee49378fe8589e8", 1574842473675l,
            "ax-sig-1");
    System.out.println(sign);
    System.out.println("b892d60c5a3e68fb05c89d80b6c0a772,1574842473675,ax-sig-1");
  }

  public void testFetchApiEndpoint() {
    AppRouter router = AppRouter.getInstance();
    String apiHost = router.getEndpoint(Configure.TEST_APP_ID, LeanService.API).blockingSingle();
    assertEquals("https://ohqhxu3m.lc-cn-n1-shared.com", apiHost);
    String pushHost = router.getEndpoint(Configure.TEST_APP_ID, LeanService.PUSH).blockingSingle();
    System.out.println(pushHost);
  }

  public void testFetchRTMEndpoint() {
    AppRouter router = AppRouter.getInstance();
    String rtmRouterServer = router.getEndpoint(Configure.TEST_APP_ID, LeanService.RTM).blockingSingle();
    RTMConnectionServerResponse response = router.fetchRTMConnectionServer(rtmRouterServer, Configure.TEST_APP_ID,
            null, 1, false).blockingSingle();
    assertNotNull(response.getServer());
    assertNotNull(response.getGroupId());
    assertNotNull(response.getGroupUrl());
    assertNotNull(response.getSecondary());
    assertTrue(response.getTtl() > System.currentTimeMillis() / 1000);
  }

  public void testRegionCheck() {
    final Set<String> NorthAmericaSpecialApps = new HashSet<>();
    NorthAmericaSpecialApps.add("msjqtclsfmfeznwvm29dqvuwddt3cqmziszf0rjddxho8eis");
    NorthAmericaSpecialApps.add("iuuztdrr4mj683kbsmwoalt1roaypb5d25eu0f23lrfsthgn");
    NorthAmericaSpecialApps.add("glvame9g0qlj3a4o29j5xdzzrypxvvb30jt4vnvm66klph4r");
    NorthAmericaSpecialApps.add("nf3udjhnnsbe99qg04j7oslck4w1yp2geewcy1kp6wskbu5w");
    NorthAmericaSpecialApps.add("143mgzglqmg4d0simqtn1zswggcro2ykugj76th8l38u3cm5");
    NorthAmericaSpecialApps.add("18ry1wsn1p7808tagf2ka7sy1omna3nihe45cet0ne4xhg46");
    NorthAmericaSpecialApps.add("7az5r9i0v95acx932a518ygz7mvr26uc7e3xxaq9s389sd2o");
    NorthAmericaSpecialApps.add("kekxwm8uz1wtgxzvv5kitsgsammjcx4lcgm5b159qia5rqo5");
    NorthAmericaSpecialApps.add("q3er6vs0dkawy15skjeuktf7l4eam438wn5jkts2j7fpf2y3");
    NorthAmericaSpecialApps.add("tsvezhhlefbdj1jbkohynipehgtpk353sfonvbtlyxaraqxy");
    NorthAmericaSpecialApps.add("8FfQwpvihLHK4htqmtEvkNrv");
    NorthAmericaSpecialApps.add("AjQYwoIyObTeEkD16v1eCq55");
    NorthAmericaSpecialApps.add("Ol0Cw6zL1xP9IIqJpiSv9uYC");
    NorthAmericaSpecialApps.add("E0mVu1VMWrwBodUFWBpWzLNV");
    NorthAmericaSpecialApps.add("wnDg0lPt0wcYGJSiHRwHBhD4");
    NorthAmericaSpecialApps.add("W9BCIPx2biwKiKfUvVJtc8kF");
    NorthAmericaSpecialApps.add("J0Ev9alAhaS4IdnxBA95wKgn");
    NorthAmericaSpecialApps.add("nHptjiXlt3g8mcraXYRDpYFT");
    NorthAmericaSpecialApps.add("pFcwt2MaALYf70POa7bIqe0J");
    NorthAmericaSpecialApps.add("YHE5exCaW7UolMFJUtHvXTUY");
    Object[] appIds = NorthAmericaSpecialApps.toArray();
    for (int i = 0; i < appIds.length; i++) {
      if (LeanCloud.REGION.NorthAmerica != AppRouter.getAppRegion((String) appIds[i])) {
        fail();
      }
    }
  }

}
