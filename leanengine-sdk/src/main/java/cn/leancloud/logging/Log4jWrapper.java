package cn.leancloud.logging;

import cn.leancloud.AVLogger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class Log4jWrapper extends AVLogger {
  Logger logger = null;
  public Log4jWrapper(Logger var) {
    if (null == var) {
      throw new IllegalArgumentException("Logger is null");
    }
    this.logger = var;
  }

  @Override
  public void setLogLevel(Level logLevel) {
    super.setLogLevel(logLevel);
  }

  private org.apache.logging.log4j.Level nativeLevel(Level level) {
    org.apache.logging.log4j.Level rst = org.apache.logging.log4j.Level.OFF;
    switch (level) {
      case ERROR:
        rst = org.apache.logging.log4j.Level.ERROR;
        break;
      case WARNING:
        rst = org.apache.logging.log4j.Level.WARN;
        break;
      case DEBUG:
        rst = org.apache.logging.log4j.Level.DEBUG;
        break;
      case INFO:
        rst = org.apache.logging.log4j.Level.INFO;
        break;
      case VERBOSE:
      case ALL:
        rst = org.apache.logging.log4j.Level.ALL;
        break;
      case OFF:
      default:
        break;
    }
    return rst;
  }

  protected void internalWriteLog(Level level, String msg) {
    logger.log(nativeLevel(level), msg);
  }
  protected void internalWriteLog(Level level, String msg, Throwable tr) {
    logger.log(nativeLevel(level), msg, tr);
  }
  protected void internalWriteLog(Level level, Throwable tr) {
    logger.log(nativeLevel(level), tr);
  }

}
