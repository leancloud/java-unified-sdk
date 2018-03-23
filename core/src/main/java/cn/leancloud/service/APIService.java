package cn.leancloud.service;

import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.AVRole;
import cn.leancloud.AVUser;
import cn.leancloud.types.AVDate;
import cn.leancloud.types.AVNull;
import cn.leancloud.upload.FileUploadToken;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

import com.alibaba.fastjson.*;

public interface APIService {
  /**
   * Object Operations.
   */

  @GET("/1.1/classes/{className}")
  Observable<List<? extends AVObject>> findObjects(@Path("className") String className);

  @GET("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> fetchObject(@Path("className") String className, @Path("objectId") String objectId);

  @POST("/1.1/classes/{className}?fetchWhenSave=true")
  Observable<AVObject> createObject(@Path("className") String className, @Body JSONObject object);

  @PUT("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> updateObject(@Path("className") String className, @Path("objectId") String objectId,
                                    @Body JSONObject object);

  @DELETE("/1.1/classes/{className}/{objectId}")
  Observable<AVNull> deleteObject(@Path("className") String className, @Path("objectId") String objectId);

  @POST("/1.1/batch")
  Observable<AVNull> batchSave(@Body JSONObject object);

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
  Observable<JSONObject> login(@Body JSONObject object);

  @PUT("/1.1/users/{objectId}/updatePassword")
  Observable<AVUser> updatePassword(@Path("objectId") String objectId, @Body JSONObject object);
}
