package cn.leancloud.logging;

import cn.leancloud.LCLogger;

public class DummyLogger extends InternalLogger {
  protected void internalWriteLog(LCLogger.Level level, String msg) {}
  protected void internalWriteLog(LCLogger.Level level, String msg, Throwable tr) {}
  protected void internalWriteLog(LCLogger.Level level, Throwable tr) {}
}
