package cn.leancloud.logging;

import cn.leancloud.AVLogAdapter;
import cn.leancloud.AVLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4jAdapter extends AVLogAdapter {

  public AVLogger getLogger(String tag) {
    Logger logger = LogManager.getLogger(tag);
    return new Log4jWrapper(logger);
  }
}
