package cn.leancloud.logging;

import cn.leancloud.AVLogAdapter;
import cn.leancloud.AVLogger;

public class DefaultLoggerAdapter extends AVLogAdapter {
  public AVLogger getLogger(String tag) {
    return new DefaultLogger(tag);
  }
}
