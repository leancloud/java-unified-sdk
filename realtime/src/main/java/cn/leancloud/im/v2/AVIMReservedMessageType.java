package cn.leancloud.im.v2;

/**
 * 预留的 MessageType
 *
 */
public enum AVIMReservedMessageType {
  UnsupportedMessageType(0),
  TextMessageType(-1),
  ImageMessageType(-2),
  AudioMessageType(-3),
  VideoMessageType(-4),
  LocationMessageType(-5),
  FileMessageType(-6);
  int type;

  AVIMReservedMessageType(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public static AVIMReservedMessageType getAVIMReservedMessageType(int type) {
    switch (type) {
      case 0:
        return UnsupportedMessageType;
      case -1:
        return TextMessageType;
      case -2:
        return ImageMessageType;
      case -3:
        return AudioMessageType;
      case -4:
        return VideoMessageType;
      case -5:
        return LocationMessageType;
      case -6:
        return FileMessageType;
      default:
        return UnsupportedMessageType;
    }
  }
}