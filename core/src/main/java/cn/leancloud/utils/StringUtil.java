package cn.leancloud.utils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
  public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

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

  public static boolean equals(String left, String right) {
    if (null == left || null == right) {
      return false;
    }
    return left.equals(right);
  }

  public static boolean equals(List<String> left, List<String> right) {
    if (null == left || null == right) {
      return false;
    }
    if (left.size() != right.size()) {
      return false;
    }
    return left.containsAll(right);
  }

  public static boolean equalsIgnoreCase(String left, String right) {
    if (null == left || null == right) {
      return false;
    }
    return left.equalsIgnoreCase(right);
  }

  public static String stringFromDate(Date date) {
    if (null == date) {
      return null;
    }

    SimpleDateFormat df = new SimpleDateFormat(dateFormat);
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    return df.format(date);
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

  public static String stringFromBytes(byte[] data) {
    try {
      return new String(data, "UTF-8");
    } catch (Exception e) {
      // e.printStackTrace();
    }
    return null;
  }

  private static String convertTimestamp(String kvPhrase) {
    String dateFormBegin = "new Date(";
    int index = kvPhrase.indexOf(dateFormBegin);
    if (index < 0) {
      return kvPhrase;
    }
    String key = kvPhrase.substring(0, index);
    String value = kvPhrase.substring(index).substring(dateFormBegin.length());
    String separator = value.substring(value.length() - 1);
    value = value.substring(0, value.length() - 2);
    Date realDate = new Date(Long.parseLong(value));
    return key + "\"" + StringUtil.stringFromDate(realDate) + "\"" + separator;
  }

  public static String replaceFastjsonDateForm(String text) {
    if (StringUtil.isEmpty(text)) {
      return text;
    }
    String fastjsonDateSpecialForm = "\"[a-zA-Z0-9]+\":new Date\\(\\d+\\)[,})\\]]";
    Pattern fastjsonDateSpecialPattern = Pattern.compile(fastjsonDateSpecialForm);
    Matcher m = fastjsonDateSpecialPattern.matcher(text);
    Map<String, String> replaceKVs = new HashMap<String, String>();
    while (m.find()) {
      String subString = m.group();
      String replacedString = convertTimestamp(subString);
      replaceKVs.put(subString, replacedString);
    }

    for (String k : replaceKVs.keySet()) {
      String replacedString = replaceKVs.get(k);
      text = text.replace(k, replacedString);
    }

    return text;
  }

  public static String join(CharSequence delimiter,
                            Iterable<? extends CharSequence> elements) {
    if (null == delimiter || null == elements) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    boolean isFirstElem = true;
    for (CharSequence cs: elements) {
      if (!isFirstElem) {
        sb.append(delimiter);
      } else {
        isFirstElem = false;
      }
      sb.append(cs);
    }
    return sb.toString();
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
