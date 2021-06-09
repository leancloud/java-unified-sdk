package cn.leancloud.im.v2;

/**
 * Message Query Interval
 * Created by fengjunwen on 2017/9/5.
 */

public class LCIMMessageInterval {
  public AVIMMessageIntervalBound startIntervalBound;
  public AVIMMessageIntervalBound endIntervalBound;

  public static class AVIMMessageIntervalBound {
    public String messageId;
    public long timestamp;
    public boolean closed;
    private AVIMMessageIntervalBound(String messageId, long timestamp, boolean closed) {
      this.messageId = messageId;
      this.timestamp = timestamp;
      this.closed = closed;
    }
  }

  /**
   * create query bound
   * @param messageId - message id
   * @param timestamp - message timestamp
   * @param closed    - included specified message flag.
   *                    true: include
   *                    false: not include.
   * @return  query interval bound instance
   */
  public static AVIMMessageIntervalBound createBound(String messageId, long timestamp, boolean closed) {
    return new AVIMMessageIntervalBound(messageId, timestamp, closed);
  }

  /**
   * query interval constructor.
   * @param start - interval start bound
   * @param end   - interval end bound
   */
  public LCIMMessageInterval(AVIMMessageIntervalBound start, AVIMMessageIntervalBound end) {
    this.startIntervalBound = start;
    this.endIntervalBound = end;
  }
}
