package cn.leancloud.logging;

public abstract class InternalLoggerAdapter {
  public InternalLogger getLogger(Class clazz) {
    if (null == clazz) {
      return null;
    }
    InternalLogger logger = getLogger(clazz.getCanonicalName());
    return logger;
  }

  public abstract InternalLogger getLogger(String tag);
}
