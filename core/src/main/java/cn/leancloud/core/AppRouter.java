package cn.leancloud.core;

import cn.leancloud.core.AVOSCloud;

public class AppRouter {
  public static AVOSCloud.REGION getAppRegion(String applicationId) {
    return AVOSCloud.REGION.NorthChina;
  }
}
