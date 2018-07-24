package cn.leancloud.im.v2;

public enum AVIMMessageQueryDirection {
  AVIMMessageQueryDirectionUnknown(-1),
  AVIMMessageQueryDirectionFromNewToOld(0),
  AVIMMessageQueryDirectionFromOldToNew(1);

  private static String descriptions[] = new String[]{"Unknown", "Old", "New"};
  private int code = -1;

  private AVIMMessageQueryDirection(int code) {
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }

  public String getDescription() {
    return descriptions[code + 1];
  }

  public static AVIMMessageQueryDirection parseFromCode(int code) {
    switch (code) {
      case 0:
        return AVIMMessageQueryDirectionFromNewToOld;
      case 1:
        return AVIMMessageQueryDirectionFromOldToNew;
      default:
        return AVIMMessageQueryDirectionUnknown;
    }
  }

}
