package cn.leancloud.core.service;

import cn.leancloud.core.AVObject;
import cn.leancloud.core.types.AVDate;
import cn.leancloud.internal.FileUploadToken;
import io.reactivex.Observable;
import retrofit2.http.*;

import java.util.List;

import com.alibaba.fastjson.*;

public interface APIService {
  @GET("/1.1/classes/{className}")
  Observable<List<AVObject>> findObjects(@Path("className") String className);

  @GET("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> fetchObject(@Path("className") String className, @Path("objectId") String objectId);

  @POST("/1.1/classes/{className}")
  Observable<AVObject> createObject(@Path("className") String className, @Body JSONObject object);

  @PUT("/1.1/classes/{className}/{objectId}")
  Observable<AVObject> updateObject(@Path("className") String className, @Path("objectId") String objectId, @Body JSONObject object);

  @DELETE("/1.1/classes/{className}/{objectId}")
  Observable<Boolean> deleteObject(@Path("className") String className, @Path("objectId") String objectId);

  @POST("/1.1/filetokens")
  Observable<FileUploadToken> createUploadToken();

  @GET("/1.1/date")
  Observable<AVDate> currentTimeMillis();
}
