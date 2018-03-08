package cn.leancloud;

public abstract class AVLogger {
  public enum Level {
    OFF(0), ERROR(1),WARNING(2), INFO(3), DEBUG(4), VERBOSE(5), ALL(6);
    private Level(int intLevel) {
      this.intLevel = intLevel;
    }
    public int intLevel() {
      return this.intLevel;
    }
    private int intLevel;
  }

  private Level level = Level.INFO;

  public Level getLogLevel() {
    return level;
  }

  public void setLogLevel(Level logLevel) {
    this.level = logLevel;
  }

  public void v(String msg) {
    writeLog(Level.VERBOSE, msg);
  }

  public void v(String msg, Throwable tr) {
    writeLog(Level.VERBOSE, msg, tr);
  }

  public void d(String msg) {
    writeLog(Level.DEBUG, msg);
  }

  public void d(String msg, Throwable tr) {
    writeLog(Level.DEBUG, msg, tr);
  }

  public void i(String msg) {
    writeLog(Level.INFO, msg);
  }

  public void i(String msg, Throwable tr) {
    writeLog(Level.INFO, msg, tr);
  }

  public void w(String msg) {
    writeLog(Level.WARNING, msg);
  }

  public void w(String msg, Throwable tr) {
    writeLog(Level.WARNING, msg, tr);
  }

  public void w(Throwable tr) {
    writeLog(Level.WARNING, tr);
  }

  public void e(String msg) {
    writeLog(Level.ERROR, msg);
  }

  public void e(String msg, Throwable tr) {
    writeLog(Level.ERROR, msg, tr);
  }

  protected boolean isEnabled(Level testLevel) {
    return this.level.intLevel() > testLevel.intLevel();
  }

  protected abstract void internalWriteLog(Level level, String msg);
  protected abstract void internalWriteLog(Level level, String msg, Throwable tr);
  protected abstract void internalWriteLog(Level level, Throwable tr);

  protected void writeLog(Level level, String msg) {
    if (!isEnabled(level)) {
      return;
    }
    internalWriteLog(level, msg);
  }

  protected void writeLog(Level level, String msg, Throwable tr) {
    if (!isEnabled(level)) {
      return;
    }
    internalWriteLog(level, msg, tr);
  }

  protected void writeLog(Level level, Throwable tr) {
    if (!isEnabled(level)) {
      return;
    }
    internalWriteLog(level, tr);
  }
}
