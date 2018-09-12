package cn.leancloud.core;

import cn.leancloud.codec.MD5;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.StringUtil;

public class RequestSignImplementation {
  private static boolean useMasterKey = false;
  private static String masterKey = null;

  public static String requestSign() {
    return requestSign(AVUtils.getCurrentTimestamp(), isUseMasterKey());
  }

  public static String requestSign(long ts, boolean useMasterKey) {
    String appKey = AVOSCloud.getApplicationKey();
    StringBuilder builder = new StringBuilder();

    StringBuilder result = new StringBuilder();

    result.append(MD5.computeMD5(builder.append(ts).append(useMasterKey ? masterKey : appKey).toString()).toLowerCase());
    result.append(',').append(ts);
    if (useMasterKey) {
      result.append(",master");
    }
    return result.toString();
  }

  public static void setMasterKey(String masKey) {
    if (!StringUtil.isEmpty(masKey)) {
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
