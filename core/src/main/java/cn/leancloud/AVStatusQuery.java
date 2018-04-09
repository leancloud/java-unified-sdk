package cn.leancloud;

public class AVStatusQuery extends AVQuery<AVStatus> {
  public AVStatusQuery() {
    super(AVStatus.CLASS_NAME, AVStatus.class);
    getInclude().add("source");
  }
}
