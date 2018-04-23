package cn.leancloud.service;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryName;


public interface AppRouterService {
  @GET("/2/route")
  Observable<AppAccessEndpoint> getRouter(@QueryName String appId);
}
