package cn.leancloud.logging;

public class DefaultLoggerAdapter extends InternalLoggerAdapter {
  public InternalLogger getLogger(String tag) {
    return new DefaultLogger(tag);
  }
}
