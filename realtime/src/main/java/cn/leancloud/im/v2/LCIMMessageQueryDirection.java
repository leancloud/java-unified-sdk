package cn.leancloud.im.v2;

public enum LCIMMessageQueryDirection {
  DirectionUnknown(-1),
  DirectionFromNewToOld(0),
  DirectionFromOldToNew(1);

  private static String descriptions[] = new String[]{"Unknown", "Old", "New"};
  private int code = -1;

  private LCIMMessageQueryDirection(int code) {
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }

  public String getDescription() {
    return descriptions[code + 1];
  }

  public static LCIMMessageQueryDirection parseFromCode(int code) {
    switch (code) {
      case 0:
        return DirectionFromNewToOld;
      case 1:
        return DirectionFromOldToNew;
      default:
        return DirectionUnknown;
    }
  }

}
