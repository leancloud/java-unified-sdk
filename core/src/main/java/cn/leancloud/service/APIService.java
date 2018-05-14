package cn.leancloud.service;

import cn.leancloud.*;
import cn.leancloud.query.AVQueryResult;
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

import com.alibaba.fastjson.*;

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
  Observable<AVObject> createObject(@Path("className") String className, @Body JSONObject object,
                                    @Query("fetchWhenSave") boolean fetchFlag,
                                    @Query("where") JSONObject where);

  @PUT("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> updateObject(@Path("className") String className, @Path("objectId") String objectId,
                                    @Body JSONObject object, @Query("fetchWhenSave") boolean fetchFlag,
                                    @Query("where") JSONObject where);

  @DELETE("/1.1/classes/{className}/{objectId}")
  Observable<AVNull> deleteObject(@Path("className") String className, @Path("objectId") String objectId);

  @POST("/1.1/batch")
  Observable<JSONArray> batchCreate(@Body JSONObject param);

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
  Observable<JSONObject> batchUpdate(@Body JSONObject param);

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
  Observable<FileUploadToken> createUploadToken(@Body JSONObject fileData);

  @POST("/1.1/fileCallback")
  Call<AVNull> fileCallback(@Body JSONObject result);

  @GET("/1.1/files/{objectId}")
  Observable<AVFile> fetchFile(@Path("objectId") String objectId);

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

  @POST("/1.1/usersByMobilePhone")
  Observable<AVUser> signupByMobilePhone(@Body JSONObject object);

  @POST("/1.1/login")
  Observable<AVUser> login(@Body JSONObject object);

  @PUT("/1.1/users/{objectId}/updatePassword")
  Observable<AVUser> updatePassword(@Path("objectId") String objectId, @Body JSONObject object);

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
  Observable<JSONObject> followUser(@Path("followee") String followee, @Path("follower") String follower,
                                    @Body Map<String, Object> param);

  @DELETE("/1.1/users/{followee}/friendship/{follower}")
  Observable<JSONObject> unfollowUser(@Path("followee") String followee, @Path("follower") String follower);

  /**
   * Status API
   */
  @POST("/1.1/statuses")
  Observable<AVStatus> postStatus();

  @GET("/1.1/statuses/{statusId}")
  Observable<AVStatus> fetchStatus(@Path("statusId") String statusId);

  @DELETE("/1.1/statuses/{statusId}")
  Observable<AVNull> deleteStatus(@Path("statusId") String statusId);

  @DELETE("/1.1/subscribe/statuses/inbox")
  Observable<AVNull> deleteInboxStatus(@QueryMap Map<String, String> query);

  @GET("/1.1/subscribe/statuses")
  Observable<List<AVStatus>> queryStatuses();

  @GET("/1.1/subscribe/statuses/count")
  Observable<Integer> getInboxUnreadCount();

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
}
