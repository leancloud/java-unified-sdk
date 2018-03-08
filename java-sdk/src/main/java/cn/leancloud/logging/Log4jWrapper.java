package cn.leancloud.logging;

import cn.leancloud.AVLogger;
import org.apache.logging.log4j.Logger;

public class Log4jWrapper extends AVLogger {
  Logger logger = null;
  public Log4jWrapper(Logger var) {
    if (null == var) {
      throw new IllegalArgumentException("Logger is null");
    }
    this.logger = var;
  }

  protected void internalWriteLog(Level level, String msg) {
    ;
  }
  protected void internalWriteLog(Level level, String msg, Throwable tr) {
    ;
  }
  protected void internalWriteLog(Level level, Throwable tr) {
    ;
  }

}
