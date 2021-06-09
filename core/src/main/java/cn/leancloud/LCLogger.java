package cn.leancloud;

import cn.leancloud.core.LeanCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.logging.InternalLogger;
import cn.leancloud.utils.StringUtil;

public class LCLogger {
  public enum Level {
    OFF(0), ERROR(1),WARNING(2), INFO(3), DEBUG(4), VERBOSE(5), ALL(6);
    Level(int intLevel) {
      this.intLevel = intLevel;
    }
    public int intLevel() {
      return this.intLevel;
    }
    private int intLevel;
  }
  private InternalLogger internalLogger = null;
  private String tag = null;

  public LCLogger(String tag) {
    this.tag = tag;
  }

  public void v(String msg) {
    writeLog(Level.VERBOSE, msg);
  }

  public void v(String msg, Throwable tr) {
    writeLog(Level.VERBOSE, msg, tr);
  }

  public void d(String msg) {
    writeLog(Level.DEBUG, msg);
  }

  public void d(String msg, Throwable tr) {
    writeLog(Level.DEBUG, msg, tr);
  }

  public void i(String msg) {
    writeLog(Level.INFO, msg);
  }

  public void i(String msg, Throwable tr) {
    writeLog(Level.INFO, msg, tr);
  }

  public void w(String msg) {
    writeLog(Level.WARNING, msg);
  }

  public void w(String msg, Throwable tr) {
    writeLog(Level.WARNING, msg, tr);
  }

  public void w(Throwable tr) {
    writeLog(Level.WARNING, tr);
  }

  public void e(String msg) {
    writeLog(Level.ERROR, msg);
  }

  public void e(String msg, Throwable tr) {
    writeLog(Level.ERROR, msg, tr);
  }

  private synchronized InternalLogger getInternalLogger() {
    if (null == internalLogger) {
      internalLogger = AppConfiguration.getLogAdapter().getLogger(tag);
    }
    return internalLogger;
  }

  protected boolean isEnabled(Level testLevel) {
    return LeanCloud.getLogLevel().intLevel() >= testLevel.intLevel();
  }


  protected void writeLog(Level level, String msg) {
    if (!isEnabled(level)) {
      return;
    }
    if (null == msg) {
      msg = "";
    }
    InternalLogger internalLogger = getInternalLogger();
    internalLogger.writeLog(level, msg);
  }

  protected void writeLog(Level level, String msg, Throwable tr) {
    if (null == tr) {
      writeLog(level, msg);
      return;
    }
    if (StringUtil.isEmpty(msg)) {
      writeLog(level, tr);
      return;
    }

    if (!isEnabled(level)) {
      return;
    }
    InternalLogger internalLogger = getInternalLogger();
    internalLogger.writeLog(level, msg, tr);
  }

  protected void writeLog(Level level, Throwable tr) {
    if (!isEnabled(level)) {
      return;
    }
    if (null == tr) {
      return;
    }
    InternalLogger internalLogger = getInternalLogger();
    internalLogger.writeLog(level, tr);
  }
}
