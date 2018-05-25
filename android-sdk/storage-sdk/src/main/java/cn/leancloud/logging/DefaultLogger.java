package cn.leancloud.logging;

import cn.leancloud.AVLogger.Level;
import android.util.Log;

public class DefaultLogger extends InternalLogger {
  private String tag = null;
  public DefaultLogger(String tag) {
    this.tag = tag;
  }
  protected void internalWriteLog(Level level, String msg) {
    switch (level) {
      case ALL:
      case VERBOSE:
        Log.v(tag, msg);
        break;
      case DEBUG:
        Log.d(tag, msg);
        break;
      case INFO:
        Log.i(tag, msg);
        break;
      case WARNING:
        Log.w(tag, msg);
        break;
      case ERROR:
        Log.e(tag, msg);
        break;
      default:
        break;
    }
  }

  protected void internalWriteLog(Level level, String msg, Throwable tr) {
    switch (level) {
      case ALL:
      case VERBOSE:
        Log.v(tag, msg, tr);
        break;
      case DEBUG:
        Log.d(tag, msg, tr);
        break;
      case INFO:
        Log.i(tag, msg, tr);
        break;
      case WARNING:
        Log.w(tag, msg, tr);
        break;
      case ERROR:
        Log.e(tag, msg, tr);
        break;
      default:
        break;
    }
  }

  protected void internalWriteLog(Level level, Throwable tr) {
    switch (level) {
      case WARNING:
        Log.w(tag, tr);
        break;
      default:
        break;
    }
  }
}
