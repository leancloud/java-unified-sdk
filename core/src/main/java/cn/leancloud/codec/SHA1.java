package cn.leancloud.codec;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {
  private static String convertToHex(byte[] data) {
    StringBuilder buf = new StringBuilder();
    for (byte b : data) {
      int halfbyte = (b >>> 4) & 0x0F;
      int two_halfs = 0;
      do {
        buf.append((0 <= halfbyte) && (halfbyte <= 9)
                ? (char) ('0' + halfbyte)
                : (char) ('a' + (halfbyte - 10)));
        halfbyte = b & 0x0F;
      } while (two_halfs++ < 1);
    }
    return buf.toString();
  }

  public static String compute(byte[] data) throws NoSuchAlgorithmException,
          UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    md.update(data, 0, data.length);
    byte[] sha1hash = md.digest();
    return convertToHex(sha1hash);
  }
}
