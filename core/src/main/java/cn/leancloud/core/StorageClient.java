package cn.leancloud.core;

import cn.leancloud.*;
import cn.leancloud.cache.QueryResultCache;
import cn.leancloud.ops.Utils;
import cn.leancloud.query.AVQueryResult;
import cn.leancloud.service.APIService;
import cn.leancloud.sms.AVCaptchaDigest;
import cn.leancloud.sms.AVCaptchaOption;
import cn.leancloud.sms.AVCaptchaValidateResult;
import cn.leancloud.types.AVDate;
import cn.leancloud.types.AVNull;
import cn.leancloud.upload.FileUploadToken;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.util.*;

public class StorageClient {
  private static AVLogger LOGGER = LogUtil.getLogger(StorageClient.class);

  private APIService apiService = null;
  private boolean asynchronized = false;
  private PaasClient.SchedulerCreator defaultCreator = null;
  private QueryResultCache queryResultCache = QueryResultCache.getInstance();
  private AVUser currentUser = null;

  public StorageClient(APIService apiService, boolean asyncRequest, PaasClient.SchedulerCreator observerSchedulerCreator) {
    this.apiService = apiService;
    this.asynchronized = asyncRequest;
    this.defaultCreator = observerSchedulerCreator;
  }

  public void setCurrentUser(AVUser newUser) {
    this.currentUser = newUser;
  }

  public AVUser getCurrentUser() {
    return this.currentUser;
  }

  // TODO: need to change observable thread in case of AVFile.saveInBackground.
  private Observable wrappObservable(Observable observable) {
    if (null == observable) {
      return null;
    }
    if (asynchronized) {
      observable = observable.subscribeOn(Schedulers.io());
    }
    if (null != defaultCreator) {
      observable = observable.observeOn(defaultCreator.create());
    }
    return observable;
  }

  private Observable wrappObservableInBackground(Observable observable) {
    if (null == observable) {
      return null;
    }
    Scheduler scheduler = Schedulers.io();
    if (asynchronized) {
      observable = observable.subscribeOn(scheduler);
    }
    if (null != defaultCreator) {
      observable = observable.observeOn(scheduler);
    }
    return observable;
  }

  public Observable<AVDate> getServerTime() {
    Observable<AVDate> date = wrappObservable(apiService.currentTimeMillis());
    return date;
  }

  public Observable<? extends AVObject> fetchObject(final String className, String objectId, String includeKeys) {
    Observable<AVObject> object = null;
    if (StringUtil.isEmpty(includeKeys)) {
      object = wrappObservable(apiService.fetchObject(className, objectId));
    } else {
      object = wrappObservable(apiService.fetchObject(className, objectId, includeKeys));
    }
    return object.map(new Function<AVObject, AVObject>() {
              public AVObject apply(AVObject avObject) throws Exception {
                return Transformer.transform(avObject, className);
              }
            });
  }

  public Observable<List<AVObject>> queryObjects(final String className, final Map<String, String> query,
                                                 AVQuery.CachePolicy cachePolicy, final long maxAgeInMilliseconds) {
    final String cacheKey = QueryResultCache.generateKeyForQueryCondition(className, query);
    switch (cachePolicy) {
      case CACHE_ONLY:
        return wrappObservable(QueryResultCache.getInstance().getCacheResult(className, query, maxAgeInMilliseconds));
      case CACHE_ELSE_NETWORK:
        return wrappObservable(QueryResultCache.getInstance().getCacheResult(className, query, maxAgeInMilliseconds))
                .onErrorReturn(new Function<Throwable, List<AVObject>>() {
                  public List<AVObject> apply(Throwable o) throws Exception {
                    LOGGER.d("failed to query local cache, cause: " + o.getMessage() + ", try to query networking");
                    return apiService.queryObjects(className, query)
                            .map(new Function<AVQueryResult, List<AVObject>>() {
                              public List<AVObject> apply(AVQueryResult o) throws Exception {
                                o.setClassName(className);
                                for (AVObject obj: o.getResults()) {
                                  obj.setClassName(className);
                                }
                                QueryResultCache.getInstance().cacheResult(cacheKey, o.toJSONString());
                                LOGGER.d("invoke within StorageClient.queryObjects(). resultSize:" + ((null != o.getResults())? o.getResults().size(): 0));
                                return o.getResults();
                              }
                            }).blockingFirst();
                  }
                });
      case NETWORK_ELSE_CACHE:
        return wrappObservable(apiService.queryObjects(className, query))
                .map(new Function<AVQueryResult, List<AVObject>>() {
                  public List<AVObject> apply(AVQueryResult o) throws Exception {
                    o.setClassName(className);
                    for (AVObject obj: o.getResults()) {
                      obj.setClassName(className);
                    }
                    QueryResultCache.getInstance().cacheResult(cacheKey, o.toJSONString());
                    LOGGER.d("invoke within StorageClient.queryObjects(). resultSize:" + ((null != o.getResults())? o.getResults().size(): 0));
                    return o.getResults();
                  }
                })
                .onErrorReturn(new Function<Throwable, List<AVObject>>() {
                  public List<AVObject> apply(Throwable o) throws Exception {
                    LOGGER.d("failed to query networking, cause: " + o.getMessage() + ", try to query local cache.");
                    return QueryResultCache.getInstance().getCacheResult(className, query, maxAgeInMilliseconds).blockingFirst();
                  }
                });
      case IGNORE_CACHE:
      default:
        return wrappObservable(apiService.queryObjects(className, query)).map(new Function<AVQueryResult, List<AVObject>>() {
          public List<AVObject> apply(AVQueryResult o) throws Exception {
            o.setClassName(className);
            for (AVObject obj: o.getResults()) {
              obj.setClassName(className);
            }
            QueryResultCache.getInstance().cacheResult(cacheKey, o.toJSONString());
            LOGGER.d("invoke within StorageClient.queryObjects(). resultSize:" + ((null != o.getResults())? o.getResults().size(): 0));
            return o.getResults();
          }
        });
    }
  }

  public Observable<AVQueryResult> cloudQuery(Map<String, String> query) {
    return wrappObservable(apiService.cloudQuery(query));
  }

  public Observable<Integer> queryCount(final String className, Map<String, String> query) {
    return wrappObservable(apiService.queryObjects(className, query).map(new Function<AVQueryResult, Integer>() {
      public Integer apply(AVQueryResult o) throws Exception {
        LOGGER.d("invoke within StorageClient.queryCount(). result:" + o + ", return:" + o.getCount());
        return o.getCount();
      }
    }));
  }

  public Observable<AVNull> deleteObject(final String className, String objectId) {
    return wrappObservable(apiService.deleteObject(className, objectId));
  }

  public Observable<? extends AVObject> createObject(final String className, JSONObject data, boolean fetchFlag) {
    Observable<AVObject> object = wrappObservable(apiService.createObject(className, data, fetchFlag));
    return object.map(new Function<AVObject, AVObject>() {
      public AVObject apply(AVObject avObject) {
        LOGGER.d(avObject.toString());
        return Transformer.transform(avObject, className);
      }
    });
  }

  public Observable<? extends AVObject> saveObject(final String className, String objectId, JSONObject data, boolean fetchFlag) {
    Observable<AVObject> object = wrappObservable(apiService.updateObject(className, objectId, data, fetchFlag));
    return object.map(new Function<AVObject, AVObject>() {
      public AVObject apply(AVObject avObject) {
        LOGGER.d("saveObject finished. intermediaObj=" + avObject.toString() + ", convert to " + className);
        return Transformer.transform(avObject, className);
      }
    });
  }

  public Observable<AVFile> fetchFile(String objectId) {
    Observable<AVFile> object = wrappObservable(apiService.fetchFile(objectId));
    return object.map(new Function<AVFile, AVFile>() {
      public AVFile apply(AVFile avFile) throws Exception {
        avFile.setClassName(AVFile.CLASS_NAME);
        return avFile;
      }
    });
  }

  public Observable<FileUploadToken> newUploadToken(JSONObject fileData) {
    return wrappObservableInBackground(apiService.createUploadToken(fileData));
  }

  public void fileCallback(JSONObject result) throws IOException {
    apiService.fileCallback(result).execute();
    return;
  }

  public Observable<JSONArray> batchSave(JSONObject parameter) {
    // resposne is:
    // [{"success":{"updatedAt":"2018-03-30T06:21:08.052Z","objectId":"5abd026d9f54540038791715"}},
    //  {"success":{"updatedAt":"2018-03-30T06:21:08.092Z","objectId":"5abd026d9f54540038791715"}},
    //  {"success":{"updatedAt":"2018-03-30T06:21:08.106Z","objectId":"5abd026d9f54540038791715"}}]
    Observable<JSONArray> result = wrappObservable(apiService.batchCreate(parameter));
    return result;
  }

  public Observable<JSONObject> batchUpdate(JSONObject parameter) {
    // response is:
    // {"5abd026d9f54540038791715":{"updatedAt":"2018-03-30T06:21:46.084Z","objectId":"5abd026d9f54540038791715"}}
    Observable<JSONObject> result = wrappObservable(apiService.batchUpdate(parameter));
    return result;
  }

  public Observable<AVUser> signUp(JSONObject data) {
    return wrappObservable(apiService.signup(data));
  }

  public <T extends AVUser> Observable<T> logIn(JSONObject data, final Class<T> clazz) {
    Observable<AVUser> object = wrappObservable(apiService.login(data));
    return object.map(new Function<AVUser, T>() {
      public T apply(AVUser avUser) throws Exception {
        return Transformer.transform(avUser, clazz);
      }
    });
  }

  public Observable<Boolean> checkAuthenticated(String sessionToken) {
    Map<String, String> param = new HashMap<String, String>(1);
    param.put("session_token", sessionToken);
    return wrappObservable(apiService.checkAuthenticated(param).map(new Function<AVUser, Boolean>() {
      public Boolean apply(AVUser o) throws Exception {
        if (null != o) {
          return true;
        } else {
          return false;
        }
      }
    }));
  }

  public <T extends AVUser> Observable<T> createUserBySession(String sessionToken, final Class<T> clazz) {
    Map<String, String> param = new HashMap<String, String>(1);
    param.put("session_token", sessionToken);
    return apiService.checkAuthenticated(param).map(new Function<AVUser, T>() {
      public T apply(AVUser avUser) throws Exception {
        if (null == avUser) {
          return null;
        }
        return Transformer.transform(avUser, clazz);
      }
    });
  }

  public Observable<Boolean> refreshSessionToken(final AVUser user) {
    return wrappObservable(apiService.refreshSessionToken(user.getObjectId()).map(new Function<AVUser, Boolean>() {
      public Boolean apply(AVUser avUser) throws Exception {
        if (null != avUser && !StringUtil.isEmpty(avUser.getSessionToken())) {
          user.internalChangeSessionToken(avUser.getSessionToken());
          return true;
        }
        return false;
      }
    }));
  }

  public Observable<AVNull> requestResetPassword(String email) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("email", email);
    return wrappObservable(apiService.requestResetPassword(map));
  }

  public Observable<AVNull> requestResetPasswordBySmsCode(String phoneNumber, String validateToken) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("mobilePhoneNumber", phoneNumber);
    if (!StringUtil.isEmpty(validateToken)) {
      map.put("validate_token", validateToken);
    }
    return wrappObservable(apiService.requestResetPasswordBySmsCode(map));
  }

  public Observable<AVNull> requestEmailVerify(String email) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("email", email);
    return wrappObservable(apiService.requestEmailVerify(map));
  }

  public Observable<AVNull> requestMobilePhoneVerify(String mobilePhone, String validateToken) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("mobilePhoneNumber", mobilePhone);
    if (!StringUtil.isEmpty(validateToken)) {
      map.put("validate_token", validateToken);
    }
    return wrappObservable(apiService.requestMobilePhoneVerify(map));
  }

  public Observable<AVNull> verifyMobilePhone(String verifyCode) {
    return wrappObservable(apiService.verifyMobilePhone(verifyCode));
  }

  public Observable<AVNull> requestLoginSmsCode(String phoneNumber, String validateToken) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("mobilePhoneNumber", phoneNumber);
    if (!StringUtil.isEmpty(validateToken)) {
      map.put("validate_token", validateToken);
    }
    return wrappObservable(apiService.requestLoginSmsCode(map));
  }

  public Observable<AVNull> resetPasswordBySmsCode(String smsCode, String newPass) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("password", newPass);
    return wrappObservable(apiService.resetPasswordBySmsCode(smsCode, map));
  }

  public Observable<AVNull> updatePassword(final AVUser user, String oldPass, String newPass) {
    if (null == user) {
      return Observable.error(new IllegalArgumentException("user is null"));
    }
    if (StringUtil.isEmpty(oldPass) || StringUtil.isEmpty(newPass)) {
      return Observable.error(new IllegalArgumentException("old password or new password is empty"));
    }
    JSONObject param = new JSONObject();
    param.put("old_password", oldPass);
    param.put("new_password", newPass);
    return wrappObservable(apiService.updatePassword(user.getObjectId(), param).map(new Function<AVUser, AVNull>() {
      public AVNull apply(AVUser var1) throws Exception {
        if (null != var1) {
          user.internalChangeSessionToken(var1.getSessionToken());
        }
        return new AVNull();
      }
    }));
  }

  public Observable<JSONObject> followUser(String followee, String follower, Map<String, Object> attr) {
    return wrappObservable(apiService.followUser(followee, follower, attr));
  }

  public Observable<JSONObject> unfollowUser(String followee, String follower) {
    return wrappObservable(apiService.unfollowUser(followee, follower));
  }

  public <T> Observable<T> callRPC(String name, Object param) {
    return wrappObservable(apiService.cloudRPC(name, param))
            .map(new Function<Map<String, ?>, T>() {
              public T apply(Map<String, ?> resultMap) throws Exception {
                try {
                  Object resultValue = resultMap.get("result");
                  if (resultValue instanceof Collection) {
                    return (T) Utils.getObjectFrom((Collection) resultValue);
                  } else if (resultValue instanceof Map) {
                    return (T) Utils.getObjectFrom((Map) resultValue);
                  } else {
                    return (T) resultValue;
                  }
                } catch (Exception ex) {
                  return null;
                }
              }
            });
  }

  public <T> Observable<T> callFunction(String name, Map<String, Object> params) {
    return wrappObservable(apiService.cloudFunction(name, params))
            .map(new Function<Map<String, ?>, T>() {
              public T apply(Map<String, ?> resultMap) throws Exception {
                try {
                  Object resultValue = resultMap.get("result");
                  if (resultValue instanceof Collection) {
                    return (T) Utils.getObjectFrom((Collection) resultValue);
                  } else if (resultValue instanceof Map) {
                    return (T) Utils.getObjectFrom((Map) resultValue);
                  } else {
                    return (T) resultValue;
                  }
                } catch (Exception ex) {
                  return null;
                }
              }
            });
  }

  public Observable<AVCaptchaDigest> requestCaptcha(AVCaptchaOption option) {
    return wrappObservable(apiService.requestCaptcha(option.getRequestParam()));
  }

  public Observable<AVCaptchaValidateResult> verifyCaptcha(String code, String token) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(token)) {
      return Observable.error(new IllegalArgumentException("code or token is empty"));
    }
    Map<String, String> param = new HashMap<String, String>(2);
    param.put("captcha_code", code);
    param.put("captcha_token", token);
    return wrappObservable(apiService.verifyCaptcha(param));
  }

  public Observable<AVNull> requestSMSCode(String mobilePhone, Map<String, Object> param) {
    param.put("mobilePhoneNumber", mobilePhone);
    return wrappObservable(apiService.requestSMSCode(param));
  }

  public Observable<AVNull> verifySMSCode(String code, String mobilePhone) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("code or mobilePhone is empty"));
    }
    Map<String, Object> param = new HashMap<String, Object>(1);
    param.put("mobilePhoneNumber", mobilePhone);
    return wrappObservable(apiService.verifySMSCode(code, param));
  }
}
