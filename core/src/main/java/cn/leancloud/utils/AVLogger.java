package cn.leancloud.utils;

public abstract class AVLogger {
  public static final int LOG_LEVEL_VERBOSE = 1 << 1;
  public static final int LOG_LEVEL_DEBUG = 1 << 2;
  public static final int LOG_LEVEL_INFO = 1 << 3;
  public static final int LOG_LEVEL_WARNING = 1 << 4;
  public static final int LOG_LEVEL_ERROR = 1 << 5;
  public static final int LOG_LEVEL_NONE = ~0;

  private boolean debugEnabled;
  private int logLevel = LOG_LEVEL_NONE;

  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  public void setDebugEnabled(boolean enable) {
    debugEnabled = enable;
  }

  public boolean showInternalDebugLog() {
    return false;
  }

  public int getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(int logLevel) {
    this.logLevel = logLevel;
  }

  public abstract int v(String tag, String msg);

  public abstract int v(String tag, String msg, Throwable tr);

  public abstract int d(String tag, String msg);

  public abstract int d(String tag, String msg, Throwable tr);

  public abstract int i(String tag, String msg);

  public abstract int i(String tag, String msg, Throwable tr);

  public abstract int w(String tag, String msg);

  public abstract int w(String tag, String msg, Throwable tr);


  public abstract int w(String tag, Throwable tr);

  public abstract int e(String tag, String msg);

  public abstract int e(String tag, String msg, Throwable tr);
}
