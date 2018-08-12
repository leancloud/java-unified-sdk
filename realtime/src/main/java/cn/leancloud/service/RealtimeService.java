package cn.leancloud.service;

import cn.leancloud.AVObject;
import cn.leancloud.im.Signature;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface RealtimeService {
  @POST("/1.1/push")
  Observable<AVObject> sendPushRequest();

  @POST("/1.1/rtm/sign")
  Observable<Signature> createSignature(@Body JSONObject sessionToken);

  @GET("/1.1/classes/_ConversationMemberInfo")
  Observable<Map<String, List<JSONObject>>> queryMemberInfo(@Header("X-LC-IM-Session-Token") String realtimeSessionToken,
                                               @QueryMap Map<String, String> query);

  @POST("/1.1/LiveQuery/subscribe")
  Observable<JSONObject> subscribe(@Body JSONObject param);

  @POST("/1.1/LiveQuery/unsubscribe")
  Observable<JSONObject> unsubscribe(@Body JSONObject param);

}
