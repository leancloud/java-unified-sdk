package cn.leancloud.push.lite.rest;

import com.alibaba.fastjson.JSONObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PushRouterRestAPI {
  @GET("/v1/route")
  Call<JSONObject> getWSServer(@Query("appId") String appId, @Query("installationId") String installationId,
                               @Query("secure") int secure);
}
