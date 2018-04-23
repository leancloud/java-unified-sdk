package cn.leancloud.core;

public enum AVOSServices {

  API("api"), PUSH("push"), RTM("rtm"), STATS("stats"), ENGINE("engine");

  private String service;

  private AVOSServices(String service) {
    this.service = service;
  }

  @Override
  public String toString() {
    return this.service;
  }
}
