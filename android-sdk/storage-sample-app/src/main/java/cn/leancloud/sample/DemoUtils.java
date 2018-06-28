package cn.leancloud.sample;

import android.content.Context;
import android.text.Html;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * Created by fengjunwen on 2018/3/22.
 */

public class DemoUtils {
  public static void loadCodeAtWebView(Context context, String code, WebView webView) {
    webView.getSettings().setJavaScriptEnabled(true);
    InputStream inputStream = null;
    String template = null;
    try {
      inputStream = context.getAssets().open("index.html");
      template = readTextFile(inputStream);
      template = template.replace("__CODE__", Html.escapeHtml(code));
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeQuietly(inputStream);
    }
    String baseUrl = "file:///android_asset/";
    webView.loadDataWithBaseURL(baseUrl, template, "text/html", "UTF-8", "");
  }

  public static void closeQuietly(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  static public String readTextFile(InputStream inputStream) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte buf[] = new byte[8 * 1024];
    int len;
    try {
      while ((len = inputStream.read(buf)) != -1) {
        outputStream.write(buf, 0, len);
      }
      outputStream.close();
      inputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return outputStream.toString();
  }

  public static Method getMethodSafely(Class<?> cls, String name, Class<?>... parameterTypes) {
    try {
      if (cls == null) {
        return null;
      }
      return cls.getMethod(name, parameterTypes);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return null;
  }

  public static void invokeMethod(Object receiver, Method method, Object... args) throws Exception {
    if (method == null) {
      throw new NullPointerException();
    }

    try {
      method.invoke(receiver, args);
    } catch (Exception exception) {
      exception.printStackTrace();
      throw exception;
    }
  }

  public static boolean isBlankString(final String string) {
    if (string == null || string.trim().isEmpty()) {
      return true;
    }
    return false;
  }

  public static String getRandomString(int length) {
    String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder randomString = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      randomString.append(letters.charAt(new Random().nextInt(letters.length())));
    }

    return randomString.toString();
  }

  public static byte[] readFile(File file) {
    RandomAccessFile rf = null;
    byte[] data = null;
    try {
      rf = new RandomAccessFile(file, "r");
      data = new byte[(int) rf.length()];
      rf.readFully(data);
    } catch (Exception exception) {
      exception.printStackTrace();
    } finally {
      closeQuietly(rf);
    }
    return data;
  }
}