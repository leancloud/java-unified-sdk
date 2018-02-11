package cn.leancloud.utils;

public class StringUtil {
  public static boolean isEmpty(String str) {
    return null == str || str.trim().length() == 0;
  }
}
