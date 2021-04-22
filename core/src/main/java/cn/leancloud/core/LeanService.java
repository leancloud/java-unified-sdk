package cn.leancloud.core;

public enum LeanService {

  API("api"), PUSH("push"), RTM("rtm"), STATS("stats"), ENGINE("engine");

  private String service;

  private LeanService(String service) {
    this.service = service;
  }

  @Override
  public String toString() {
    return this.service;
  }
}
