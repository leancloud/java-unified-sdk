package cn.leancloud.service;

import cn.leancloud.*;
import cn.leancloud.json.JSONArray;
import cn.leancloud.json.JSONObject;
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
  String HEADER_KEY_LC_SESSIONTOKEN = "X-LC-Session";

  /**
   * Object Operations.
   */

  @GET("/1.1/classes/{className}")
  Observable<List<? extends AVObject>> findObjects(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                                   @Path("className") String className);

  @GET("/1.1/classes/{className}")
  Observable<AVQueryResult> queryObjects(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                         @Path("className") String className, @QueryMap Map<String, String> query);

  @GET("/1.1/cloudQuery")
  Observable<AVQueryResult> cloudQuery(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                       @QueryMap Map<String, String> query);

  @GET("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> fetchObject(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                   @Path("className") String className, @Path("objectId") String objectId);

  @GET("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> fetchObject(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                   @Path("className") String className, @Path("objectId") String objectId,
                                   @Query("include") String includeKeys);

  @POST("/1.1/classes/{className}")
  Observable<AVObject> createObject(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                    @Path("className") String className, @Body JSONObject object,
                                    @Query("fetchWhenSave") boolean fetchFlag,
                                    @Query("where") JSONObject where);

  @PUT("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> updateObject(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                    @Path("className") String className, @Path("objectId") String objectId,
                                    @Body JSONObject object, @Query("fetchWhenSave") boolean fetchFlag,
                                    @Query("where") JSONObject where);

//  @DELETEWITHBODY("/1.1/classes/{className}/{objectId}")
  @HTTP(method = "DELETE", path = "/1.1/classes/{className}/{objectId}", hasBody = true)
  Observable<AVNull> deleteObject(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                  @Path("className") String className, @Path("objectId") String objectId,
                                  @Body Map<String, Object> param);

  @POST("/1.1/batch")
  Observable<List<Map<String, Object>>> batchCreate(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                                    @Body JSONObject param);

  /**
   * AVInstalltion methods.
   */

  @POST("/1.1/{endpointClass}")
  Observable<AVObject> saveWholeObject(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                       @Path("endpointClass") String endpointClass, @Body JSONObject object,
                                       @Query("fetchWhenSave") boolean fetchFlag,
                                       @Query("where") JSONObject where);
  @PUT("/1.1/{endpointClass}/{objectId}")
  Observable<AVObject> saveWholeObject(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                       @Path("endpointClass") String endpointClass, @Path("objectId") String objectId,
                                       @Body JSONObject object, @Query("fetchWhenSave") boolean fetchFlag,
                                       @Query("where") JSONObject where);
  @GET("/1.1/{endpointClass}/{objectId}")
  Observable<AVObject> getWholeObject(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                      @Path("endpointClass") String endpointClass, @Path("objectId") String objectId,
                                      @Query("include") String includeKeys);
//  @DELETE("/1.1/{endpointClass}/{objectId}")
  @HTTP(method = "DELETE", path = "/1.1/{endpointClass}/{objectId}", hasBody = true)
  Observable<AVNull> deleteWholeObject(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                       @Path("endpointClass") String endpointClass, @Path("objectId") String objectId,
                                       @Body Map<String, Object> param);

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
  Observable<JSONObject> batchUpdate(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                     @Body JSONObject param);

  /**
   * Cloud Functions
   */
  @POST("/1.1/functions/{name}")
  Observable<Map<String, Object>> cloudFunction(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                                @Path("name") String functionName, @Body Map<String, Object> param);

  @POST("/1.1/call/{name}")
  Observable<Map<String, Object>> cloudRPC(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                           @Path("name") String functionName, @Body Object param);

  /**
   * File Operations.
   */

  @POST("/1.1/fileTokens")
  Observable<FileUploadToken> createUploadToken(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                                @Body JSONObject fileData);

  @POST("/1.1/fileCallback")
  Call<AVNull> fileCallback(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                            @Body JSONObject result);

  @GET("/1.1/files/{objectId}")
  Observable<AVFile> fetchFile(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                               @Path("objectId") String objectId);

  @GET("/1.1/date")
  Observable<AVDate> currentTimeMillis();

  /**
   * Role Operations.
   */
  @POST("/1.1/roles")
  Observable<AVRole> createRole(@Body JSONObject object);

  /**
   * User Operations.
   */

  @POST("/1.1/users")
  Observable<AVUser> signup(@Body JSONObject object);
  @POST("/1.1/users")
  Observable<AVUser> signup(@Body JSONObject object, @Query("failOnNotExist") boolean failOnNotExist);
  @GET("/1.1/users")
  Observable<AVQueryResult> queryUsers(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                       @QueryMap Map<String, String> query);

  @POST("/1.1/users/friendshipRequests")
  Observable<AVObject> applyFriendship(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                       @Body JSONObject param);

  @PUT("/1.1/users/friendshipRequests/{requestId}/accept")
  Observable<AVObject> acceptFriendshipRequest(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                               @Path("requestId") String requestId,
                                                          @Body JSONObject param);

  @PUT("/1.1/users/friendshipRequests/{requestId}/decline")
  Observable<AVObject> declineFriendshipRequest(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                                @Path("requestId") String requestId);

  @POST("/1.1/usersByMobilePhone")
  Observable<AVUser> signupByMobilePhone(@Body JSONObject object);

  @POST("/1.1/login")
  Observable<AVUser> login(@Body JSONObject object);

  @PUT("/1.1/users/{objectId}/updatePassword")
  Observable<AVUser> updatePassword(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                    @Path("objectId") String objectId, @Body JSONObject object);

  @GET("/1.1/users/me")
  Observable<AVUser> checkAuthenticated(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                        @QueryMap Map<String, String> query);

  @PUT("/1.1/users/{objectId}/refreshSessionToken")
  Observable<AVUser> refreshSessionToken(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                         @Path("objectId") String objectId);

  @POST("/1.1/requestPasswordReset")
  Observable<AVNull> requestResetPassword(@Body Map<String, String> param);

  @POST("/1.1/requestPasswordResetBySmsCode")
  Observable<AVNull> requestResetPasswordBySmsCode(@Body Map<String, String> param);

  @PUT("/1.1/resetPasswordBySmsCode/{smsCode}")
  Observable<AVNull> resetPasswordBySmsCode(@Path("smsCode") String smsCode, @Body Map<String, String> param);

  @POST("/1.1/requestEmailVerify")
  Observable<AVNull> requestEmailVerify(@Body Map<String, String> param);

  @POST("/1.1/requestMobilePhoneVerify")
  Observable<AVNull> requestMobilePhoneVerify(@Body Map<String, String> param);

  @POST("/1.1/verifyMobilePhone/{verifyCode}")
  Observable<AVNull> verifyMobilePhone(@Path("verifyCode") String verifyCode);

  @POST("/1.1/requestLoginSmsCode")
  Observable<AVNull> requestLoginSmsCode(@Body Map<String, String> param);

  @POST("/1.1/users/{followee}/friendship/{follower}")
  Observable<JSONObject> followUser(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                    @Path("followee") String followee, @Path("follower") String follower,
                                    @Body Map<String, Object> param);

  @PUT("/1.1/users/{followee}/friendship/{friendId}")
  Observable<AVFriendship> updateFriendship(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                            @Path("followee") String followee, @Path("friendId") String friendId,
                                            @Body Map<String, Object> param);

  @DELETE("/1.1/users/{followee}/friendship/{follower}")
  Observable<JSONObject> unfollowUser(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                      @Path("followee") String followee, @Path("follower") String follower);

  @GET("/1.1/users/{userId}/followers")
  Observable<JSONObject> getFollowers(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                      @Path("userId") String userId);

  @GET("/1.1/users/{userId}/followees")
  Observable<JSONObject> getFollowees(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                      @Path("userId") String userId);

  @GET("/1.1/users/{userId}/followersAndFollowees")
  Observable<JSONObject> getFollowersAndFollowees(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                                  @Path("userId") String userId);

  /**
   * Status API
   */
  @POST("/1.1/statuses")
  Observable<AVStatus> postStatus(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                  @Body Map<String, Object> param);

  @GET("/1.1/statuses/{statusId}")
  Observable<AVStatus> fetchSingleStatus(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                         @Path("statusId") String statusId);

  @GET("/1.1/statuses")
  Observable<AVQueryResult> fetchStatuses(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                          @QueryMap Map<String, String> query);

  @DELETE("/1.1/statuses/{statusId}")
  Observable<AVNull> deleteStatus(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                  @Path("statusId") String statusId);

  @DELETE("/1.1/subscribe/statuses/inbox")
  Observable<AVNull> deleteInboxStatus(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                       @QueryMap Map<String, Object> query);

  @GET("/1.1/subscribe/statuses")
  Observable<AVQueryResult> queryInbox(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                       @QueryMap Map<String, String> query);

  @GET("/1.1/subscribe/statuses/count")
  Observable<JSONObject> getInboxCount(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                       @QueryMap Map<String, String> query);

  @POST("/1.1/subscribe/statuses/resetUnreadCount")
  Observable<AVNull> resetInboxUnreadCount(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken);


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

  @POST("/1.1/requestChangePhoneNumber")
  Observable<AVNull> requestSMSCodeForUpdatingPhoneNumber(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                                          @Body Map<String, Object> param);

  @POST("/1.1/changePhoneNumber")
  Observable<AVNull> verifySMSCodeForUpdatingPhoneNumber(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                                         @Body Map<String, Object> param);

  /**
   * FullText Search API
   */
  @GET("/1.1/search/select")
  Observable<AVSearchResponse> search(@Header(HEADER_KEY_LC_SESSIONTOKEN) String sessionToken,
                                      @QueryMap Map<String, String> query);
}
