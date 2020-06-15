package cn.leancloud.service;

import cn.leancloud.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cn.leancloud.query.AVQueryResult;
import cn.leancloud.search.AVSearchResponse;
import cn.leancloud.sms.AVCaptchaDigest;
import cn.leancloud.sms.AVCaptchaValidateResult;
import cn.leancloud.types.AVDate;
import cn.leancloud.types.AVNull;
import cn.leancloud.upload.FileUploadToken;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface APIService {
  /**
   * Object Operations.
   */

  @GET("/1.1/classes/{className}")
  Observable<List<? extends AVObject>> findObjects(@Path("className") String className);

  @GET("/1.1/classes/{className}")
  Observable<AVQueryResult> queryObjects(@Path("className") String className, @QueryMap Map<String, String> query);

  @GET("/1.1/cloudQuery")
  Observable<AVQueryResult> cloudQuery(@QueryMap Map<String, String> query);

  @GET("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> fetchObject(@Path("className") String className, @Path("objectId") String objectId);

  @GET("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> fetchObject(@Path("className") String className, @Path("objectId") String objectId,
                                   @Query("include") String includeKeys);

  @POST("/1.1/classes/{className}")
  Observable<AVObject> createObject(@Path("className") String className, @Body JsonObject object,
                                    @Query("fetchWhenSave") boolean fetchFlag,
                                    @Query("where") JsonObject where);

  @PUT("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> updateObject(@Path("className") String className, @Path("objectId") String objectId,
                                    @Body JsonObject object, @Query("fetchWhenSave") boolean fetchFlag,
                                    @Query("where") JsonObject where);

  @DELETE("/1.1/classes/{className}/{objectId}")
  Observable<AVNull> deleteObject(@Path("className") String className, @Path("objectId") String objectId);

  @POST("/1.1/batch")
  Observable<JsonArray> batchCreate(@Body JsonObject param);

  /**
   * AVInstalltion methods.
   */

  @POST("/1.1/{endpointClass}")
  Observable<AVObject> saveWholeObject(@Path("endpointClass") String endpointClass, @Body JsonObject object,
                                       @Query("fetchWhenSave") boolean fetchFlag,
                                       @Query("where") JsonObject where);
  @PUT("/1.1/{endpointClass}/{objectId}")
  Observable<AVObject> saveWholeObject(@Path("endpointClass") String endpointClass, @Path("objectId") String objectId,
                                       @Body JsonObject object, @Query("fetchWhenSave") boolean fetchFlag,
                                       @Query("where") JsonObject where);
  @GET("/1.1/{endpointClass}/{objectId}")
  Observable<AVObject> getWholeObject(@Path("endpointClass") String endpointClass, @Path("objectId") String objectId,
                                      @Query("include") String includeKeys);
  @DELETE("/1.1/{endpointClass}/{objectId}")
  Observable<AVNull> deleteWholeObject(@Path("endpointClass") String endpointClass, @Path("objectId") String objectId);

  /**
   * request format:
   *    requests: [unit, unit]
   * unit format:
   *    {"path":"/1.1/classes/{class}/{objectId}",
   *     "method":"PUT",
   *     "body":{"{field}":operationJson,
   *             "__internalId":"{objectId}",
   *             "__children":[]},
   *     "params":{}
   *    }
   * for update same field with multiple operations, we must use batchUpdate instead of batchSave,
   * otherwise, `__internalId` will become a common field of target instance.
   */
  @POST("/1.1/batch/save")
  Observable<JsonObject> batchUpdate(@Body JsonObject param);

  /**
   * Cloud Functions
   */
  @POST("/1.1/functions/{name}")
  Observable<Map<String, Object>> cloudFunction(@Path("name") String functionName, @Body Map<String, Object> param);

  @POST("/1.1/call/{name}")
  Observable<Map<String, Object>> cloudRPC(@Path("name") String functionName, @Body Object param);

  /**
   * File Operations.
   */

  @POST("/1.1/fileTokens")
  Observable<FileUploadToken> createUploadToken(@Body JsonObject fileData);

  @POST("/1.1/fileCallback")
  Call<AVNull> fileCallback(@Body JsonObject result);

  @GET("/1.1/files/{objectId}")
  Observable<AVFile> fetchFile(@Path("objectId") String objectId);

  @GET("/1.1/date")
  Observable<AVDate> currentTimeMillis();

  /**
   * Role Operations.
   */
  @POST("/1.1/roles")
  Observable<AVRole> createRole(@Body JsonObject object);

  /**
   * User Operations.
   */

  @POST("/1.1/users")
  Observable<AVUser> signup(@Body JsonObject object);
  @POST("/1.1/users")
  Observable<AVUser> signup(@Body JsonObject object, @Query("failOnNotExist") boolean failOnNotExist);
  @GET("/1.1/users")
  Observable<AVQueryResult> queryUsers(@QueryMap Map<String, String> query);

  @POST("/1.1/usersByMobilePhone")
  Observable<AVUser> signupByMobilePhone(@Body JsonObject object);

  @POST("/1.1/login")
  Observable<AVUser> login(@Body JsonObject object);

  @PUT("/1.1/users/{objectId}/updatePassword")
  Observable<AVUser> updatePassword(@Path("objectId") String objectId, @Body JsonObject object);

  @PUT("/1.1/resetPasswordBySmsCode/{smsCode}")
  Observable<AVNull> resetPasswordBySmsCode(@Path("smsCode") String smsCode, @Body Map<String, String> param);

  @GET("/1.1/users/me")
  Observable<AVUser> checkAuthenticated(@QueryMap Map<String, String> query);

  @PUT("/1.1/users/{objectId}/refreshSessionToken")
  Observable<AVUser> refreshSessionToken(@Path("objectId") String objectId);

  @POST("/1.1/requestPasswordReset")
  Observable<AVNull> requestResetPassword(@Body Map<String, String> param);

  @POST("/1.1/requestPasswordResetBySmsCode")
  Observable<AVNull> requestResetPasswordBySmsCode(@Body Map<String, String> param);

  @POST("/1.1/requestEmailVerify")
  Observable<AVNull> requestEmailVerify(@Body Map<String, String> param);

  @POST("/1.1/requestMobilePhoneVerify")
  Observable<AVNull> requestMobilePhoneVerify(@Body Map<String, String> param);

  @POST("/1.1/requestLoginSmsCode")
  Observable<AVNull> requestLoginSmsCode(@Body Map<String, String> param);

  @POST("/1.1/verifyMobilePhone/{verifyCode}")
  Observable<AVNull> verifyMobilePhone(@Path("verifyCode") String verifyCode);

  @POST("/1.1/users/{followee}/friendship/{follower}")
  Observable<JsonObject> followUser(@Path("followee") String followee, @Path("follower") String follower,
                                    @Body Map<String, Object> param);

  @DELETE("/1.1/users/{followee}/friendship/{follower}")
  Observable<JsonObject> unfollowUser(@Path("followee") String followee, @Path("follower") String follower);

  @GET("/1.1/users/{userId}/followers")
  Observable<JsonObject> getFollowers(@Path("userId") String userId);

  @GET("/1.1/users/{userId}/followees")
  Observable<JsonObject> getFollowees(@Path("userId") String userId);

  @GET("/1.1/users/{userId}/followersAndFollowees")
  Observable<JsonObject> getFollowersAndFollowees(@Path("userId") String userId);

  /**
   * Status API
   */
  @POST("/1.1/statuses")
  Observable<AVStatus> postStatus(@Body Map<String, Object> param);

  @GET("/1.1/statuses/{statusId}")
  Observable<AVStatus> fetchSingleStatus(@Path("statusId") String statusId);

  @GET("/1.1/statuses")
  Observable<AVQueryResult> fetchStatuses(@QueryMap Map<String, String> query);

  @DELETE("/1.1/statuses/{statusId}")
  Observable<AVNull> deleteStatus(@Path("statusId") String statusId);

  @DELETE("/1.1/subscribe/statuses/inbox")
  Observable<AVNull> deleteInboxStatus(@QueryMap Map<String, Object> query);

  @GET("/1.1/subscribe/statuses")
  Observable<AVQueryResult> queryInbox(@QueryMap Map<String, String> query);

  @GET("/1.1/subscribe/statuses/count")
  Observable<JsonObject> getInboxCount(@QueryMap Map<String, String> query);

  @POST("/1.1/subscribe/statuses/resetUnreadCount")
  Observable<AVNull> resetInboxUnreadCount();


  /**
   * SMS / Capture requests
   */
  @GET("/1.1/requestCaptcha")
  Observable<AVCaptchaDigest> requestCaptcha(@QueryMap Map<String, String> query);

  @POST("/1.1/verifyCaptcha")
  Observable<AVCaptchaValidateResult> verifyCaptcha(@Body Map<String, String> param);

  @POST("/1.1/requestSmsCode")
  Observable<AVNull> requestSMSCode(@Body Map<String, Object> param);

  @POST("/1.1/verifySmsCode/{code}")
  Observable<AVNull> verifySMSCode(@Path("code") String code, @Body Map<String, Object> param);

  /**
   * FullText Search API
   */
  @GET("/1.1/search/select")
  Observable<AVSearchResponse> search(@QueryMap Map<String, String> query);
}
