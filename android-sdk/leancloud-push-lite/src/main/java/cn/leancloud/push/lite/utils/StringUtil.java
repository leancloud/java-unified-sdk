package cn.leancloud.push.lite.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class StringUtil {
  private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  public static boolean isEmpty(String str) {
    return null == str || str.trim().length() == 0;
  }

  public static boolean isDigitString(String s) {
    if (s == null) return false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!Character.isDigit(c)) {
        return false;
      }
    }
    return true;
  }

  public static Date dateFromString(String content) {
    if (isEmpty(content)) return null;
    if (isDigitString(content)) {
      return new Date(Long.parseLong(content));
    }
    Date date = null;
    SimpleDateFormat format = new SimpleDateFormat(dateFormat);
    format.setTimeZone(TimeZone.getTimeZone("UTC"));

    try {
      date = format.parse(content);
    } catch (Exception exception) {
    }
    return date;
  }
}
