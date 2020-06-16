package cn.leancloud.impl;

import cn.leancloud.EngineAppConfiguration;
import cn.leancloud.core.AppRouter;
import cn.leancloud.service.AppAccessEndpoint;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;

public class EnvFirstAppRouter extends AppRouter {
  private boolean isLocalEngineCall = false;

  @Override
  public Observable<AppAccessEndpoint> fetchServerHostsInBackground(String appId) {
    String apiServer = EngineAppConfiguration.getEnvOrProperty("LEANCLOUD_API_SERVER");
    String apiPort = EngineAppConfiguration.getEnvOrProperty("LEANCLOUD_APP_PORT");
    if (!StringUtil.isEmpty(apiServer)) {
      AppAccessEndpoint accessEndpoint = new AppAccessEndpoint();
      accessEndpoint.setTtl(36000);
      accessEndpoint.setStatsServer(apiServer);
      accessEndpoint.setRtmRouterServer(apiServer);
      accessEndpoint.setPushServer(apiServer);
      if (isLocalEngineCall) {
        accessEndpoint.setEngineServer("http://0.0.0.0:" + apiPort);
      } else {
        accessEndpoint.setEngineServer(apiServer);
      }
      accessEndpoint.setApiServer(apiServer);
      return Observable.just(accessEndpoint);
    }
    return super.fetchServerHostsInBackground(appId);
  }

  public void setLocalEngineCallEnabled(boolean enabled) {
    isLocalEngineCall  = enabled;
  }

}
