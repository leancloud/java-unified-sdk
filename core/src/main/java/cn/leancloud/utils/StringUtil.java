package cn.leancloud.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class StringUtil {
  private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  public static boolean isEmpty(String str) {
    return null == str || str.trim().length() == 0;
  }

  public static String stringFromDate(Date date) {
    if (null == date) {
      return null;
    }

    SimpleDateFormat df = new SimpleDateFormat(dateFormat);
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    String isoDate = df.format(date);
    return isoDate;
  }

  static Random random = new Random(System.currentTimeMillis());

  public static String getRandomString(int length) {
    String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder randomString = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      randomString.append(letters.charAt(random.nextInt(letters.length())));
    }

    return randomString.toString();
  }
}
