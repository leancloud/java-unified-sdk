package cn.leancloud.logging;

public class DummyLoggerFactory extends InternalLoggerAdapter {
  public InternalLogger getLogger(String tag) {
    return new DummyLogger();
  }
}
