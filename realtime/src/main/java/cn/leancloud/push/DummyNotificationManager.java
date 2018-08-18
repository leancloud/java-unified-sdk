package cn.leancloud.push;

class DummyNotificationManager extends AVNotificationManager {
  String getApplicationName() {
    return "Notification";
  }

  void sendNotification(String from, String msg) throws IllegalArgumentException {
    ;
  }

  void sendBroadcast(String channel, String msg, String action) {
    ;
  }
}
