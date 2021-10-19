package cn.leancloud.service;

import cn.leancloud.core.LeanService;
import cn.leancloud.utils.StringUtil;

public class AppAccessEndpoint {
  private long ttl;

  private String statsServer;

  private String pushServer;

  private String rtmRouterServer;

  private String apiServer;

  private String engineServer;

  public long getTtl() {
    return ttl;
  }

  public void setTtl(long ttl) {
    this.ttl = ttl;
  }

  public String getStatsServer() {
    return statsServer;
  }

  public void setStatsServer(String statsServer) {
    this.statsServer = statsServer;
  }

  public String getPushServer() {
    return pushServer;
  }

  public void setPushServer(String pushServer) {
    this.pushServer = pushServer;
  }

  public String getRtmRouterServer() {
    return rtmRouterServer;
  }

  public void setRtmRouterServer(String rtmRouterServer) {
    this.rtmRouterServer = rtmRouterServer;
  }

  public String getApiServer() {
    return apiServer;
  }

  public void setApiServer(String apiServer) {
    this.apiServer = apiServer;
  }

  public String getEngineServer() {
    return engineServer;
  }

  public void setEngineServer(String engineServer) {
    this.engineServer = engineServer;
  }

  public String getServerHost(LeanService service) {
    String result = "";
    switch (service) {
      case API:
        result = getApiServer();
        break;
      case ENGINE:
        result = getEngineServer();
        break;
      case PUSH:
        result = getPushServer();
        break;
      case RTM:
        result = getRtmRouterServer();
        break;
      case STATS:
        result = getStatsServer();
        break;
    }
    return result;
  }

  public boolean hasSpecifiedEndpoint() {
    return !StringUtil.isEmpty(apiServer) || !StringUtil.isEmpty(engineServer) || !StringUtil.isEmpty(pushServer)
            || !StringUtil.isEmpty(rtmRouterServer);
  }

  public void reset() {
    this.ttl = 0;
    this.apiServer = null;
    this.engineServer = null;
    this.pushServer = null;
    this.rtmRouterServer = null;
    this.statsServer = null;
  }

  public void freezeEndpoint(LeanService service, String host) {
    switch (service) {
      case API:
        this.setApiServer(host);
        break;
      case ENGINE:
        this.setEngineServer(host);
        break;
      case PUSH:
        this.setPushServer(host);
        break;
      case RTM:
        this.setRtmRouterServer(host);
        break;
      case STATS:
        this.setStatsServer(host);
        break;
    }
  }
}
