package cn.leancloud.service;

import cn.leancloud.json.JSONObject;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PushService {
  @POST("/1.1/push")
  Observable<JSONObject> sendPushRequest(@Body JSONObject param);

}
