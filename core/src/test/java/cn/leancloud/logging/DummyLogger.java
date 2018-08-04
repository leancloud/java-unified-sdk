package cn.leancloud.logging;

import cn.leancloud.AVLogger;

public class DummyLogger extends InternalLogger {
  protected void internalWriteLog(AVLogger.Level level, String msg) {}
  protected void internalWriteLog(AVLogger.Level level, String msg, Throwable tr) {}
  protected void internalWriteLog(AVLogger.Level level, Throwable tr) {}
}
