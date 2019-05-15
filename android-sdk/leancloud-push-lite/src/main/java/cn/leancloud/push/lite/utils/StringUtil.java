package cn.leancloud.push.lite.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

  public static String md5(String string) {
    byte[] hash = null;
    try {
      hash = string.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Huh,UTF-8 should be supported?", e);
    }
    return computeMD5(hash);
  }

  public static String computeMD5(String data) {
    if (null == data) {
      return null;
    }
    return computeMD5(data.getBytes());
  }

  public static String computeMD5(byte[] input) {
    try {
      if (null == input) {
        return null;
      }
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(input, 0, input.length);
      byte[] md5bytes = md.digest();

      return hexEncodeBytes(md5bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static String hexEncodeBytes(byte[] md5bytes) {
    if (null == md5bytes) {
      return "";
    }
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < md5bytes.length; i++) {
      String hex = Integer.toHexString(0xff & md5bytes[i]);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
