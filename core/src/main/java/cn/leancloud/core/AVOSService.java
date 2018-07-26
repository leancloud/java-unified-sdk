package cn.leancloud.core;

public enum AVOSService {

  API("api"), PUSH("push"), RTM("rtm"), STATS("stats"), ENGINE("engine");

  private String service;

  private AVOSService(String service) {
    this.service = service;
  }

  @Override
  public String toString() {
    return this.service;
  }
}
