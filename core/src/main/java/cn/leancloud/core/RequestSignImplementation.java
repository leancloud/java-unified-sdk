package cn.leancloud.core;

import cn.leancloud.codec.MD5;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.StringUtil;

public class RequestSignImplementation {
  static boolean useMasterKey = false;
  private static String masterKey = null;

  public static String requestSign() {
    return requestSign(AVUtils.getCurrentTimestamp(), isUseMasterKey());
  }

  public static String requestSign(long ts, boolean useMasterKey) {
    StringBuilder builder = new StringBuilder();
    StringBuilder result = new StringBuilder();
    String appKey = AVOSCloud.getApplicationKey();

    result.append(MD5.computeMD5(builder.append(ts).append(useMasterKey ? masterKey : appKey).toString()).toLowerCase());
    result.append(',').append(ts);
    return !useMasterKey ? result.toString() : result.append(",master").toString();
  }

  public static void setMasterKey(String masKey) {
    if (!StringUtil.isEmpty(masterKey)) {
      masterKey = masKey;
      useMasterKey = true;
    } else {
      useMasterKey = false;
      masterKey = null;
    }
  }

  protected static boolean isUseMasterKey() {
    return useMasterKey;
  }
}
