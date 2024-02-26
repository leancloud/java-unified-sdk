package cn.leancloud.core;

import cn.leancloud.*;
import cn.leancloud.cache.QueryResultCache;
import cn.leancloud.gson.NumberDeserializerDoubleAsIntFix;
import cn.leancloud.ops.Utils;
import cn.leancloud.query.LCQueryResult;
import cn.leancloud.search.LCSearchResponse;
import cn.leancloud.service.APIService;
import cn.leancloud.sms.LCCaptchaDigest;
import cn.leancloud.sms.LCCaptchaOption;
import cn.leancloud.sms.LCCaptchaValidateResult;
import cn.leancloud.types.LCDate;
import cn.leancloud.types.LCNull;
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
  private static LCLogger LOGGER = LogUtil.getLogger(StorageClient.class);
  private static final String CUSTOM_ENDPOINT_PREFIX = "CUSTOM_ENDPOINT_";

  private APIService apiService = null;
  private boolean asynchronized = false;
  private AppConfiguration.SchedulerCreator defaultCreator = null;
  private QueryResultCache queryResultCache = QueryResultCache.getInstance();
  private LCUser currentUser = null;

  public StorageClient(APIService apiService, boolean asyncRequest, AppConfiguration.SchedulerCreator observerSchedulerCreator) {
    this.apiService = apiService;
    this.asynchronized = asyncRequest;
    this.defaultCreator = observerSchedulerCreator;
  }

  public void setCurrentUser(LCUser newUser) {
    this.currentUser = newUser;
  }

  public LCUser getCurrentUser() {
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

  public Observable wrapObservableNoResume(Observable observable) {
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

  private Observable wrapObservableInBackground(Observable observable) {
    if (null == observable) {
      return null;
    }
    Scheduler scheduler = Schedulers.io();
    if (asynchronized) {
      observable = observable.subscribeOn(scheduler);
    }
    observable = observable.observeOn(scheduler);
    return observable;
  }

  public Observable<LCDate> getServerTime() {
    Observable<LCDate> date = wrapObservable(apiService.currentTimeMillis());
    return date;
  }

  public Observable<? extends LCObject> fetchObject(final LCUser authenticatedUser,
                                                    final String className, String objectId, String includeKeys) {
    Observable<LCObject> object = null;
    String authenticatedSession = getSessionToken(authenticatedUser);
    if (StringUtil.isEmpty(includeKeys)) {
      object = wrapObservable(apiService.fetchObject(authenticatedSession, className, objectId));
    } else {
      object = wrapObservable(apiService.fetchObject(authenticatedSession, className, objectId, includeKeys));
    }
    if (null == object) {
      return object;
    }
    return object.map(new Function<LCObject, LCObject>() {
              public LCObject apply(LCObject LCObject) throws Exception {
                return Transformer.transform(LCObject, className);
              }
            });
  }

  public boolean hasCachedResult(String className, Map<String, String> query, long maxAgeInMilliseconds) {
    return QueryResultCache.getInstance().hasCachedResult(className, query, maxAgeInMilliseconds);
  }

  private String getSessionToken(LCUser avUser) {
    if (null == avUser) {
      if (AppConfiguration.isIncognitoMode() || null == LCUser.currentUser()) {
        return "";
      } else {
        return LCUser.currentUser().getSessionToken();
      }
    }
    return avUser.getSessionToken();
  }

  private Observable<LCQueryResult> queryRemoteServer(final LCUser authenticatedUser,
                                                      String className, final Map<String, String> query) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    if (LCUser.CLASS_NAME.equalsIgnoreCase(className)) {
      return wrapObservable(apiService.queryUsers(authenticatedSession, query));
    }else if(LCBlockRelation.CLASS_NAME.equalsIgnoreCase(className)){
      return wrapObservable(apiService.getBlockListOfFriend(authenticatedSession, query));
    } else if(className.startsWith(CUSTOM_ENDPOINT_PREFIX)){
      String endPoint = className.substring(CUSTOM_ENDPOINT_PREFIX.length());
      return wrapObservable(apiService.queryObjectsByCustomEndPoint(authenticatedSession, endPoint, query));
    } else{
      return wrapObservable(apiService.queryObjects(authenticatedSession, className, query));
    }

  }

  public Observable<List<LCUser>> strictlyQueryUsers(final LCUser authenticatedUser,
                                                     final Map<String, String> query){
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.strictlyQueryUsers(authenticatedSession, query))
      .map(new Function<LCQueryResult, List<LCObject>>() {
        public List<LCObject> apply(LCQueryResult o) throws Exception {
          o.setClassName(LCUser.CLASS_NAME);
          for (LCObject obj: o.getResults()) {
            obj.setClassName(LCUser.CLASS_NAME);
          }
          LOGGER.d("invoke within StorageClient.strictlyQueryUsers(). resultSize:"
                  + ((null != o.getResults())? o.getResults().size(): 0));
          return o.getResults();
        }
      })
      .map(new Function<List<LCObject>, List<LCUser>>() {
        public List<LCUser> apply(List<LCObject> var1) throws Exception {
          List<LCUser> result = new ArrayList<LCUser>(var1.size());
          for (LCObject obj: var1) {
            LCUser tmp = Transformer.transform(obj, LCUser.CLASS_NAME);
            result.add(tmp);
          }
          return result;
        }
      });                                        
  }


  public Observable<List<LCObject>> queryObjects(final LCUser authenticatedUser,
                                                 final String className, final String endPoint, final Map<String, String> query,
                                                 LCQuery.CachePolicy cachePolicy, final long maxAgeInMilliseconds) {
    final String cacheKey = QueryResultCache.generateKeyForQueryCondition(className, query);
    Observable<List<LCObject>> result = null;
    Observable<LCQueryResult> queryResult = null;
    //check whether use endPoint
    final String validPath = (endPoint == null || endPoint.length() == 0) ? className : (CUSTOM_ENDPOINT_PREFIX+endPoint);

    switch (cachePolicy) {
      case CACHE_ONLY:
        result = wrapObservable(
                QueryResultCache.getInstance().getCacheResult(className, query, maxAgeInMilliseconds, true));
        break;
      case CACHE_ELSE_NETWORK:
        result = wrapObservable(
                QueryResultCache.getInstance().getCacheResult(className, query, maxAgeInMilliseconds, false))
                .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends List<LCObject>>>() {
                  @Override
                  public ObservableSource<? extends List<LCObject>> apply(Throwable throwable) throws Exception {
                    LOGGER.d("failed to query local cache, cause: " + throwable.getMessage() + ", try to query networking");
                    return queryRemoteServer(authenticatedUser, validPath, query)
                            .map(new Function<LCQueryResult, List<LCObject>>() {
                              public List<LCObject> apply(LCQueryResult o) throws Exception {
                                o.setClassName(className);
                                for (LCObject obj: o.getResults()) {
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
      case CACHE_THEN_NETWORK:
        Observable<List<LCObject>> cacheResult = QueryResultCache.getInstance()
                .getCacheResult(className, query, maxAgeInMilliseconds, true);
        Observable<List<LCObject>> networkResult = queryRemoteServer(authenticatedUser, validPath, query)
                .map(new Function<LCQueryResult, List<LCObject>>() {
          @Override
          public List<LCObject> apply(LCQueryResult o) throws Exception {
            o.setClassName(className);
            for (LCObject obj : o.getResults()) {
              obj.setClassName(className);
            }
            QueryResultCache.getInstance().cacheResult(cacheKey, o.toJSONString());
            LOGGER.d("invoke within StorageClient.queryObjects(). resultSize:"
                    + ((null != o.getResults()) ? o.getResults().size() : 0));
            return o.getResults();
          }
        });
        result = wrapObservableNoResume(Observable.concat(cacheResult, networkResult));
        break;
      case NETWORK_ELSE_CACHE:
        queryResult =  queryRemoteServer(authenticatedUser, validPath, query);
        if (null != queryResult) {
          result = queryResult.map(new Function<LCQueryResult, List<LCObject>>() {
            public List<LCObject> apply(LCQueryResult o) throws Exception {
              o.setClassName(className);
              for (LCObject obj : o.getResults()) {
                obj.setClassName(className);
              }
              QueryResultCache.getInstance().cacheResult(cacheKey, o.toJSONString());
              LOGGER.d("invoke within StorageClient.queryObjects(). resultSize:"
                      + ((null != o.getResults()) ? o.getResults().size() : 0));
              return o.getResults();
            }
          }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends List<LCObject>>>() {
            @Override
            public ObservableSource<? extends List<LCObject>> apply(Throwable throwable) throws Exception {
              LOGGER.d("failed to query networking, cause: " + throwable.getMessage()
                      + ", try to query local cache.");
              return QueryResultCache.getInstance().getCacheResult(className, query, maxAgeInMilliseconds, true);
            }
          });
        }
        break;
      case IGNORE_CACHE:
      default:
        queryResult = queryRemoteServer(authenticatedUser, validPath, query);
        if (null != queryResult) {
          result = queryResult.map(new Function<LCQueryResult, List<LCObject>>() {
            public List<LCObject> apply(LCQueryResult o) throws Exception {
              o.setClassName(className);
              for (LCObject obj: o.getResults()) {
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

  public Observable<LCQueryResult> cloudQuery(final LCUser authenticatedUser, Map<String, String> query) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.cloudQuery(authenticatedSession, query));
  }

  public Observable<Integer> queryCount(final LCUser authenticatedUser,
                                        final String className, Map<String, String> query) {
    Observable<LCQueryResult> queryResult = this.queryRemoteServer(authenticatedUser, className, query);
    if (null == queryResult) {
      return null;
    }
    return queryResult.map(new Function<LCQueryResult, Integer>() {
      public Integer apply(LCQueryResult o) throws Exception {
        LOGGER.d("invoke within StorageClient.queryCount(). result:" + o + ", return:" + o.getCount());
        return o.getCount();
      }
    });
  }

  public Observable<LCNull> deleteObject(final LCUser authenticatedUser,
                                         final String className, String objectId, Map<String, Object> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.deleteObject(authenticatedSession, className, objectId, param));
  }

  public Observable<? extends LCObject> createObject(final LCUser authenticatedUser,
                                                     final String className, JSONObject data, boolean fetchFlag,
                                                     JSONObject where) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<LCObject> object = wrapObservable(apiService.createObject(authenticatedSession, className, data, fetchFlag,
            where));
    if (null == object) {
      return null;
    }
    return object.map(new Function<LCObject, LCObject>() {
      public LCObject apply(LCObject LCObject) {
        LOGGER.d(LCObject.toString());
        return Transformer.transform(LCObject, className);
      }
    });
  }

  public Observable<? extends LCObject> saveObject(final LCUser authenticatedUser,
                                                   final String className, String objectId, JSONObject data,
                                                   boolean fetchFlag, JSONObject where) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<LCObject> object = wrapObservable(apiService.updateObject(authenticatedSession, className, objectId, data,
            fetchFlag, where));
    if (null == object) {
      return null;
    }
    return object.map(new Function<LCObject, LCObject>() {
      public LCObject apply(LCObject LCObject) {
        LOGGER.d("saveObject finished. intermediaObj=" + LCObject.toString() + ", convert to " + className);
        return Transformer.transform(LCObject, className);
      }
    });
  }

  public <E extends LCObject> Observable<E> saveWholeObject(final LCUser authenticatedUser,
                                                            final Class<E> clazz, final String endpointClass,
                                                            String objectId,
                                                            JSONObject object, boolean fetchFlag, JSONObject where) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<LCObject> result = null;
    if (StringUtil.isEmpty(objectId)) {
      result = wrapObservable(apiService.saveWholeObject(authenticatedSession, endpointClass, object, fetchFlag, where));
    } else {
      result = wrapObservable(apiService.saveWholeObject(authenticatedSession, endpointClass, objectId, object,
              fetchFlag, where));
    }

    if (null == result) {
      return null;
    }
    return result.map(new Function<LCObject, E>() {
      @Override
      public E apply(LCObject LCObject) throws Exception {
        return Transformer.transform(LCObject, clazz);
      }
    });
  }

  public Observable<LCObject> getWholeObject(final LCUser authenticatedUser,
                                             final String endpointClass, String objectId, String includeKeys) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.getWholeObject(authenticatedSession, endpointClass, objectId, includeKeys));
  }

  public Observable<LCNull> deleteWholeObject(final LCUser authenticatedUser,
                                              final String endpointClass, String objectId, Map<String, Object> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.deleteWholeObject(authenticatedSession, endpointClass, objectId, param));
  }

  public Observable<LCFile> fetchFile(final LCUser authenticatedUser, String objectId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<LCFile> object = wrapObservable(apiService.fetchFile(authenticatedSession, objectId));
    if (null == object) {
      return null;
    }
    return object.map(new Function<LCFile, LCFile>() {
      public LCFile apply(LCFile avFile) throws Exception {
        avFile.setClassName(LCFile.CLASS_NAME);
        return avFile;
      }
    });
  }

  public Observable<FileUploadToken> newUploadToken(final LCUser authenticatedUser, JSONObject fileData) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservableInBackground(apiService.createUploadToken(authenticatedSession, fileData));
  }

  public void fileCallback(final LCUser authenticatedUser, JSONObject result) throws IOException {
    String authenticatedSession = getSessionToken(authenticatedUser);
    apiService.fileCallback(authenticatedSession, result).execute();
    return;
  }

  public Observable<List<Map<String, Object>>> batchSave(final LCUser authenticatedUser, JSONObject parameter) {
    // resposne is:
    // [{"success":{"updatedAt":"2018-03-30T06:21:08.052Z","objectId":"5abd026d9f54540038791715"}},
    //  {"success":{"updatedAt":"2018-03-30T06:21:08.092Z","objectId":"5abd026d9f54540038791715"}},
    //  {"success":{"updatedAt":"2018-03-30T06:21:08.106Z","objectId":"5abd026d9f54540038791715"}}]
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<List<Map<String, Object>>> result = wrapObservable(apiService.batchCreate(authenticatedSession,
            parameter));
    return result;
  }

  public Observable<JSONObject> batchUpdate(final LCUser authenticatedUser, JSONObject parameter) {
    // response is:
    // {"5abd026d9f54540038791715":{"updatedAt":"2018-03-30T06:21:46.084Z","objectId":"5abd026d9f54540038791715"}}
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<JSONObject> result = wrapObservable(apiService.batchUpdate(authenticatedSession, parameter));
    return result;
  }

  public Observable<LCUser> signUp(JSONObject data) {
    return wrapObservable(apiService.signup(data));
  }
  public Observable<LCUser> signUpWithFlag(JSONObject data, boolean failOnNotExist) {
    return wrapObservable(apiService.signup(data, failOnNotExist));
  }
  public <T extends LCUser> Observable<T> signUpOrLoginByMobilephone(final JSONObject data, final Class<T> clazz) {
    return wrapObservable(apiService.signupByMobilePhone(data)).map(new Function<LCUser, T>() {
      @Override
      public T apply(LCUser avUser) throws Exception {
        T rst = Transformer.transform(avUser, clazz);
        attachLoginInfo(data, rst);
        LCUser.changeCurrentUser(rst, true);
        return rst;
      }
    });
  }

  private <T extends LCUser> void attachLoginInfo(final JSONObject data, T rst) {
    if (null == data || null == rst) {
      return;
    }
    if (data.containsKey(LCUser.ATTR_EMAIL)) {
      rst.setEmail(data.getString(LCUser.ATTR_EMAIL));
    }
    if (data.containsKey(LCUser.ATTR_USERNAME)) {
      rst.setUsername(data.getString(LCUser.ATTR_USERNAME));
    }
    if (data.containsKey(LCUser.ATTR_MOBILEPHONE)) {
      rst.setMobilePhoneNumber(data.getString(LCUser.ATTR_MOBILEPHONE));
    }
  }
  public <T extends LCUser> Observable<T> logIn(final JSONObject data, final Class<T> clazz) {
    Observable<LCUser> object = wrapObservable(apiService.login(data));
    if (null == object) {
      return null;
    }
    return object.map(new Function<LCUser, T>() {
      public T apply(LCUser avUser) throws Exception {
        T rst = Transformer.transform(avUser, clazz);
        attachLoginInfo(data, rst);
        LCUser.changeCurrentUser(rst, true);
        return rst;
      }
    });
  }

  public Observable<LCFriendshipRequest> applyFriendshipRequest(final LCUser authenticatedUser, final JSONObject data) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<LCObject> result = wrapObservable(apiService.applyFriendship(authenticatedSession, data));
    if (null == result) {
      return null;
    }
    return result.map(new Function<LCObject, LCFriendshipRequest>() {
      @Override
      public LCFriendshipRequest apply(LCObject LCObject) throws Exception {
        return Transformer.transform(LCObject, LCFriendshipRequest.class);
      }
    });
  }

  public Observable<LCFriendshipRequest> acceptFriendshipRequest(final LCUser authenticatedUser,
                                                                 final LCFriendshipRequest request, JSONObject param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<LCObject> result = wrapObservable(apiService.acceptFriendshipRequest(authenticatedSession,
            request.getObjectId(), param));
    if (null == result) {
      return null;
    }
    return result.map(new Function<LCObject, LCFriendshipRequest>() {
      @Override
      public LCFriendshipRequest apply(LCObject LCObject) throws Exception {
        LCFriendshipRequest response = Transformer.transform(LCObject, LCFriendshipRequest.class);
        request.getServerData().put(LCFriendshipRequest.ATTR_STATUS, LCFriendshipRequest.INTERNAL_STATUS_ACCEPTED);
        request.getServerData().put(LCObject.KEY_UPDATED_AT, response.getUpdatedAtString());
        return request;
      }
    });
  }

  public Observable<LCFriendshipRequest> declineFriendshipRequest(final LCUser authenticatedUser,
                                                                  final LCFriendshipRequest request) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    Observable<LCObject> result = wrapObservable(apiService.declineFriendshipRequest(authenticatedSession, request.getObjectId()));
    return result.map(new Function<LCObject, LCFriendshipRequest>() {
      @Override
      public LCFriendshipRequest apply(LCObject LCObject) throws Exception {
        LCFriendshipRequest response = Transformer.transform(LCObject, LCFriendshipRequest.class);
        request.getServerData().put(LCFriendshipRequest.ATTR_STATUS, LCFriendshipRequest.INTERNAL_STATUS_DECLINED);
        request.getServerData().put(LCObject.KEY_UPDATED_AT, response.getUpdatedAtString());
        return request;
      }
    });
  }

  public Observable<Boolean> checkAuthenticated(String sessionToken) {
    Map<String, String> param = new HashMap<String, String>(1);
    param.put("session_token", sessionToken);
    Observable<LCUser> apiResult = wrapObservable(apiService.checkAuthenticated(sessionToken, param));
    if (null == apiResult) {
      return Observable.just(false);
    }
    return apiResult.map(new Function<LCUser, Boolean>() {
      public Boolean apply(LCUser o) throws Exception {
        if (null != o) {
          return true;
        } else {
          return false;
        }
      }
    });
  }

  public <T extends LCUser> Observable<T> createUserBySession(String sessionToken, final Class<T> clazz) {
    Map<String, String> param = new HashMap<String, String>(1);
    param.put("session_token", sessionToken);
    Observable<LCUser> result = wrapObservable(apiService.checkAuthenticated(sessionToken, param));
    return result.map(new Function<LCUser, T>() {
      public T apply(LCUser avUser) throws Exception {
        if (null == avUser) {
          LOGGER.e("The mapper function returned a null value.");
          return null;
        }
        return Transformer.transform(avUser, clazz);
      }
    });
  }

  public Observable<Boolean> refreshSessionToken(final LCUser user) {
    return wrapObservable(apiService.refreshSessionToken(user.getSessionToken(), user.getObjectId()).map(new Function<LCUser, Boolean>() {
      public Boolean apply(LCUser avUser) throws Exception {
        if (null != avUser && !StringUtil.isEmpty(avUser.getSessionToken())) {
          user.internalChangeSessionToken(avUser.getSessionToken());
          return true;
        }
        return false;
      }
    }));
  }

  public Observable<JSONObject> retrieveShortToken(String sessionToken) {
    return wrapObservable(apiService.retrieveShortToken(sessionToken));
  }

  public Observable<LCNull> requestResetPassword(String email) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("email", email);
    return wrapObservable(apiService.requestResetPassword(map));
  }

  public Observable<LCNull> requestResetPasswordBySmsCode(String phoneNumber, String validateToken) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("mobilePhoneNumber", phoneNumber);
    if (!StringUtil.isEmpty(validateToken)) {
      map.put("validate_token", validateToken);
    }
    return wrapObservable(apiService.requestResetPasswordBySmsCode(map));
  }

  public Observable<LCNull> requestEmailVerify(String email) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("email", email);
    return wrapObservable(apiService.requestEmailVerify(map));
  }

  public Observable<LCNull> requestMobilePhoneVerify(String mobilePhone, String validateToken) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("mobilePhoneNumber", mobilePhone);
    if (!StringUtil.isEmpty(validateToken)) {
      map.put("validate_token", validateToken);
    }
    return wrapObservable(apiService.requestMobilePhoneVerify(map));
  }

  public Observable<LCNull> verifyMobilePhone(String verifyCode, String phoneNumber) {
    Map<String, String> map = new HashMap<String, String>();
    if (phoneNumber != null) {
      map.put("mobilePhoneNumber", phoneNumber);
    }
    return wrapObservable(apiService.verifyMobilePhone(verifyCode, map));
  }

  public Observable<LCNull> requestLoginSmsCode(String phoneNumber, String validateToken) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("mobilePhoneNumber", phoneNumber);
    if (!StringUtil.isEmpty(validateToken)) {
      map.put("validate_token", validateToken);
    }
    return wrapObservable(apiService.requestLoginSmsCode(map));
  }

  public Observable<LCNull> resetPasswordBySmsCode(String smsCode, String newPass, String phoneNumber) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("password", newPass);
    if (phoneNumber != null) {
      map.put("mobilePhoneNumber", phoneNumber);
    }
    return wrapObservable(apiService.resetPasswordBySmsCode(smsCode, map));
  }

  public Observable<LCNull> updatePassword(final LCUser user, String oldPass, String newPass) {
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
            .map(new Function<LCUser, LCNull>() {
      public LCNull apply(LCUser var1) throws Exception {
        if (null != var1) {
          user.internalChangeSessionToken(var1.getSessionToken());
        }
        return new LCNull();
      }
    }));
  }

  public Observable<JSONObject> followUser(final LCUser authenticatedUser,
                                           String followee, String follower, Map<String, Object> attr) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.followUser(authenticatedSession, followee, follower, attr));
  }

  public Observable<JSONObject> unfollowUser(final LCUser authenticatedUser,
                                             String followee, String follower) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.unfollowUser(authenticatedSession, followee, follower));
  }

  public Observable<LCFriendship> updateFriendship(final LCUser authenticatedUser,
                                                   String followeeUserid, String friendObjectId, Map<String, Object> attr) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.updateFriendship(authenticatedSession, followeeUserid, friendObjectId, attr));
  }

  public Observable<JSONObject> getFollowersAndFollowees(final LCUser authenticatedUser, String userId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.getFollowersAndFollowees(authenticatedSession, userId));
  }

  public Observable<List<LCFriendship>> queryFriendship(final LCUser authenticatedUser, Map<String, String> conditions) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(
            apiService.getFriends(authenticatedSession, conditions)
                    .map(new Function<LCQueryResult, List<LCFriendship>>() {
      @Override
      public List<LCFriendship> apply(LCQueryResult result) throws Exception {
        if (null == result || null == result.getResults()) {
          return null;
        }
        List<LCObject> originResult = result.getResults();
        List<LCFriendship> convertedResult = new ArrayList<>(originResult.size());
        for (LCObject obj : originResult) {
          convertedResult.add(new LCFriendship(obj));
        }
        return convertedResult;
      }
    }));
  }

  public Observable<JSONObject> blockFriend(final LCUser authenticatedUser, String objectId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.blockFriendByObjectId(authenticatedSession, objectId));
  }

  public Observable<JSONObject> unblockFriend(final LCUser authenticatedUser, String objectId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.unblockFriendByObjectId(authenticatedSession, objectId));
  }

  public Observable<LCStatus> postStatus(final LCUser authenticatedUser, Map<String, Object> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.postStatus(authenticatedSession, param));
  }

  public Observable<LCStatus> fetchStatus(final LCUser authenticatedUser, String objectId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.fetchSingleStatus(authenticatedSession, objectId));
  }

  public Observable<List<LCStatus>> queryStatus(final LCUser authenticatedUser, Map<String, String> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.fetchStatuses(authenticatedSession, param)
            .map(new Function<LCQueryResult, List<LCStatus>>() {
      @Override
      public List<LCStatus> apply(LCQueryResult o) throws Exception {
        if (null == o) {
          LOGGER.e("The mapper function returned a null value.");
          return null;
        }
        List<LCStatus> results = new ArrayList<>();
        for (LCObject obj: o.getResults()) {
          results.add(new LCStatus(obj));
        }
        return results;
      }
    }));
  }

  public Observable<List<LCStatus>> queryInbox(final LCUser authenticatedUser, Map<String, String> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.queryInbox(authenticatedSession, param)
            .map(new Function<LCQueryResult, List<LCStatus>>() {
      @Override
      public List<LCStatus> apply(LCQueryResult o) throws Exception {
        if (null == o) {
          LOGGER.e("The mapper function returned a null value.");
          return null;
        }
        List<LCStatus> results = new ArrayList<>();
        for (LCObject obj: o.getResults()) {
          results.add(new LCStatus(obj));
        }
        return results;
      }
    }));
  }

  public Observable<JSONObject> getInboxCount(final LCUser authenticatedUser, Map<String, String> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.getInboxCount(authenticatedSession, param));
  }

  public Observable<LCNull> deleteStatus(final LCUser authenticatedUser, String statusId) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.deleteStatus(authenticatedSession, statusId));
  }

  public Observable<LCNull> deleteInboxStatus(final LCUser authenticatedUser, Map<String, Object> param) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.deleteInboxStatus(authenticatedSession, param));
  }

  public <T> Observable<T> callRPC(final LCUser authenticatedUser, String name, Object param) {
    return callRPC(authenticatedUser, name, param, false, null);
  }

  <T> Observable<T> callRPC(final LCUser authenticatedUser,
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
            // Notice!!
            // 这里缓存内容的不同（与 callFunc 相比），导致 RPC 结果解析时需要采用完全不一样的方法——需要调用方指定结果类型。
            // 应该是统一成 callFunc 的结果才对，不过那样会导致原有缓存失效，并且要解析之前缓存的结果也还是需要明确告知结果类型。
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

  public <T> Observable<T> callFunction(final LCUser authenticatedUser, String name, Map<String, Object> params) {
    return callFunction(authenticatedUser, name, params, false, null);
  }

  <T> Observable<T> callFunction(final LCUser authenticatedUser,
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
            // Notice!!
            // 这里缓存内容的不同（与 callRPC 相比），造成了缓存 json 解析的差异。
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
                                       LCQuery.CachePolicy cachePolicy, final long maxAgeInMilliseconds,
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

  public <T> Observable<T> callRPCWithCachePolicy(final LCUser asAuthenticatedUser,
                                                  final String name, final Map<String, Object> param,
                                                  final LCQuery.CachePolicy cachePolicy, final long maxCacheAge,
                                                  final Class<T> clazz) {
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
                            Object parsedObject = JSON.parseObject(s, clazz);
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
                        cachePolicy != LCQuery.CachePolicy.IGNORE_CACHE && cachePolicy != LCQuery.CachePolicy.NETWORK_ONLY,
                        cacheKey);
              }
            });
  }

  public <T> Observable<T> callFunctionWithCachePolicy(final LCUser asAuthenticatedUser,
                                                       final String name, final Map<String, Object> params,
                                                       final LCQuery.CachePolicy cachePolicy, final long maxCacheAge,
                                                       final Class<T> clazz) {
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
                            Object parsedObject = null;
                            try {
                              JSONObject tmpObject = JSON.parseObject(s);
                              if (null != tmpObject && tmpObject.containsKey("result")) {
                                parsedObject = tmpObject.get("result");
                              } else {
                                // compatible for existing cache data(json).
                                parsedObject = tmpObject;
                              }
                            } catch (Exception exception) {
                              // compatible for existing cache data(array or primitives).
                              // 应该不会发生。
                              parsedObject = JSON.parseObject(s, clazz);
                            }
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
                        cachePolicy != LCQuery.CachePolicy.IGNORE_CACHE && cachePolicy != LCQuery.CachePolicy.NETWORK_ONLY,
                        cacheKey);
              }
            });
  }


  public Observable<LCCaptchaDigest> requestCaptcha(LCCaptchaOption option) {
    return wrapObservable(apiService.requestCaptcha(option.getRequestParam()));
  }

  public Observable<LCCaptchaValidateResult> verifyCaptcha(String code, String token) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(token)) {
      return Observable.error(new IllegalArgumentException("code or token is empty"));
    }
    Map<String, String> param = new HashMap<String, String>(2);
    param.put("captcha_code", code);
    param.put("captcha_token", token);
    return wrapObservable(apiService.verifyCaptcha(param));
  }

  public Observable<LCNull> requestSMSCode(String mobilePhone, Map<String, Object> param) {
    param.put("mobilePhoneNumber", mobilePhone);
    return wrapObservable(apiService.requestSMSCode(param));
  }

  public Observable<LCNull> requestSMSCodeForUpdatingPhoneNumber(LCUser asUser, String mobilePhone, Map<String, Object> param) {
    param.put("mobilePhoneNumber", mobilePhone);
    String sessionToken = getSessionToken(asUser);
    return wrapObservable(apiService.requestSMSCodeForUpdatingPhoneNumber(sessionToken, param));
  }

  public Observable<LCNull> verifySMSCode(String code, String mobilePhone) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("code or mobilePhone is empty"));
    }
    Map<String, Object> param = new HashMap<String, Object>(1);
    param.put("mobilePhoneNumber", mobilePhone);
    return wrapObservable(apiService.verifySMSCode(code, param));
  }

  public Observable<LCNull> verifySMSCodeForUpdatingPhoneNumber(LCUser asUser, String code, String mobilePhone) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("code or mobilePhone is empty"));
    }
    String sessionToken = getSessionToken(asUser);
    Map<String, Object> param = new HashMap<String, Object>(1);
    param.put("mobilePhoneNumber", mobilePhone);
    param.put("code", code);
    return wrapObservable(apiService.verifySMSCodeForUpdatingPhoneNumber(sessionToken, param));
  }

  public Observable<LCSearchResponse> search(final LCUser authenticatedUser, Map<String, String> params) {
    String authenticatedSession = getSessionToken(authenticatedUser);
    return wrapObservable(apiService.search(authenticatedSession, params));
  }

  /**
   * Leaderboard API
   */

  /**
   * create leaderboard.
   * @param params parameters.
   * @return observer instance.
   */
  public Observable<LCObject> createLeaderboard(Map<String, Object> params) {
    return wrapObservable(apiService.createLeaderboard(params));
  }

  /**
   * fetch leaderboard with name.
   * @param name leaderboard name
   * @return observer instance.
   */
  public Observable<LCObject> fetchLeaderboard(String name) {
    return wrapObservable(apiService.fetchLeaderboard(name));
  }

  /**
   * update leaderboard with attributes.
   * @param name leaderboard name
   * @param params attributes.
   * @return observer instance.
   */
  public Observable<LCObject> updateLeaderboard(String name, Map<String, Object> params) {
    return wrapObservable(apiService.updateLeaderboard(name, params));
  }

  /**
   * reset leaderboard
   * @param name leaderboard name
   * @return observer instance.
   */
  public Observable<LCObject> resetLeaderboard(String name) {
    return wrapObservable(apiService.resetLeaderboard(name));
  }

  /**
   * destroy leaderboard
   * @param name leaderboard name
   * @return observer instance.
   */
  public Observable<Boolean> destroyLeaderboard(String name) {
    return wrapObservable(apiService.destroyLeaderboard(name)).map(new Function<LCObject, Boolean>() {
      @Override
      public Boolean apply(LCObject object) throws Exception {
        return null != object;
      }
    });
  }

  /**
   * update user statistics.
   * @param user user object
   * @param params attributes.
   * @param overwrite overwrite flag.
   * @return observer instance.
   */
  public Observable<LCStatisticResult> updateUserStatistics(final LCUser user,
                                                     List<Map<String, Object>> params,
                                                     boolean overwrite) {
    if (null == user) {
      return Observable.error(new IllegalArgumentException("user is null"));
    }
    if (null == params || params.size() < 1) {
      return Observable.error(new IllegalArgumentException("params is empty"));
    }
    String sessionToken = user.getSessionToken();
    int overwriteFlag = overwrite? 1: 0;
    if (user.isAuthenticated()) {
      return wrapObservable(apiService.updateAuthenticatedUserStatistics(sessionToken, params, overwriteFlag));
    } else {
      return wrapObservable(apiService.updateUserStatistics(user.getObjectId(), params, overwriteFlag));
    }
  }

  public Observable<LCStatisticResult> updateObjectStatistics(final String objectId,
                                                            List<Map<String, Object>> params,
                                                            boolean overwrite) {
    if (StringUtil.isEmpty(objectId)) {
      return Observable.error(new IllegalArgumentException("objectId is invalid."));
    }
    if (null == params || params.size() < 1) {
      return Observable.error(new IllegalArgumentException("params is invalid."));
    }
    int overwriteFlag = overwrite? 1: 0;
    return wrapObservable(apiService.updateObjectStatistics(objectId, params, overwriteFlag));
  }

  public Observable<LCStatisticResult> updateEntityStatistics(final String entityId,
                                                            List<Map<String, Object>> params,
                                                            boolean overwrite) {
    if (StringUtil.isEmpty(entityId)) {
      return Observable.error(new IllegalArgumentException("entityId is invalid."));
    }
    if (null == params || params.size() < 1) {
      return Observable.error(new IllegalArgumentException("params is invalid."));
    }
    int overwriteFlag = overwrite? 1: 0;
    return wrapObservable(apiService.updateEntityStatistics(entityId, params, overwriteFlag));
  }

  public Observable<LCStatisticResult> getUserStatistics(final String userObjectId, List<String> statisticNames) {
    if (StringUtil.isEmpty(userObjectId)) {
      return Observable.error(new IllegalArgumentException("userObjectId is invalid."));
    }
    String statistics = StringUtil.join(",", statisticNames);
    return wrapObservable(apiService.getUserStatistics(userObjectId, statistics));
  }

  public Observable<LCStatisticResult> getEntityStatistics(final String entityId, List<String> statisticNames) {
    if (StringUtil.isEmpty(entityId)) {
      return Observable.error(new IllegalArgumentException("entityId is null"));
    }
    String statistics = StringUtil.join(",", statisticNames);
    return wrapObservable(apiService.getEntityStatistics(entityId, statistics));
  }

  public Observable<LCStatisticResult> getObjectStatistics(final String objectId, List<String> statisticNames) {
    if (StringUtil.isEmpty(objectId)) {
      return Observable.error(new IllegalArgumentException("objectId is null"));
    }
    String statistics = StringUtil.join(",", statisticNames);
    return wrapObservable(apiService.getObjectStatistics(objectId, statistics));
  }

  public Observable<LCStatisticResult> getGroupStatistics(final String leaderboardType, final String statisticName,
                                                          List<String> targetKeys) {
    if (StringUtil.isEmpty(leaderboardType)) {
      return Observable.error(new IllegalArgumentException("leaderboard type is null"));
    }
    if (StringUtil.isEmpty(statisticName)) {
      return Observable.error(new IllegalArgumentException("statistic name is null"));
    }
    if (null == targetKeys || targetKeys.size() < 1) {
      return Observable.just(new LCStatisticResult());
    }
    Map<String, Object> param = new HashMap<>();
    param.put("ids", targetKeys);
    return wrapObservable(apiService.queryGroupStatistics(leaderboardType, statisticName, param));
  }

  public Observable<LCLeaderboardResult> getLeaderboardResults(String leaderboardType, String statisticName, int skip, int limit,
                                                      List<String> selectUserKeys,
                                                      List<String> includeUserKeys,
                                                      List<String> includeStatisticNames,
                                                      int version, boolean withCount) {
    if (StringUtil.isEmpty(leaderboardType) || StringUtil.isEmpty(statisticName)) {
      return Observable.error(new IllegalArgumentException("memberType or statisticName is null"));
    }
    String selectKeys = StringUtil.join(",", selectUserKeys);
    String includeKeys = StringUtil.join(",", includeUserKeys);
    String includeStatistics = StringUtil.join(",", includeStatisticNames);
    Map<String, Object> params = new HashMap<>();
    if (skip > 0) {
      params.put("startPosition", skip);
    }
    if (limit > 0) {
      params.put("maxResultsCount", limit);
    }
    if (!StringUtil.isEmpty(selectKeys)) {
      params.put("selectKeys", selectKeys);
    }
    if (!StringUtil.isEmpty(includeKeys)) {
      params.put("includeKeys", includeKeys);
    }
    if (!StringUtil.isEmpty(includeStatistics)) {
      params.put("includeStatistics", includeStatistics);
    }
    if (version > LCLeaderboard.INVALID_VERSION) {
      params.put("version", version);
    }
    if (withCount) {
      params.put("count", 1);
    }
    return wrapObservable(apiService.getLeaderboardResults(leaderboardType, statisticName, params));
  }

  public Observable<LCLeaderboardResult> getLeaderboardGroupResults(String leaderboardType, String statisticName,
                                                                    List<String> groupUserIds,
                                                                    int skip, int limit, List<String> selectUserKeys,
                                                                    List<String> includeUserKeys,
                                                                    List<String> includeStatisticNames,
                                                                    int version) {
    if (StringUtil.isEmpty(leaderboardType) || StringUtil.isEmpty(statisticName)) {
      return Observable.error(new IllegalArgumentException("memberType or statisticName is null"));
    }
    if (!"user".equalsIgnoreCase(leaderboardType)) {
      return Observable.error(new IllegalArgumentException("only memberType of user is supported."));
    }
    if (null == groupUserIds || groupUserIds.isEmpty()) {
      return Observable.error(new IllegalArgumentException("group user id is empty."));
    }
    String selectKeys = StringUtil.join(",", selectUserKeys);
    String includeKeys = StringUtil.join(",", includeUserKeys);
    String includeStatistics = StringUtil.join(",", includeStatisticNames);
    Map<String, Object> params = new HashMap<>();
    if (skip > 0) {
      params.put("startPosition", skip);
    }
    if (limit > 0) {
      params.put("maxResultsCount", limit);
    }
    if (!StringUtil.isEmpty(selectKeys)) {
      params.put("selectKeys", selectKeys);
    }
    if (!StringUtil.isEmpty(includeKeys)) {
      params.put("includeKeys", includeKeys);
    }
    if (!StringUtil.isEmpty(includeStatistics)) {
      params.put("includeStatistics", includeStatistics);
    }
    if (version > LCLeaderboard.INVALID_VERSION) {
      params.put("version", version);
    }
    Map<String, Object> data = new HashMap<>();
    data.put("ids", groupUserIds);
    return wrapObservable(apiService.queryLeaderboardGroupResults(leaderboardType, statisticName, params, data));
  }

  public Observable<LCLeaderboardResult> getLeaderboardAroundResults(String leaderboardType, String statisticName, String targetId,
                                                            int skip, int limit, List<String> selectUserKeys,
                                                            List<String> includeUserKeys,
                                                            List<String> includeStatisticNames, int version) {
    if (StringUtil.isEmpty(leaderboardType) || StringUtil.isEmpty(statisticName)) {
      return Observable.error(new IllegalArgumentException("memberType or statisticName is null"));
    }
    String selectKeys = StringUtil.join(",", selectUserKeys);
    String includeKeys = StringUtil.join(",", includeUserKeys);
    String includeStatistics = StringUtil.join(",", includeStatisticNames);
    Map<String, Object> params = new HashMap<>();
    if (skip > 0) {
      params.put("startPosition", skip);
    }
    if (limit > 0) {
      params.put("maxResultsCount", limit);
    }
    if (!StringUtil.isEmpty(selectKeys)) {
      params.put("selectKeys", selectKeys);
    }
    if (!StringUtil.isEmpty(includeKeys)) {
      params.put("includeKeys", includeKeys);
    }
    if (!StringUtil.isEmpty(includeStatistics)) {
      params.put("includeStatistics", includeStatistics);
    }
    if (version > LCLeaderboard.INVALID_VERSION) {
      params.put("version", version);
    }
    return wrapObservable(apiService.getLeaderboardAroundResults(leaderboardType, statisticName, targetId, params));
  }

  public Observable<LCLeaderboardResult> getLeaderboardAroundInGroupResults(String leaderboardType, String statisticName,
                                                                            List<String> groupUserIds, String targetId,
                                                                            int limit, List<String> selectUserKeys,
                                                                            List<String> includeUserKeys,
                                                                            List<String> includeStatisticNames,
                                                                            int version) {
    if (StringUtil.isEmpty(leaderboardType) || StringUtil.isEmpty(statisticName)) {
      return Observable.error(new IllegalArgumentException("memberType or statisticName is null"));
    }
    if (!"user".equalsIgnoreCase(leaderboardType)) {
      return Observable.error(new IllegalArgumentException("only memberType of user is supported."));
    }
    if (null == groupUserIds || groupUserIds.isEmpty()) {
      return Observable.error(new IllegalArgumentException("group user id is empty."));
    }
    String selectKeys = StringUtil.join(",", selectUserKeys);
    String includeKeys = StringUtil.join(",", includeUserKeys);
    String includeStatistics = StringUtil.join(",", includeStatisticNames);
    Map<String, Object> params = new HashMap<>();
    if (limit > 0) {
      params.put("maxResultsCount", limit);
    }
    if (!StringUtil.isEmpty(selectKeys)) {
      params.put("selectKeys", selectKeys);
    }
    if (!StringUtil.isEmpty(includeKeys)) {
      params.put("includeKeys", includeKeys);
    }
    if (!StringUtil.isEmpty(includeStatistics)) {
      params.put("includeStatistics", includeStatistics);
    }
    if (version > LCLeaderboard.INVALID_VERSION) {
      params.put("version", version);
    }
    Map<String, Object> data = new HashMap<>();
    data.put("ids", groupUserIds);
    return wrapObservable(apiService.queryLeaderboardAroundInGroupResults(leaderboardType, statisticName, targetId,
            params, data));
  }
}
