package cn.leancloud.logging;

import cn.leancloud.AVLogAdapter;
import cn.leancloud.AVLogger;
import cn.leancloud.utils.StringUtil;

import java.util.logging.Logger;

public class SimpleLoggerAdapter extends AVLogAdapter {

  public AVLogger getLogger(String tag) {
    Logger logger = null;
    if (StringUtil.isEmpty(tag)) {
      logger = Logger.getAnonymousLogger();
    } else {
      logger = Logger.getLogger(tag);
    }
    SimpleLogger result = new SimpleLogger(logger);
    result.setLogLevel(this.getLevel());
    return result;
  }
}
