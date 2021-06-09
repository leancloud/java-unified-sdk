package cn.leancloud.codec;

import cn.leancloud.cache.PersistenceUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MDFive {
  private static final int MAX_FILE_BUF_SIZE = 1024*1024*2;

  public static String computeMD5(String data) {
    if (null == data) {
      return null;
    }
    return computeMD5(data.getBytes());
  }
  public static String computeMD5(byte[] data) {
    if (null == data) {
      return "";
    }
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(data, 0, data.length);
      byte[] md5bytes = md.digest();

      return hexEncodeBytes(md5bytes);
    } catch (NoSuchAlgorithmException ex) {
      return "";
    }
  }

  public static String computeFileMD5(File localFile) {
    if (null == localFile || !localFile.exists() || !localFile.isFile()) {
      return null;
    }
    try {
      String result = null;
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      InputStream is = PersistenceUtil.getInputStreamFromFile(localFile);
      if (null != is) {
        byte buf[] = new byte[MAX_FILE_BUF_SIZE];
        int len  = 0;
        while((len = is.read(buf)) != -1) {
          md5.update(buf, 0, len);
        }
        byte[] md5bytes = md5.digest();
        result = MDFive.hexEncodeBytes(md5bytes);
        is.close();
      }
      return result;
    } catch (IOException ex) {
      return null;
    } catch (NoSuchAlgorithmException ex) {
      return null;
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

  public static MDFive getInstance() {
    return new MDFive();
  }

  private MessageDigest mdInstance = null;
  private MDFive() {
    try {
      mdInstance = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException ex) {
    }
  }

  public boolean prepare() {
    if (null != mdInstance) {
      mdInstance.reset();
      return true;
    }
    return false;
  }

  public void update(byte[] input, int offset, int len) {
    if (null != mdInstance) {
      mdInstance.update(input, offset, len);
    }
  }

  public byte[] digest() {
    if (null != mdInstance) {
      return mdInstance.digest();
    }
    return null;
  }
}
