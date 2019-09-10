package cn.leancloud.livequery;

/**
 * LiveQuery Connection Handler
 */
public interface AVLiveQueryConnectionHandler {
  /**
   * Connection is open
   */
  void onConnectionOpen();

  /**
   * Connection is close
   */
  void onConnectionClose();

  /**
   * Connection failed.
   * @param code
   * @param reason
   */
  void onConnectionError(int code, String reason);
}
