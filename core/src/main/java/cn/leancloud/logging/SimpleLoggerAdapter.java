package cn.leancloud.logging;

import cn.leancloud.AVLogAdapter;
import cn.leancloud.AVLogger;
import cn.leancloud.utils.StringUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;

public class SimpleLoggerAdapter extends AVLogAdapter {
  private static ConsoleHandler consoleHandler = new ConsoleHandler();
  static {
    consoleHandler.setLevel(Level.ALL);
  }

  protected AVLogger getLogger(String tag) {
    Logger logger = null;
    if (StringUtil.isEmpty(tag)) {
      logger = Logger.getAnonymousLogger();
    } else {
      logger = Logger.getLogger(tag);
    }
    logger.addHandler(consoleHandler);
    SimpleLogger result = new SimpleLogger(logger);
    result.setLogLevel(this.getLevel());
    return result;
  }
}
