package cn.leancloud.logging;

import cn.leancloud.AVLogger;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SimpleLogger extends AVLogger {
  private Logger logger = null;

  public SimpleLogger(Logger logger) {
    if (null == logger) {
      throw new IllegalArgumentException("Logger is null.");
    }
    this.logger = logger;
  }

  @Override
  public void setLogLevel(Level logLevel) {
    super.setLogLevel(logLevel);
    java.util.logging.Level utilLevel = getNativeLevel(logLevel);
    this.logger.setLevel(utilLevel);
  }

  private java.util.logging.Level getNativeLevel(Level level) {
    java.util.logging.Level result;
    switch (level) {
      case OFF:
        result = java.util.logging.Level.OFF;
        break;
      case ERROR:
        result = java.util.logging.Level.SEVERE;
        break;
      case WARNING:
        result = java.util.logging.Level.WARNING;
        break;
      case INFO:
        result = java.util.logging.Level.INFO;
        break;
      case DEBUG:
        result = java.util.logging.Level.FINE;
        break;
      case VERBOSE:
        result = java.util.logging.Level.FINER;
        break;
      default:
        result = java.util.logging.Level.ALL;
        break;
    }
    return result;
  }

  protected void internalWriteLog(Level level, String msg) {
    java.util.logging.Level nativeLevel = getNativeLevel(level);
    this.logger.log(nativeLevel, msg);
  }

  protected void internalWriteLog(Level level, String msg, Throwable tr) {
    java.util.logging.Level nativeLevel = getNativeLevel(level);
    this.logger.log(nativeLevel, msg, tr);
  }

  protected void internalWriteLog(Level level, Throwable tr) {
    java.util.logging.Level nativeLevel = getNativeLevel(level);
    LogRecord record = new LogRecord(nativeLevel, "");
    record.setThrown(tr);
    this.logger.log(record);
  }
}