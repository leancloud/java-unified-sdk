package cn.leancloud.service;

import cn.leancloud.core.AVOSService;
import cn.leancloud.utils.StringUtil;
//import com.alibaba.fastjson.annotation.JSONField;
//import com.alibaba.fastjson.annotation.JSONType;

//@JSONType
public class AppAccessEndpoint {
  //@JSONField(name = "ttl")
  private long ttl;

  //@JSONField(name = "stats_server")
  private String statServer;

  //@JSONField(name = "push_server")
  private String pushServer;

  //@JSONField(name = "rtm_router_server")
  private String rtmRouterServer;

  //@JSONField(name = "api_server")
  private String apiServer;

  //@JSONField(name = "engine_server")
  private String engineServer;

  public long getTtl() {
    return ttl;
  }

  public void setTtl(long ttl) {
    this.ttl = ttl;
  }

  public String getStatServer() {
    return statServer;
  }

  public void setStatServer(String statServer) {
    this.statServer = statServer;
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

  public String getServerHost(AVOSService service) {
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
        result = getStatServer();
        break;
    }
    return result;
  }

  public boolean hasSpecifiedEndpoint() {
    return !StringUtil.isEmpty(apiServer) || !StringUtil.isEmpty(engineServer) || !StringUtil.isEmpty(pushServer)
            || !StringUtil.isEmpty(rtmRouterServer);
  }

  public void freezeEndpoint(AVOSService service, String host) {
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
        this.setStatServer(host);
        break;
    }
  }
}
