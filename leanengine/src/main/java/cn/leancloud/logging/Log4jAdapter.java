package cn.leancloud.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4jAdapter extends InternalLoggerAdapter {

  public InternalLogger getLogger(String tag) {
    Logger logger = LogManager.getLogger(tag);
    return new Log4jWrapper(logger);
  }
}
