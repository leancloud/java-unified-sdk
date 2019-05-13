package cn.leancloud.push.lite.rest;

import com.alibaba.fastjson.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PushRestAPI {
  @POST("/1.1/push")
  Call<JSONObject> sendPushRequest(@Body JSONObject param);

  @POST("/1.1/installations")
  Call<JSONObject> saveInstallation(@Body JSONObject param, @Query("fetchWhenSave") boolean fetchFlag);

  @GET("/2/route")
  Call<JSONObject> getRouter(@Query("appId") String appId);
}
