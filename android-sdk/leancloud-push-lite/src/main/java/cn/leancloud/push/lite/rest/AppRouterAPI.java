package cn.leancloud.push.lite.rest;

import com.alibaba.fastjson.JSONObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AppRouterAPI {
  @GET("/2/route")
  Call<JSONObject> findServers(@Query("appId") String appId);
}
