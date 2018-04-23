package cn.leancloud.core;

import cn.leancloud.AVLogger;
import cn.leancloud.cache.SystemSetting;

import cn.leancloud.network.DNSDetoxicant;
import cn.leancloud.service.AppAccessEndpoint;
import cn.leancloud.service.AppRouterService;
import cn.leancloud.utils.LogUtil;

import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import io.reactivex.Observable;
import io.reactivex.Observer;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class AppRouter {
  private static final AVLogger LOGGER = LogUtil.getLogger(AppRouter.class);
  private static final String APP_ROUTER_HOST = "https://app-router.leancloud.cn";
  private static final AppRouter INSTANCE = new AppRouter();

  /**
   * 华北区 app router 请求与结果
   * https://app-router.leancloud.cn/2/route?appId=EDR0rD8otnmzF7zNGgLasHzi-MdYXbMMI
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
   * https://app-router.leancloud.cn/2/route?appId=qwTQb5S80beMUMGg3xtHsEka-9Nh9j0Va
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
   * https://app-router.leancloud.cn/2/route?appId=EDR0rD8otnmzF7zNGgLasHzi-MdYXbMMI
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
  private static final String DEFAULT_SERVER_API = AVOSServices.API.toString();
  private static final String DEFAULT_SERVER_STAT = AVOSServices.STATS.toString();
  private static final String DEFAULT_SERVER_ENGINE = AVOSServices.ENGINE.toString();
  private static final String DEFAULT_SERVER_PUSH = AVOSServices.PUSH.toString();
  private static final String DEFAULT_SERVER_RTM_ROUTER = AVOSServices.RTM.toString();

  private static final String DEFAULT_REGION_EAST_CHINA = "lncldapi.com";
  private static final String DEFAULT_REGION_NORTH_CHINA = "lncld.net";
  private static final String DEFAULT_REGION_NORTH_AMERICA = "lncldglobal.com";

  public static AVOSCloud.REGION getAppRegion(String applicationId) {
    return AVOSCloud.REGION.NorthChina;
  }

  private OkHttpClient httpClient = null;
  private AppRouterService service = null;
  private Retrofit retrofit = null;
  private AppAccessEndpoint appAccessEndpoint = null;

  protected AppRouter() {
    httpClient = new OkHttpClient.Builder()
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
    service = retrofit.create(AppRouterService.class);
  }

  protected AppAccessEndpoint buildDefaultEndpoint(String appId) {
    AppAccessEndpoint result = new AppAccessEndpoint();
    String appIdPrefix = appId.substring(0, 8).toLowerCase();
    AVOSCloud.REGION region = AVOSCloud.getRegion();
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
    result.setTtl(36000);
    return result;
  }

  public String getEndpoint(final String appId, AVOSServices service, boolean forceUpdate) {
    if (forceUpdate) {
      // force to update from server.
      fetchServerHosts(appId);
    }
    if (null == this.appAccessEndpoint) {
      SystemSetting setting = AppConfiguration.getDefaultSetting();
      String cachedResult = null;
      if (null != setting) {
        cachedResult = setting.getString(getAppRouterSPName(appId), appId, "");
      }
      if (StringUtil.isEmpty(cachedResult)) {
        appAccessEndpoint = JSON.parseObject(cachedResult, AppAccessEndpoint.class);
      } else {
        appAccessEndpoint = buildDefaultEndpoint(appId);
      }
    }
    String result = null;
    if (null != this.appAccessEndpoint) {
      switch (service) {
        case API:
          result = this.appAccessEndpoint.getApiServer();
          break;
        case ENGINE:
          result = this.appAccessEndpoint.getEngineServer();
          break;
        case PUSH:
          result = this.appAccessEndpoint.getPushServer();
          break;
        case RTM:
          result = this.appAccessEndpoint.getRtmRouterServer();
          break;
        case STATS:
          result = this.appAccessEndpoint.getStatServer();
          break;
          default:
            break;
      }
    }
    return result;
  }

  public Observable<AppAccessEndpoint> fetchServerHostsInBackground(String appId) {
    Observable<AppAccessEndpoint> result = service.getRouter(appId);
    if (AppConfiguration.isAsynchronized()) {
      result = result.subscribeOn(Schedulers.io());
    }
    AppConfiguration.SchedulerCreator creator = AppConfiguration.getDefaultScheduler();
    if (null != creator) {
      result = result.subscribeOn(creator.create());
    }
    return result;
  }

  public void fetchServerHosts(final String appId) {
    fetchServerHostsInBackground(appId).subscribe(new Observer<AppAccessEndpoint>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(AppAccessEndpoint appAccessEndpoint) {
        // save result to local cache.
        LOGGER.d(appAccessEndpoint.toString());
        AppRouter.this.appAccessEndpoint = appAccessEndpoint;
        String endPoints = JSON.toJSONString(appAccessEndpoint);
        SystemSetting setting = AppConfiguration.getDefaultSetting();
        if (null != setting) {
          setting.saveString(getAppRouterSPName(appId), appId, endPoints);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        LOGGER.w("failed to retrieve app router data.", throwable);
      }

      @Override
      public void onComplete() {
      }
    });
  }

  protected String getAppRouterSPName(String appId) {
    return "com.avos.avoscloud.approuter." + appId;
  }

}
