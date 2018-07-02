package cn.leancloud.service;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AppRouterService {
  @GET("/2/route")
  Observable<AppAccessEndpoint> getRouter(@Query("appId") String appId);

  @GET("/v1/route")
  Observable<RTMConnectionServerResponse> getRTMConnectionServer(@Query("appId") String appId, @Query("installationId") String installationId,
                                                       @Query("secure") int secure);
}
