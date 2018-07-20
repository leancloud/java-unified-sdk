package cn.leancloud.service;

import cn.leancloud.AVObject;
import cn.leancloud.im.Signature;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RealtimeService {
  @POST("/1.1/installation")
  Observable<AVObject> saveInstallation();

  @POST("/1.1/push")
  Observable<AVObject> sendPushRequest();

  @POST("/1.1/rtm/sign")
  Observable<Signature> createSignature(@Body JSONObject sessionToken);
}
