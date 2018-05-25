package cn.leancloud.logging;

import cn.leancloud.AVLogger;

public abstract class InternalLogger {
  private AVLogger.Level level = AVLogger.Level.INFO;

  public void setLevel(AVLogger.Level level) {
    this.level = level;
  }

  protected abstract void internalWriteLog(AVLogger.Level level, String msg);
  protected abstract void internalWriteLog(AVLogger.Level level, String msg, Throwable tr);
  protected abstract void internalWriteLog(AVLogger.Level level, Throwable tr);

  public void writeLog(AVLogger.Level level, String msg) {
    internalWriteLog(level, msg);
  }

  public void writeLog(AVLogger.Level level, String msg, Throwable tr) {
    internalWriteLog(level, msg, tr);
  }

  public void writeLog(AVLogger.Level level, Throwable tr) {
    internalWriteLog(level, tr);
  }
}
