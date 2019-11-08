package cn.leancloud.core;

import cn.leancloud.codec.MD5;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.StringUtil;

public class GeneralRequestSignature implements RequestSignature {
  private static boolean useMasterKey = false;
  private static String masterKey = null;

  public String generateSign() {
    return requestSign(AVUtils.getCurrentTimestamp(), isUseMasterKey());
  }

  public static String requestSign(String key, long ts, String suffix) {
    StringBuilder builder = new StringBuilder();
    StringBuilder result = new StringBuilder();
    result.append(MD5.computeMD5(builder.append(ts).append(key).toString()).toLowerCase());
    result.append(',').append(ts);
    if (null != suffix) {
      result.append(',').append(suffix);
    }
    return result.toString();
  }

  public static String requestSign(long ts, boolean useMasterKey) {
    String appKey = AVOSCloud.getApplicationKey();
    String usedKey = useMasterKey ? masterKey : appKey;
    return requestSign(usedKey, ts, useMasterKey?"master":null);
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
