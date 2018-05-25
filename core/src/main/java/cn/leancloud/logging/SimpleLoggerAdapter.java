package cn.leancloud.logging;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.utils.StringUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;

public class SimpleLoggerAdapter extends InternalLoggerAdapter {
  private static ConsoleHandler consoleHandler = new ConsoleHandler();
  static {
    consoleHandler.setLevel(Level.ALL);
  }

  public InternalLogger getLogger(String tag) {
    Logger logger = StringUtil.isEmpty(tag)?Logger.getAnonymousLogger():Logger.getLogger(tag);
    logger.addHandler(consoleHandler);
    InternalLogger ret = new SimpleLogger(logger);
    ret.setLevel(AVOSCloud.getLogLevel());
    return ret;
  }
}
