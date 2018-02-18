package cn.leancloud.codec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
  public static String computeMD5(byte[] data) {
    if (null == data) {
      return null;
    }
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(data, 0, data.length);
      byte[] md5bytes = md.digest();

      return hexEncodeBytes(md5bytes);
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
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
