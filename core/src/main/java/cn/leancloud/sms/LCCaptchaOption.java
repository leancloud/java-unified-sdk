package cn.leancloud.sms;

import java.util.HashMap;
import java.util.Map;

public class LCCaptchaOption {
  private int width = 0;
  private int height = 0;

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public Map<String, String> getRequestParam() {
    Map<String, String> result = new HashMap<String, String>(2);
    result.put("height", String.valueOf(height));
    result.put("width", String.valueOf(width));
    return result;
  }
}
