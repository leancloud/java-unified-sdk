package cn.leancloud.core;

import cn.leancloud.AVLogger;
import cn.leancloud.cache.SystemSetting;

import cn.leancloud.network.DNSDetoxicant;
import cn.leancloud.service.AppAccessEndpoint;
import cn.leancloud.service.AppRouterService;
import cn.leancloud.service.RTMConnectionServerResponse;
import cn.leancloud.utils.LogUtil;

import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import io.reactivex.Observable;

import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class AppRouter {
  private static final AVLogger LOGGER = LogUtil.getLogger(AppRouter.class);
  private static final String APP_ROUTER_HOST = "https://app-router.com";
  private static final AppRouter INSTANCE = new AppRouter();
  public static AppRouter getInstance() {
    return INSTANCE;
  }

  /**
   * 华北区 app router 请求与结果
   * https://app-router.com/2/route?appId=EDR0rD8otnmzF7zNGgLasHzi-MdYXbMMI
   * {
   *    ttl: 3600,
   *    stats_server: "nlqwjxku.stats.lncld.net",
   *    rtm_router_server: "nlqwjxku.rtm.lncld.net",
   *    push_server: "nlqwjxku.push.lncld.net",
   *    engine_server: "nlqwjxku.engine.lncld.net",
   *    api_server: "nlqwjxku.api.lncld.net",
   * }
   *
   * 华东区 app router 请求与结果
   * https://app-router.com/2/route?appId=qwTQb5S80beMUMGg3xtHsEka-9Nh9j0Va
   * {
   *    ttl: 3600,
   *    stats_server: "qwtqb5s8.stats.lncldapi.com",
   *    rtm_router_server: "qwtqb5s8.rtm.lncldapi.com",
   *    push_server: "qwtqb5s8.push.lncldapi.com",
   *    engine_server: "qwtqb5s8.engine.lncldapi.com",
   *    api_server: "qwtqb5s8.api.lncldapi.com",
   * }
   *
   * 美国区 app router 请求与结果
   * https://app-router.com/2/route?appId=EDR0rD8otnmzF7zNGgLasHzi-MdYXbMMI
   * {
   *    ttl: 3600,
   *    stats_server: "us-api.leancloud.cn",
   *    rtm_router_server: "router-a0-push.leancloud.cn",
   *    push_server: "us-api.leancloud.cn",
   *    engine_server: "us-api.leancloud.cn",
   *    api_server: "us-api.leancloud.cn",
   * }
   */
  private static final String DEFAULT_SERVER_HOST_FORMAT = "https://%s.%s.%s";
  private static final String DEFAULT_SERVER_API = AVOSService.API.toString();
  private static final String DEFAULT_SERVER_STAT = AVOSService.STATS.toString();
  private static final String DEFAULT_SERVER_ENGINE = AVOSService.ENGINE.toString();
  private static final String DEFAULT_SERVER_PUSH = AVOSService.PUSH.toString();
  private static final String DEFAULT_SERVER_RTM_ROUTER = AVOSService.RTM.toString();

  private static final String DEFAULT_REGION_EAST_CHINA = "lncldapi.com";
  private static final String DEFAULT_REGION_NORTH_CHINA = "lncld.net";
  private static final String DEFAULT_REGION_NORTH_AMERICA = "lncldglobal.com";

  public static AVOSCloud.REGION getAppRegion(String applicationId) {
    if (StringUtil.isEmpty(applicationId)) {
      return AVOSCloud.REGION.NorthChina;
    }
    if (applicationId.endsWith("-MdYXbMMI")) {
      return AVOSCloud.REGION.NorthAmerica;
    }
    if (applicationId.endsWith("-9Nh9j0Va")) {
      return AVOSCloud.REGION.EastChina;
    }
    return AVOSCloud.REGION.NorthChina;
  }

  private Retrofit retrofit = null;
  private AppAccessEndpoint defaultEndpoint = null;
  private AppAccessEndpoint customizedEndpoint = new AppAccessEndpoint();

  protected AppRouter() {
    OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(new LoggingInterceptor())
            .dns(new DNSDetoxicant())
            .build();
    retrofit = new Retrofit.Builder()
            .baseUrl(APP_ROUTER_HOST)
            .addConverterFactory(FastJsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient)
            .build();
  }

  protected AppAccessEndpoint buildDefaultEndpoint(String appId) {
    AppAccessEndpoint result = new AppAccessEndpoint();
    String appIdPrefix = appId.substring(0, 8).toLowerCase();
    AVOSCloud.REGION region = getAppRegion(appId);
    String lastHost = "";
    switch (region) {
      case NorthChina:
        lastHost = DEFAULT_REGION_NORTH_CHINA;
        break;
      case EastChina:
        lastHost = DEFAULT_REGION_EAST_CHINA;
        break;
      case NorthAmerica:
        lastHost = DEFAULT_REGION_NORTH_AMERICA;
        break;
      default:
        LOGGER.w("Invalid region");
        break;
    }
    result.setApiServer(String.format(DEFAULT_SERVER_HOST_FORMAT, appIdPrefix, DEFAULT_SERVER_API, lastHost));
    result.setEngineServer(String.format(DEFAULT_SERVER_HOST_FORMAT, appIdPrefix, DEFAULT_SERVER_ENGINE, lastHost));
    result.setPushServer(String.format(DEFAULT_SERVER_HOST_FORMAT, appIdPrefix, DEFAULT_SERVER_PUSH, lastHost));
    result.setRtmRouterServer(String.format(DEFAULT_SERVER_HOST_FORMAT, appIdPrefix, DEFAULT_SERVER_RTM_ROUTER, lastHost));
    result.setStatServer(String.format(DEFAULT_SERVER_HOST_FORMAT, appIdPrefix, DEFAULT_SERVER_STAT, lastHost));
    result.setTtl(36000 + System.currentTimeMillis() / 1000);
    return result;
  }

  private Observable<String> fetchServerFromRemote(final String appId, final AVOSService service) {
    return fetchServerHostsInBackground(appId).map(new Function<AppAccessEndpoint, String>() {
      @Override
      public String apply(AppAccessEndpoint appAccessEndpoint) throws Exception {
        String result = "";
        switch (service) {
          case API:
            result = appAccessEndpoint.getApiServer();
            break;
          case ENGINE:
            result = appAccessEndpoint.getEngineServer();
            break;
          case PUSH:
            result = appAccessEndpoint.getPushServer();
            break;
          case RTM:
            result = appAccessEndpoint.getRtmRouterServer();
            break;
          case STATS:
            result = appAccessEndpoint.getStatServer();
            break;
          default:
            break;
        }
        if (!StringUtil.isEmpty(result) && !result.startsWith("http")) {
          result = "https://" + result;
        }
        return result;
      }
    });
  }

  public boolean hasFrozenEndpoint() {
    return this.customizedEndpoint.hasSpecifiedEndpoint();
  }

  public void freezeEndpoint(final AVOSService service, String host) {
    this.customizedEndpoint.freezeEndpoint(service, host);
  }

  public Observable<String> getEndpoint(final String appId, final AVOSService service) {
    return getEndpoint(appId, service, false);
  }

  private Observable<String> getEndpoint(final String appId, final AVOSService service, boolean forceUpdate) {
    String fixedHost = this.customizedEndpoint.getServerHost(service);
    if (!StringUtil.isEmpty(fixedHost)) {
      return Observable.just(fixedHost);
    }

    if (forceUpdate) {
      // force to update from server.
      return fetchServerFromRemote(appId, service);
    }

    if (null == this.defaultEndpoint) {
      SystemSetting setting = AppConfiguration.getDefaultSetting();
      String cachedResult = null;
      if (null != setting) {
        cachedResult = setting.getString(getPersistenceKeyZone(appId, true), appId, "");
      }
      if (!StringUtil.isEmpty(cachedResult)) {
        defaultEndpoint = JSON.parseObject(cachedResult, AppAccessEndpoint.class);
        long currentSeconds = System.currentTimeMillis() / 1000;
        if (currentSeconds > defaultEndpoint.getTtl()) {
          defaultEndpoint = null;
        }
      }
      if (null == defaultEndpoint) {
        defaultEndpoint = buildDefaultEndpoint(appId);
      }
    }

    String result = null;
    switch (service) {
      case API:
        result = this.defaultEndpoint.getApiServer();
        break;
      case ENGINE:
        result = this.defaultEndpoint.getEngineServer();
        break;
      case PUSH:
        result = this.defaultEndpoint.getPushServer();
        break;
      case RTM:
        result = this.defaultEndpoint.getRtmRouterServer();
        break;
      case STATS:
        result = this.defaultEndpoint.getStatServer();
        break;
      default:
        break;
    }
    if (!StringUtil.isEmpty(result) && !result.startsWith("http")) {
      result = "https://" + result;
    }
    return Observable.just(result);
  }

  public Observable<AppAccessEndpoint> fetchServerHostsInBackground(final String appId) {
    AppRouterService service = retrofit.create(AppRouterService.class);
    Observable<AppAccessEndpoint> result = service.getRouter(appId);
    if (AppConfiguration.isAsynchronized()) {
      result = result.subscribeOn(Schedulers.io());
    }
    AppConfiguration.SchedulerCreator creator = AppConfiguration.getDefaultScheduler();
    if (null != creator) {
      result = result.observeOn(creator.create());
    }
    return result.map(new Function<AppAccessEndpoint, AppAccessEndpoint>() {
      @Override
      public AppAccessEndpoint apply(AppAccessEndpoint appAccessEndpoint) throws Exception {
        // save result to local cache.
        LOGGER.d(appAccessEndpoint.toString());
        AppRouter.this.defaultEndpoint = appAccessEndpoint;
        AppRouter.this.defaultEndpoint.setTtl(appAccessEndpoint.getTtl() + System.currentTimeMillis() / 1000);
        SystemSetting setting = AppConfiguration.getDefaultSetting();
        if (null != setting) {
          String endPoints = JSON.toJSONString(AppRouter.this.defaultEndpoint);
          setting.saveString(getPersistenceKeyZone(appId, true), appId, endPoints);
        }
        return AppRouter.this.defaultEndpoint;
      }
    });
  }

  private Observable<RTMConnectionServerResponse> fetchRTMServerFromRemote(final String routerHost, final String appId,
                                                                           final String installationId, int secure) {
    LOGGER.d("fetchRTMServerFromRemote. router=" + routerHost + ", appId=" + appId);
    Retrofit tmpRetrofit = retrofit.newBuilder().baseUrl(routerHost).build();
    AppRouterService tmpService = tmpRetrofit.create(AppRouterService.class);
    Observable<RTMConnectionServerResponse> result = tmpService.getRTMConnectionServer(appId, installationId, secure);
    if (AppConfiguration.isAsynchronized()) {
      result = result.subscribeOn(Schedulers.io());
    }
    AppConfiguration.SchedulerCreator creator = AppConfiguration.getDefaultScheduler();
    if (null != creator) {
      result = result.observeOn(creator.create());
    }
    return result.map(new Function<RTMConnectionServerResponse, RTMConnectionServerResponse>() {
      @Override
      public RTMConnectionServerResponse apply(RTMConnectionServerResponse rtmConnectionServerResponse) throws Exception {
        SystemSetting setting = AppConfiguration.getDefaultSetting();
        if (null != rtmConnectionServerResponse && null != setting) {
          rtmConnectionServerResponse.setTtl(rtmConnectionServerResponse.getTtl() + System.currentTimeMillis() / 1000);
          String cacheResult = JSON.toJSONString(rtmConnectionServerResponse);
          setting.saveString(getPersistenceKeyZone(appId, false), routerHost, cacheResult);
        }
        return rtmConnectionServerResponse;
      }
    });
  }

  public Observable<RTMConnectionServerResponse> fetchRTMConnectionServer(final String routerHost, final String appId,
                                                                          final String installationId, int secure,
                                                                          boolean forceUpdate) {
    if (!forceUpdate) {
      RTMConnectionServerResponse cachedResponse = null;
      SystemSetting setting = AppConfiguration.getDefaultSetting();
      if (null != setting) {
        String cacheServer = setting.getString(getPersistenceKeyZone(appId, false), routerHost, "");
        if (!StringUtil.isEmpty(cacheServer)) {
          try {
            cachedResponse = JSON.parseObject(cacheServer, RTMConnectionServerResponse.class);
            long currentSeconds = System.currentTimeMillis()/1000;
            if (currentSeconds > cachedResponse.getTtl()) {
              // cache is out of date.
              setting.removeKey(getPersistenceKeyZone(appId, false), routerHost);
              cachedResponse = null;
            }
            if (null != cachedResponse) {
              return Observable.just(cachedResponse);
            }
          } catch (Exception ex) {
            cachedResponse = null;
            setting.removeKey(getPersistenceKeyZone(appId, false), routerHost);
          }
        }
      }
    }
    return fetchRTMServerFromRemote(routerHost, appId, installationId, secure);
  }

  protected String getPersistenceKeyZone(String appId, boolean forAPIEndpoints) {
    if (forAPIEndpoints) {
      return "com.avos.avoscloud.approuter." + appId;
    } else {
      return "com.avos.push.router.server.cache" + appId;
    }
  }

}
