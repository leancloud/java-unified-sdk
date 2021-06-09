package cn.leancloud.logging;

import cn.leancloud.LCLogger;

public abstract class InternalLogger {
  private LCLogger.Level level = LCLogger.Level.INFO;

  public void setLevel(LCLogger.Level level) {
    this.level = level;
  }

  protected abstract void internalWriteLog(LCLogger.Level level, String msg);
  protected abstract void internalWriteLog(LCLogger.Level level, String msg, Throwable tr);
  protected abstract void internalWriteLog(LCLogger.Level level, Throwable tr);

  public void writeLog(LCLogger.Level level, String msg) {
    internalWriteLog(level, msg);
  }

  public void writeLog(LCLogger.Level level, String msg, Throwable tr) {
    internalWriteLog(level, msg, tr);
  }

  public void writeLog(LCLogger.Level level, Throwable tr) {
    internalWriteLog(level, tr);
  }
}
