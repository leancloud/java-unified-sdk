package cn.leancloud.core;

import cn.leancloud.*;
import cn.leancloud.cache.QueryResultCache;
import cn.leancloud.gson.NumberDeserializerDoubleAsIntFix;
import cn.leancloud.ops.Utils;
import cn.leancloud.query.AVQueryResult;
import cn.leancloud.search.AVSearchResponse;
import cn.leancloud.service.APIService;
import cn.leancloud.sms.AVCaptchaDigest;
import cn.leancloud.sms.AVCaptchaOption;
import cn.leancloud.sms.AVCaptchaValidateResult;
import cn.leancloud.types.AVDate;
import cn.leancloud.types.AVNull;
import cn.leancloud.upload.FileUploadToken;
import cn.leancloud.utils.ErrorUtils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.util.*;

public class StorageClient {
  private static AVLogger LOGGER = LogUtil.getLogger(StorageClient.class);

  private APIService apiService = null;
  private boolean asynchronized = false;
  private AppConfiguration.SchedulerCreator defaultCreator = null;
  private QueryResultCache queryResultCache = QueryResultCache.getInstance();
  private AVUser currentUser = null;

  public StorageClient(APIService apiService, boolean asyncRequest, AppConfiguration.SchedulerCreator observerSchedulerCreator) {
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

  public Observable wrapObservable(Observable observable) {
    if (null == observable) {
      return null;
    }
    if (asynchronized) {
      observable = observable.subscribeOn(Schedulers.io());
    }
    if (null != defaultCreator) {
      observable = observable.observeOn(defaultCreator.create());
    }
    observable = observable.onErrorResumeNext(new Function<Throwable, ObservableSource>() {
      @Override
      public ObservableSource apply(Throwable throwable) throws Exception {
        return Observable.error(ErrorUtils.propagateException(throwable));
      }
    });
    return observable;
  }

  private Observable wrapObservableInBackground(Observable observable) {
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
    Observable<AVDate> date = wrapObservable(apiService.currentTimeMillis());
    return date;
  }

  public Observable<? extends AVObject> fetchObject(final AVUser authenticatedUser,
                                                    final String className, String objectId, String includeKeys) {
    Observable<AVObject> object = null;
    String authenticatedSession = getSessionToken(authenticatedUser);
    if (StringUtil.isEmpty(includeKeys)) {
      object = wrapObservable(apiService.fetchObject(authenticatedSession, className, objectId));
    } else {
      object = wrapObservable(apiService.fetchObject(authenticatedSession, className, objectId, includeKeys));
    }
    if (null == object) {
      return object;
    }
    return object.map(new Function<AVObject, AVObject>() {
              public AVObject apply(AVObject avObject) throws Exception {
                return Transformer.transform(avObject, className);
              }
            });
  }

  public boolean hasCachedResult(String className, Map<String, String> query, long maxAgeInMilliseconds) {
    return QueryResultCache.getInstance().hasCachedResult(className, query, maxAgeInMilliseconds);
  }

  private String getSessionToken(AVUser avUser) {
    if (null == avUser) {
      if (AppConfiguration.isIncognitoMode() || null == AVUser.currentUser()) {
        return "";
      } else {
        return AVUser.currentUser().getSessionToken();
      }
    }
    return avUser.getSessionToken();
  }

  private Observable<AVQueryResult> queryRemoteServer(final AVUser authenticatedUser,
                                                      String className, final Map<String, String> query) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    if (AVUser.CLASS_NAME.equalsIgnoreCase(className)) {
      return wrapObservable(apiService.queryUsers(authenticatedSession, query));
    } else {
      return wrapObservable(apiService.queryObjects(authenticatedSession, className, query));
    }
  }

  public Observable<List<AVObject>> queryObjects(final AVUser authenticatedUser,
                                                 final String className, final Map<String, String> query,
                                                 AVQuery.CachePolicy cachePolicy, final long maxAgeInMilliseconds) {
    final String cacheKey = QueryResultCache.generateKeyForQueryCondition(className, query);
    Observable<List<AVObject>> result = null;
    Observable<AVQueryResult> queryResult = null;
    switch (cachePolicy) {
      case CACHE_ONLY:
        result = wrapObservable(
                QueryResultCache.getInstance().getCacheResult(className, query, maxAgeInMilliseconds, true));
        break;
      case CACHE_ELSE_NETWORK:
        result = wrapObservable(
                QueryResultCache.getInstance().getCacheResult(className, query, maxAgeInMilliseconds, false))
                .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends List<AVObject>>>() {
                  @Override
                  public ObservableSource<? extends List<AVObject>> apply(Throwable throwable) throws Exception {
                    LOGGER.d("failed to query local cache, cause: " + throwable.getMessage() + ", try to query networking");

                    return queryRemoteServer(authenticatedUser, className, query)
                            .map(new Function<AVQueryResult, List<AVObject>>() {
                              public List<AVObject> apply(AVQueryResult o) throws Exception {
                                o.setClassName(className);
                                for (AVObject obj: o.getResults()) {
                                  obj.setClassName(className);
                                }
                                QueryResultCache.getInstance().cacheResult(cacheKey, o.toJSONString());
                                LOGGER.d("invoke within StorageClient.queryObjects(). resultSize:"
                                        + ((null != o.getResults())? o.getResults().size(): 0));
                                return o.getResults();
                              }
                            });
                  }
                });
        break;
      case NETWORK_ELSE_CACHE:
        queryResult =  queryRemoteServer(authenticatedUser, className, query);
        if (null != queryResult) {
          result = queryResult.map(new Function<AVQueryResult, List<AVObject>>() {
            public List<AVObject> apply(AVQueryResult o) throws Exception {
              o.setClassName(className);
              for (AVObject obj : o.getResults()) {
                obj.setClassName(className);
              }
              QueryResultCache.getInstance().cacheResult(cacheKey, o.toJSONString());
              LOGGER.d("invoke within StorageClient.queryObjects(). resultSize:"
                      + ((null != o.getResults()) ? o.getResults().size() : 0));
              return o.getResults();
            }
          }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends List<AVObject>>>() {
            @Override
            public ObservableSource<? extends List<AVObject>> apply(Throwable throwable) throws Exception {
              LOGGER.d("failed to query networking, cause: " + throwable.getMessage()
                      + ", try to query local cache.");
              return QueryResultCache.getInstance().getCacheResult(className, query, maxAgeInMilliseconds, true);
            }
          });
        }
        break;
      case IGNORE_CACHE:
      default:
        queryResult = queryRemoteServer(authenticatedUser, className, query);
        if (null != queryResult) {
          result = queryResult.map(new Function<AVQueryResult, List<AVObject>>() {
            public List<AVObject> apply(AVQueryResult o) throws Exception {
              o.setClassName(className);
              for (AVObject obj: o.getResults()) {
                obj.setClassName(className);
              }
              QueryResultCache.getInstance().cacheResult(cacheKey, o.toJSONString());
              LOGGER.d("invoke within StorageClient.queryObjects(). resultSize:"
                      + ((null != o.getResults())? o.getResults().size(): 0));
              return o.getResults();
            }
          });
        }
        break;
    }
    return result;
  }

  public Observable<AVQueryResult> cloudQuery(final AVUser authenticatedUser, Map<String, String> query) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.cloudQuery(authenticatedSession, query));
  }

  public Observable<Integer> queryCount(final AVUser authenticatedUser,
                                        final String className, Map<String, String> query) {
    Observable<AVQueryResult> queryResult = this.queryRemoteServer(authenticatedUser, className, query);
    if (null == queryResult) {
      return null;
    }
    return queryResult.map(new Function<AVQueryResult, Integer>() {
      public Integer apply(AVQueryResult o) throws Exception {
        LOGGER.d("invoke within StorageClient.queryCount(). result:" + o + ", return:" + o.getCount());
        return o.getCount();
      }
    });
  }

  public Observable<AVNull> deleteObject(final AVUser authenticatedUser,
                                         final String className, String objectId, Map<String, Object> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.deleteObject(authenticatedSession, className, objectId, param));
  }

  public Observable<? extends AVObject> createObject(final AVUser authenticatedUser,
                                                     final String className, JSONObject data, boolean fetchFlag,
                                                     JSONObject where) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<AVObject> object = wrapObservable(apiService.createObject(authenticatedSession, className, data, fetchFlag,
            where));
    if (null == object) {
      return null;
    }
    return object.map(new Function<AVObject, AVObject>() {
      public AVObject apply(AVObject avObject) {
        LOGGER.d(avObject.toString());
        return Transformer.transform(avObject, className);
      }
    });
  }

  public Observable<? extends AVObject> saveObject(final AVUser authenticatedUser,
                                                   final String className, String objectId, JSONObject data,
                                                   boolean fetchFlag, JSONObject where) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<AVObject> object = wrapObservable(apiService.updateObject(authenticatedSession, className, objectId, data,
            fetchFlag, where));
    if (null == object) {
      return null;
    }
    return object.map(new Function<AVObject, AVObject>() {
      public AVObject apply(AVObject avObject) {
        LOGGER.d("saveObject finished. intermediaObj=" + avObject.toString() + ", convert to " + className);
        return Transformer.transform(avObject, className);
      }
    });
  }

  public <E extends AVObject> Observable<E> saveWholeObject(final AVUser authenticatedUser,
                                                            final Class<E> clazz, final String endpointClass,
                                                            String objectId,
                                                            JSONObject object, boolean fetchFlag, JSONObject where) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<AVObject> result = null;
    if (StringUtil.isEmpty(objectId)) {
      result = wrapObservable(apiService.saveWholeObject(authenticatedSession, endpointClass, object, fetchFlag, where));
    } else {
      result = wrapObservable(apiService.saveWholeObject(authenticatedSession, endpointClass, objectId, object,
              fetchFlag, where));
    }

    if (null == result) {
      return null;
    }
    return result.map(new Function<AVObject, E>() {
      @Override
      public E apply(AVObject avObject) throws Exception {
        return Transformer.transform(avObject, clazz);
      }
    });
  }

  public Observable<AVObject> getWholeObject(final AVUser authenticatedUser,
                                             final String endpointClass, String objectId, String includeKeys) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.getWholeObject(authenticatedSession, endpointClass, objectId, includeKeys));
  }

  public Observable<AVNull> deleteWholeObject(final AVUser authenticatedUser,
                                              final String endpointClass, String objectId, Map<String, Object> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.deleteWholeObject(authenticatedSession, endpointClass, objectId, param));
  }

  public Observable<AVFile> fetchFile(final AVUser authenticatedUser, String objectId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<AVFile> object = wrapObservable(apiService.fetchFile(authenticatedSession, objectId));
    if (null == object) {
      return null;
    }
    return object.map(new Function<AVFile, AVFile>() {
      public AVFile apply(AVFile avFile) throws Exception {
        avFile.setClassName(AVFile.CLASS_NAME);
        return avFile;
      }
    });
  }

  public Observable<FileUploadToken> newUploadToken(final AVUser authenticatedUser, JSONObject fileData) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservableInBackground(apiService.createUploadToken(authenticatedSession, fileData));
  }

  public void fileCallback(final AVUser authenticatedUser, JSONObject result) throws IOException {
    String authenticatedSession = getSessionToken(authenticatedUser);
    apiService.fileCallback(authenticatedSession, result).execute();
    return;
  }

  public Observable<List<Map<String, Object>>> batchSave(final AVUser authenticatedUser, JSONObject parameter) {
    // resposne is:
    // [{"success":{"updatedAt":"2018-03-30T06:21:08.052Z","objectId":"5abd026d9f54540038791715"}},
    //  {"success":{"updatedAt":"2018-03-30T06:21:08.092Z","objectId":"5abd026d9f54540038791715"}},
    //  {"success":{"updatedAt":"2018-03-30T06:21:08.106Z","objectId":"5abd026d9f54540038791715"}}]
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<List<Map<String, Object>>> result = wrapObservable(apiService.batchCreate(authenticatedSession,
            parameter));
    return result;
  }

  public Observable<JSONObject> batchUpdate(final AVUser authenticatedUser, JSONObject parameter) {
    // response is:
    // {"5abd026d9f54540038791715":{"updatedAt":"2018-03-30T06:21:46.084Z","objectId":"5abd026d9f54540038791715"}}
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<JSONObject> result = wrapObservable(apiService.batchUpdate(authenticatedSession, parameter));
    return result;
  }

  public Observable<AVUser> signUp(JSONObject data) {
    return wrapObservable(apiService.signup(data));
  }
  public Observable<AVUser> signUpWithFlag(JSONObject data, boolean failOnNotExist) {
    return wrapObservable(apiService.signup(data, failOnNotExist));
  }
  public <T extends AVUser> Observable<T> signUpOrLoginByMobilephone(final JSONObject data, final Class<T> clazz) {
    return wrapObservable(apiService.signupByMobilePhone(data)).map(new Function<AVUser, T>() {
      @Override
      public T apply(AVUser avUser) throws Exception {
        T rst = Transformer.transform(avUser, clazz);
        attachLoginInfo(data, rst);
        AVUser.changeCurrentUser(rst, true);
        return rst;
      }
    });
  }

  private <T extends AVUser> void attachLoginInfo(final JSONObject data, T rst) {
    if (null == data || null == rst) {
      return;
    }
    if (data.containsKey(AVUser.ATTR_EMAIL)) {
      rst.setEmail(data.getString(AVUser.ATTR_EMAIL));
    }
    if (data.containsKey(AVUser.ATTR_USERNAME)) {
      rst.setUsername(data.getString(AVUser.ATTR_USERNAME));
    }
    if (data.containsKey(AVUser.ATTR_MOBILEPHONE)) {
      rst.setMobilePhoneNumber(data.getString(AVUser.ATTR_MOBILEPHONE));
    }
  }
  public <T extends AVUser> Observable<T> logIn(final JSONObject data, final Class<T> clazz) {
    Observable<AVUser> object = wrapObservable(apiService.login(data));
    if (null == object) {
      return null;
    }
    return object.map(new Function<AVUser, T>() {
      public T apply(AVUser avUser) throws Exception {
        T rst = Transformer.transform(avUser, clazz);
        attachLoginInfo(data, rst);
        AVUser.changeCurrentUser(rst, true);
        return rst;
      }
    });
  }

  public Observable<AVFriendshipRequest> applyFriendshipRequest(final AVUser authenticatedUser, final JSONObject data) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<AVObject> result = wrapObservable(apiService.applyFriendship(authenticatedSession, data));
    if (null == result) {
      return null;
    }
    return result.map(new Function<AVObject, AVFriendshipRequest>() {
      @Override
      public AVFriendshipRequest apply(AVObject avObject) throws Exception {
        return Transformer.transform(avObject, AVFriendshipRequest.class);
      }
    });
  }

  public Observable<AVFriendshipRequest> acceptFriendshipRequest(final AVUser authenticatedUser,
                                                                 AVFriendshipRequest request, JSONObject param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<AVObject> result = wrapObservable(apiService.acceptFriendshipRequest(authenticatedSession,
            request.getObjectId(), param));
    if (null == result) {
      return null;
    }
    return result.map(new Function<AVObject, AVFriendshipRequest>() {
      @Override
      public AVFriendshipRequest apply(AVObject avObject) throws Exception {
        return Transformer.transform(avObject, AVFriendshipRequest.class);
      }
    });
  }
  public Observable<AVFriendshipRequest> declineFriendshipRequest(final AVUser authenticatedUser,
                                                                  AVFriendshipRequest request) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<AVObject> result = wrapObservable(apiService.declineFriendshipRequest(authenticatedSession, request.getObjectId()));
    return result.map(new Function<AVObject, AVFriendshipRequest>() {
      @Override
      public AVFriendshipRequest apply(AVObject avObject) throws Exception {
        return Transformer.transform(avObject, AVFriendshipRequest.class);
      }
    });
  }

  public Observable<Boolean> checkAuthenticated(String sessionToken) {
    Map<String, String> param = new HashMap<String, String>(1);
    param.put("session_token", sessionToken);
    Observable<AVUser> apiResult = wrapObservable(apiService.checkAuthenticated(sessionToken, param));
    if (null == apiResult) {
      return Observable.just(false);
    }
    return apiResult.map(new Function<AVUser, Boolean>() {
      public Boolean apply(AVUser o) throws Exception {
        if (null != o) {
          return true;
        } else {
          return false;
        }
      }
    });
  }

  public <T extends AVUser> Observable<T> createUserBySession(String sessionToken, final Class<T> clazz) {
    Map<String, String> param = new HashMap<String, String>(1);
    param.put("session_token", sessionToken);
    return apiService.checkAuthenticated(sessionToken, param).map(new Function<AVUser, T>() {
      public T apply(AVUser avUser) throws Exception {
        if (null == avUser) {
          LOGGER.e("The mapper function returned a null value.");
          return null;
        }
        return Transformer.transform(avUser, clazz);
      }
    });
  }

  public Observable<Boolean> refreshSessionToken(final AVUser user) {
    return wrapObservable(apiService.refreshSessionToken(user.getSessionToken(), user.getObjectId()).map(new Function<AVUser, Boolean>() {
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
    return wrapObservable(apiService.requestResetPassword(map));
  }

  public Observable<AVNull> requestResetPasswordBySmsCode(String phoneNumber, String validateToken) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("mobilePhoneNumber", phoneNumber);
    if (!StringUtil.isEmpty(validateToken)) {
      map.put("validate_token", validateToken);
    }
    return wrapObservable(apiService.requestResetPasswordBySmsCode(map));
  }

  public Observable<AVNull> requestEmailVerify(String email) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("email", email);
    return wrapObservable(apiService.requestEmailVerify(map));
  }

  public Observable<AVNull> requestMobilePhoneVerify(String mobilePhone, String validateToken) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("mobilePhoneNumber", mobilePhone);
    if (!StringUtil.isEmpty(validateToken)) {
      map.put("validate_token", validateToken);
    }
    return wrapObservable(apiService.requestMobilePhoneVerify(map));
  }

  public Observable<AVNull> verifyMobilePhone(String verifyCode) {
    return wrapObservable(apiService.verifyMobilePhone(verifyCode));
  }

  public Observable<AVNull> requestLoginSmsCode(String phoneNumber, String validateToken) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("mobilePhoneNumber", phoneNumber);
    if (!StringUtil.isEmpty(validateToken)) {
      map.put("validate_token", validateToken);
    }
    return wrapObservable(apiService.requestLoginSmsCode(map));
  }

  public Observable<AVNull> resetPasswordBySmsCode(String smsCode, String newPass) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("password", newPass);
    return wrapObservable(apiService.resetPasswordBySmsCode(smsCode, map));
  }

  public Observable<AVNull> updatePassword(final AVUser user, String oldPass, String newPass) {
    if (null == user) {
      return Observable.error(new IllegalArgumentException("user is null"));
    }
    if (StringUtil.isEmpty(oldPass) || StringUtil.isEmpty(newPass)) {
      return Observable.error(new IllegalArgumentException("old password or new password is empty"));
    }
    JSONObject param = JSONObject.Builder.create(null);
    param.put("old_password", oldPass);
    param.put("new_password", newPass);
    return wrapObservable(apiService.updatePassword(user.getSessionToken(), user.getObjectId(), param)
            .map(new Function<AVUser, AVNull>() {
      public AVNull apply(AVUser var1) throws Exception {
        if (null != var1) {
          user.internalChangeSessionToken(var1.getSessionToken());
        }
        return new AVNull();
      }
    }));
  }

  public Observable<JSONObject> followUser(final AVUser authenticatedUser,
                                           String followee, String follower, Map<String, Object> attr) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.followUser(authenticatedSession, followee, follower, attr));
  }

  public Observable<JSONObject> unfollowUser(final AVUser authenticatedUser,
                                             String followee, String follower) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.unfollowUser(authenticatedSession, followee, follower));
  }

  public Observable<AVFriendship> updateFriendship(final AVUser authenticatedUser,
                                                   String followeeUserid, String friendObjectId, Map<String, Object> attr) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.updateFriendship(authenticatedSession, followeeUserid, friendObjectId, attr));
  }

  public Observable<JSONObject> getFollowersAndFollowees(final AVUser authenticatedUser, String userId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.getFollowersAndFollowees(authenticatedSession, userId));
  }

  public Observable<AVStatus> postStatus(final AVUser authenticatedUser, Map<String, Object> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.postStatus(authenticatedSession, param));
  }

  public Observable<AVStatus> fetchStatus(final AVUser authenticatedUser, String objectId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.fetchSingleStatus(authenticatedSession, objectId));
  }

  public Observable<List<AVStatus>> queryStatus(final AVUser authenticatedUser, Map<String, String> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.fetchStatuses(authenticatedSession, param)
            .map(new Function<AVQueryResult, List<AVStatus>>() {
      @Override
      public List<AVStatus> apply(AVQueryResult o) throws Exception {
        if (null == o) {
          LOGGER.e("The mapper function returned a null value.");
          return null;
        }
        List<AVStatus> results = new ArrayList<>();
        for (AVObject obj: o.getResults()) {
          results.add(new AVStatus(obj));
        }
        return results;
      }
    }));
  }

  public Observable<List<AVStatus>> queryInbox(final AVUser authenticatedUser, Map<String, String> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.queryInbox(authenticatedSession, param)
            .map(new Function<AVQueryResult, List<AVStatus>>() {
      @Override
      public List<AVStatus> apply(AVQueryResult o) throws Exception {
        if (null == o) {
          LOGGER.e("The mapper function returned a null value.");
          return null;
        }
        List<AVStatus> results = new ArrayList<>();
        for (AVObject obj: o.getResults()) {
          results.add(new AVStatus(obj));
        }
        return results;
      }
    }));
  }

  public Observable<JSONObject> getInboxCount(final AVUser authenticatedUser, Map<String, String> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.getInboxCount(authenticatedSession, param));
  }

  public Observable<AVNull> deleteStatus(final AVUser authenticatedUser, String statusId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.deleteStatus(authenticatedSession, statusId));
  }

  public Observable<AVNull> deleteInboxStatus(final AVUser authenticatedUser, Map<String, Object> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.deleteInboxStatus(authenticatedSession, param));
  }

  public <T> Observable<T> callRPC(final AVUser authenticatedUser, String name, Object param) {
    return callRPC(authenticatedUser, name, param, false, null);
  }

  <T> Observable<T> callRPC(final AVUser authenticatedUser,
                            final String name, final Object param, final boolean enableCache, final String cacheKey) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<Map<String, ?>> cloudCall =  wrapObservable(apiService.cloudRPC(authenticatedSession, name, param));
    if (null == cloudCall) {
      return null;
    }
    return cloudCall.map(new Function<Map<String, ?>, T>() {
      public T apply(Map<String, ?> resultMap) throws Exception {
        try {
          Object resultValue = resultMap.get("result");
          if (enableCache && !StringUtil.isEmpty(cacheKey)) {
            LOGGER.d("cache rpc result:" + JSON.toJSONString(resultValue));
            QueryResultCache.getInstance().cacheResult(cacheKey, JSON.toJSONString(resultValue));
          }
          if (resultValue instanceof Collection) {
            return (T) Utils.getObjectFrom((Collection) resultValue);
          } else if (resultValue instanceof Map) {
            return (T) Utils.getObjectFrom((Map) resultValue);
          } else {
            return (T) resultValue;
          }
        } catch (Exception ex) {
          LOGGER.d("RPCFunction error: " + ex.getMessage());
          return null;
        }
      }
    });
  }

  public <T> Observable<T> callFunction(final AVUser authenticatedUser, String name, Map<String, Object> params) {
    return callFunction(authenticatedUser, name, params, false, null);
  }

  <T> Observable<T> callFunction(final AVUser authenticatedUser,
                                 String name, Map<String, Object> params, final boolean enableCache, final String cacheKey) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<Map<String, ?>> cloudCall = wrapObservable(apiService.cloudFunction(authenticatedSession, name, params));
    if (null == cloudCall) {
      return null;
    }
    return cloudCall.map(new Function<Map<String, ?>, T>() {
      public T apply(Map<String, ?> resultMap) throws Exception {
        try {
          Object resultValue = resultMap.get("result");
          if (enableCache && !StringUtil.isEmpty(cacheKey)) {
            LOGGER.d("cache cloud function result:" + JSON.toJSONString(resultValue));
            QueryResultCache.getInstance().cacheResult(cacheKey, JSON.toJSONString(resultMap));
          }
          if (resultValue instanceof Collection) {
            return (T) Utils.getObjectFrom((Collection) resultValue);
          } else if (resultValue instanceof Map) {
            return (T) Utils.getObjectFrom((Map) resultValue);
          } else {
            return (T) resultValue;
          }
        } catch (Exception ex) {
          LOGGER.d("CloudFunction error: " + ex.getMessage());
          return null;
        }
      }
    });
  }

  interface QueryExecutor {
    <T> Observable<T> executor();
  }
  <T> Observable<T> executeCachedQuery(final String clazz, final Map<String, Object> query,
                                       AVQuery.CachePolicy cachePolicy, final long maxAgeInMilliseconds,
                                       final QueryExecutor cacheQueryExecutor,
                                       final QueryExecutor remoteQueryExecutor) {
    Observable<T> result = null;
    switch (cachePolicy) {
      case CACHE_ONLY:
        result = cacheQueryExecutor.executor();
        break;
      case CACHE_ELSE_NETWORK:
        result = cacheQueryExecutor.executor();
        result = result.onErrorResumeNext(new Function<Throwable, Observable<T>>() {
          @Override
          public Observable<T> apply(Throwable throwable) throws Exception {
            LOGGER.d("failed to query local cache, cause: " + throwable.getMessage() + ", try to query networking");
            return remoteQueryExecutor.executor();
          }
        });
        break;
      case NETWORK_ELSE_CACHE:
        result = remoteQueryExecutor.executor();
        result = result.onErrorResumeNext(new Function<Throwable, Observable<T>>() {
          @Override
          public Observable<T> apply(Throwable throwable) throws Exception {
            LOGGER.d("failed to query networking, cause: " + throwable.getMessage()
                    + ", try to query local cache.");
            return cacheQueryExecutor.executor();
          }
        });
        break;
      case IGNORE_CACHE:
      default:
        result = remoteQueryExecutor.executor();
        break;
    }
    return result;
  }

  public <T> Observable<T> callRPCWithCachePolicy(final AVUser asAuthenticatedUser,
                                                  final String name, final Map<String, Object> param,
                                                  final AVQuery.CachePolicy cachePolicy, final long maxCacheAge) {
    final String cacheKey = QueryResultCache.generateCachedKey(name, param);
    return executeCachedQuery(name, param, cachePolicy, maxCacheAge,
            new QueryExecutor() {
              @Override
              public <T> Observable<T> executor() {
                return QueryResultCache.getInstance().getCacheRawResult(name, cacheKey, maxCacheAge, true)
                        .map(new Function<String, T>() {
                          @Override
                          public T apply(String s) throws Exception {
                            if (StringUtil.isEmpty(s)) {
                              return null;
                            }
                            LOGGER.d("found cached rpc result: " + s);
                            Object parsedObject = JSON.parse(s);
                            if (parsedObject instanceof Collection) {
                              return (T) Utils.getObjectFrom((Collection) parsedObject);
                            } else if (parsedObject instanceof Map) {
                              return (T) Utils.getObjectFrom((Map) parsedObject);
                            } else {
                              return (T) parsedObject;
                            }
                          }
                        });
              }
            },
            new QueryExecutor() {
              @Override
              public <T> Observable<T> executor() {
                return callRPC(asAuthenticatedUser, name, param,
                        cachePolicy != AVQuery.CachePolicy.IGNORE_CACHE && cachePolicy != AVQuery.CachePolicy.NETWORK_ONLY,
                        cacheKey);
              }
            });
  }

  public <T> Observable<T> callFunctionWithCachePolicy(final AVUser asAuthenticatedUser,
                                                       final String name, final Map<String, Object> params,
                                                       final AVQuery.CachePolicy cachePolicy, final long maxCacheAge) {
    final String cacheKey = QueryResultCache.generateCachedKey(name, params);
    return executeCachedQuery(name, params, cachePolicy, maxCacheAge,
            new QueryExecutor() {
              @Override
              public <T> Observable<T> executor() {
                return QueryResultCache.getInstance().getCacheRawResult(name, cacheKey, maxCacheAge, true)
                        .map(new Function<String, T>() {
                          @Override
                          public T apply(String s) throws Exception {
                            if (StringUtil.isEmpty(s)) {
                              return null;
                            }
                            LOGGER.d("found cached function result: " + s);
                            Object parsedObject = JSON.parseObject(s).get("result");
                            if (parsedObject instanceof Collection) {
                              return (T) Utils.getObjectFrom((Collection) parsedObject);
                            } else if (parsedObject instanceof Map) {
                              return (T) Utils.getObjectFrom((Map) parsedObject);
                            } else if (parsedObject instanceof Number) {
                              return (T) NumberDeserializerDoubleAsIntFix.parsePrecisionNumber((Number) parsedObject);
                            } else {
                              return (T) parsedObject;
                            }
                          }
                        });
              }
            },
            new QueryExecutor() {
              @Override
              public <T> Observable<T> executor() {
                return callFunction(asAuthenticatedUser, name, params,
                        cachePolicy != AVQuery.CachePolicy.IGNORE_CACHE && cachePolicy != AVQuery.CachePolicy.NETWORK_ONLY,
                        cacheKey);
              }
            });
  }


  public Observable<AVCaptchaDigest> requestCaptcha(AVCaptchaOption option) {
    return wrapObservable(apiService.requestCaptcha(option.getRequestParam()));
  }

  public Observable<AVCaptchaValidateResult> verifyCaptcha(String code, String token) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(token)) {
      return Observable.error(new IllegalArgumentException("code or token is empty"));
    }
    Map<String, String> param = new HashMap<String, String>(2);
    param.put("captcha_code", code);
    param.put("captcha_token", token);
    return wrapObservable(apiService.verifyCaptcha(param));
  }

  public Observable<AVNull> requestSMSCode(String mobilePhone, Map<String, Object> param) {
    param.put("mobilePhoneNumber", mobilePhone);
    return wrapObservable(apiService.requestSMSCode(param));
  }

  public Observable<AVNull> requestSMSCodeForUpdatingPhoneNumber(AVUser asUser, String mobilePhone, Map<String, Object> param) {
    param.put("mobilePhoneNumber", mobilePhone);
    String sessionToken = getSessionToken(asUser);
    return wrapObservable(apiService.requestSMSCodeForUpdatingPhoneNumber(sessionToken, param));
  }

  public Observable<AVNull> verifySMSCode(String code, String mobilePhone) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("code or mobilePhone is empty"));
    }
    Map<String, Object> param = new HashMap<String, Object>(1);
    param.put("mobilePhoneNumber", mobilePhone);
    return wrapObservable(apiService.verifySMSCode(code, param));
  }

  public Observable<AVNull> verifySMSCodeForUpdatingPhoneNumber(AVUser asUser, String code, String mobilePhone) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("code or mobilePhone is empty"));
    }
    String sessionToken = getSessionToken(asUser);
    Map<String, Object> param = new HashMap<String, Object>(1);
    param.put("mobilePhoneNumber", mobilePhone);
    param.put("code", code);
    return wrapObservable(apiService.verifySMSCodeForUpdatingPhoneNumber(sessionToken, param));
  }

  public Observable<AVSearchResponse> search(final AVUser authenticatedUser, Map<String, String> params) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.search(authenticatedSession, params));
  }
}
