package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.core.PaasClient;
import cn.leancloud.ops.DeleteOperation;
import cn.leancloud.ops.RemoveOperation;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO: need transfer Anonymous User/Common User

@AVClassName("_User")
@JSONType(deserializer = ObjectTypeAdapter.class, serializer = ObjectTypeAdapter.class)
public class AVUser extends AVObject {
  private static final String ATTR_USERNAME = "username";
  private static final String ATTR_PASSWORD = "password";
  private static final String ATTR_EMAIL = "email";
  private static final String ATTR_MOBILEPHONE = "mobilePhoneNumber";
  private static final String ATTR_SMSCODE = "smsCode";
  private static final String ATTR_MOBILEPHONE_VERIFIED = "mobilePhoneVerified";
  public static final String ATTR_SESSION_TOKEN = "sessionToken";

  private static final String AUTHDATA_TAG = "authData";

  private static final String AUTHDATA_ATTR_UNIONID = "unionid";
  private static final String AUTHDATA_ATTR_UNIONID_PLATFORM = "platform";
  private static final String AUTHDATA_ATTR_MAIN_ACCOUNT = "main_account";

  private static final String ILLEGALARGUMENT_MSG_FORMAT = "illegal parameter. %s must not null/empty.";

  public static final String CLASS_NAME = "_User";

  public enum SNS_PLATFORM {
    FACEBOOK("facebook"), TWITTER("twitter"), QQ("qq"), WEIBO("weibo"), WECHAT("weixin");
    SNS_PLATFORM(String name) {
      this.name = name;
    }
    private String name;
    public String getName() {
      return this.name;
    }
  }

  /**
   * constructor
   */
  public AVUser() {
    super(CLASS_NAME);
  }

  /**
   * 获取当前登录用户
   *
   * @return
   */
  public static AVUser currentUser() {
    return getCurrentUser();
  }

  /**
   * get user email.
   * @return
   */
  @JSONField(serialize = false)
  public String getEmail() {
    return (String) get(ATTR_EMAIL);
  }

  /**
   * set user email
   *
   * @param email
   */
  public void setEmail(String email) {
    put(ATTR_EMAIL, email);
  }

  /**
   * get user name.
   *
   * @return
   */
  @JSONField(serialize = false)
  public String getUsername() {
    return (String) get(ATTR_USERNAME);
  }

  /**
   * set user name.
   *
   * @param name
   */
  public void setUsername(String name) {
    put(ATTR_USERNAME, name);
  }

  /**
   * get user password.
   * @return
   */
  @JSONField(serialize = false)
  public String getPassword() {
    return (String) get(ATTR_PASSWORD);
  }

  /**
   * set user password.
   *
   * @param password
   */
  public void setPassword(String password) {
    put(ATTR_PASSWORD, password);
  }

  /**
   * get user mobilephone.
   *
   * @return
   */
  @JSONField(serialize = false)
  public String getMobilePhoneNumber() {
    return (String) get(ATTR_MOBILEPHONE);
  }

  /**
   * set user mobilephone.
   *
   * @param mobile
   */
  public void setMobilePhoneNumber(String mobile) {
    put(ATTR_MOBILEPHONE, mobile);
  }

  /**
   * whether user's mobilephone is verified or not.
   *
   * @return
   */
  @JSONField(serialize = false)
  public boolean isMobilePhoneVerified() {
    return getBoolean(ATTR_MOBILEPHONE_VERIFIED);
  }

  /**
   * get user session token.
   * if user not login, session token is null.
   *
   * @return
   */
  @JSONField(serialize = false)
  public String getSessionToken() {
    return (String)get(ATTR_SESSION_TOKEN);
  }

  /**
   * not use it!
   */
  public void internalChangeSessionToken(String token) {
    getServerData().put(ATTR_SESSION_TOKEN, token);
  }

  /**
   * whether user is authenticated or not.
   * @return
   */
  @JSONField(serialize = false)
  public boolean isAuthenticated() {
    // TODO: need to support thirdparty login.
    String sessionToken = getSessionToken();
    return !StringUtil.isEmpty(sessionToken);
  }

  /**
   * sign up(blocking).
   */
  public void signUp() {
    signUpInBackground().blockingSubscribe();
  }

  /**
   * sign up in background.
   * @return
   */
  public Observable<AVUser> signUpInBackground() {
    JSONObject paramData = generateChangedParam();
    logger.d("signup param: " + paramData.toJSONString());
    return PaasClient.getStorageClient().signUp(paramData).map(new Function<AVUser, AVUser>() {
      @Override
      public AVUser apply(AVUser avUser) throws Exception {
        AVUser.this.mergeRawData(avUser);
        return AVUser.this;
      }
    });
  }

  /**
   * signUpOrLoginByMobilePhone
   *
   * @param mobilePhoneNumber
   * @param smsCode
   * @return
   */
  public static AVUser signUpOrLoginByMobilePhone(String mobilePhoneNumber, String smsCode) {
    return signUpOrLoginByMobilePhone(mobilePhoneNumber, smsCode, AVUser.class);
  }

  /**
   * signUpOrLoginByMobilePhone
   * @param mobilePhoneNumber
   * @param smsCode
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T extends AVUser> T signUpOrLoginByMobilePhone(String mobilePhoneNumber, String smsCode, Class<T> clazz) {
    return signUpOrLoginByMobilePhoneInBackground(mobilePhoneNumber, smsCode, clazz).blockingSingle();
  }

  /**
   * signUpOrLoginByMobilePhoneInBackground
   * @param mobilePhoneNumber
   * @param smsCode
   * @return
   */
  public static Observable<AVUser> signUpOrLoginByMobilePhoneInBackground(String mobilePhoneNumber, String smsCode) {
    return signUpOrLoginByMobilePhoneInBackground(mobilePhoneNumber, smsCode, AVUser.class);
  }

  /**
   * signUpOrLoginByMobilePhoneInBackground
   *
   * @param mobilePhoneNumber
   * @param smsCode
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T extends AVUser> Observable<T> signUpOrLoginByMobilePhoneInBackground(String mobilePhoneNumber, String smsCode, Class<T> clazz) {
    if (StringUtil.isEmpty(mobilePhoneNumber)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "mobilePhoneNumber")));
    }
    if (StringUtil.isEmpty(smsCode)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "smsCode")));
    }
    if (null == clazz) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "clazz")));
    }
    Map<String, Object> params = createUserMap(null, null, null, mobilePhoneNumber, smsCode);
    JSONObject data = new JSONObject(params);
    return PaasClient.getStorageClient().signUpOrLoginByMobilephone(data, clazz);
  }

  /**
   * logIn in background
   *
   * @param username
   * @param password
   * @return
   */
  public static Observable<AVUser> logIn(String username, String password) {
    return logIn(username, password, AVUser.class);
  }

  public static Observable<AVUser> logInAnonymously() {
    String anonymousId = UUID.randomUUID().toString().toLowerCase();
    Map<String, Object> param = new HashMap<>();
    param.put("id", anonymousId);
    return loginWithAuthData(param, "anonymous");
  }

  /**
   * logIn in background
   *
   * @param username
   * @param password
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T extends AVUser> Observable<T> logIn(String username, String password, final Class<T> clazz) {
    Map<String, Object> params = createUserMap(username, password, null, null, null);
    JSONObject data = new JSONObject(params);
    return PaasClient.getStorageClient().logIn(data, clazz);
  }

  public static Observable<AVUser> loginByMobilePhoneNumber(String mobile, String password) {
    return loginByMobilePhoneNumber(mobile, password, AVUser.class);
  }

  public static <T extends AVUser> Observable<T> loginByMobilePhoneNumber(String mobile, String password, final Class<T> clazz) {
    Map<String, Object> params = createUserMap(null, password, null, mobile, null);
    JSONObject data = new JSONObject(params);
    return PaasClient.getStorageClient().logIn(data, clazz);
  }

  public static Observable<AVUser> loginBySMSCode(String mobile, String smsCode) {
    return loginBySMSCode(mobile, smsCode, AVUser.class);
  }

  public static <T extends AVUser> Observable<T> loginBySMSCode(String mobile, String smsCode, Class<T> clazz) {
    Map<String, Object> params = createUserMap(null, null, null, mobile, smsCode);
    JSONObject data = new JSONObject(params);
    return PaasClient.getStorageClient().logIn(data, clazz);
  }

  private static Map<String, Object> createUserMap(String username, String password, String email,
                                                   String phoneNumber, String smsCode) {
    Map<String, Object> map = new HashMap<String, Object>();

    if (StringUtil.isEmpty(username) && StringUtil.isEmpty(phoneNumber)) {
      throw new IllegalArgumentException("Blank username and blank mobile phone number");
    }
    if (!StringUtil.isEmpty(username)) {
      map.put(ATTR_USERNAME, username);
    }
    if (!StringUtil.isEmpty(password)) {
      map.put(ATTR_PASSWORD, password);
    }
    if (!StringUtil.isEmpty(email)) {
      map.put(ATTR_EMAIL, email);
    }
    if (!StringUtil.isEmpty(phoneNumber)) {
      map.put(ATTR_MOBILEPHONE, phoneNumber);
    }
    if (!StringUtil.isEmpty(smsCode)) {
      map.put(ATTR_SMSCODE, smsCode);
    }
    return map;
  }

  private static Map<String, String> createUserMapAFAP(String username, String password, String email,
                                                       String phoneNumber, String smsCode) {
    Map<String, String> map = new HashMap<String, String>();

    if (!StringUtil.isEmpty(username)) {
      map.put("username", username);
    }
    if (!StringUtil.isEmpty(password)) {
      map.put("password", password);
    }
    if (!StringUtil.isEmpty(email)) {
      map.put("email", email);
    }
    if (!StringUtil.isEmpty(phoneNumber)) {
      map.put("mobilePhoneNumber", phoneNumber);
    }
    if (!StringUtil.isEmpty(smsCode)) {
      map.put("smsCode", smsCode);
    }
    return map;
  }

  public static Observable<AVUser> loginWithAuthData(final Map<String, Object> authData, final String platform) {
    return loginWithAuthData(AVUser.class, authData, platform);
  }

  public static Observable<AVUser> loginWithAuthData(final Map<String, Object> authData, final String platform,
                                                                   final String unionId, final String unionIdPlatform, final boolean asMainAccount) {
    return loginWithAuthData(AVUser.class, authData, platform, unionId, unionIdPlatform, asMainAccount);
  }

  public static <T extends AVUser> Observable<T> loginWithAuthData(final Class<T> clazz, final Map<String, Object> authData, final String platform) {
    if (null == clazz) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "clazz")));
    }
    if (null == authData || authData.isEmpty()) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "authData")));
    }
    if (StringUtil.isEmpty(platform)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "platform")));
    }
    Map<String, Object> data = new HashMap<String, Object>();
    Map<String, Object> authMap = new HashMap<String, Object>();
    authMap.put(platform, authData);
    data.put(AUTHDATA_TAG, authMap);
    JSONObject param = new JSONObject(data);
    return PaasClient.getStorageClient().signUp(param).map(new Function<AVUser, T>() {
      @Override
      public T apply(AVUser avUser) throws Exception {
        T result = Transformer.transform(avUser, clazz);
        AVUser.changeCurrentUser(result, true);
        return result;
      }
    });
  }

  public static <T extends AVUser> Observable<T> loginWithAuthData(final Class<T> clazz, final Map<String, Object> authData, final String platform,
                                                                   final String unionId, final String unionIdPlatform, final boolean asMainAccount) {
    if (StringUtil.isEmpty(unionId)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "unionId")));
    }
    if (StringUtil.isEmpty(unionIdPlatform)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "unionIdPlatform")));
    }
    if (null == authData || authData.isEmpty()) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "authData")));
    }
    authData.put(AUTHDATA_ATTR_UNIONID, unionId);
    authData.put(AUTHDATA_ATTR_UNIONID_PLATFORM, unionIdPlatform);
    if (asMainAccount) {
      authData.put(AUTHDATA_ATTR_MAIN_ACCOUNT, asMainAccount);
    }
    return loginWithAuthData(clazz, authData, platform);
  }

  public Observable<AVUser> loginWithAuthData(final Map<String, Object> authData, final String platform,
                                              final boolean failOnNotExist) {
    if (null == authData || authData.isEmpty()) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "authData")));
    }
    if (StringUtil.isEmpty(platform)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "platform")));
    }

    Map<String, String> userData = createUserMapAFAP(getUsername(), null, getEmail(), getMobilePhoneNumber(), null);
    Map<String, Object> data = new HashMap<>();
    Map<String, Object> authMap = new HashMap<String, Object>();
    authMap.put(platform, authData);
    if(!userData.isEmpty()) {
      data.putAll(userData);
    }
    data.put(AUTHDATA_TAG, authMap);
    JSONObject param = new JSONObject(data);
    return PaasClient.getStorageClient().signUpWithFlag(param, failOnNotExist).map(new Function<AVUser, AVUser>() {
      @Override
      public AVUser apply(AVUser avUser) throws Exception {
        AVUser.this.resetByRawData(avUser);
        AVUser.changeCurrentUser(AVUser.this, true);
        return AVUser.this;
      }
    });
  }

  public Observable<AVUser> loginWithAuthData(final Map<String, Object> authData, final String platform,
                                              final String unionId, final String unionIdPlatform,
                                              final boolean asMainAccount, final boolean failOnNotExist) {
    if (null == authData || authData.isEmpty()) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "authData")));
    }
    if (StringUtil.isEmpty(unionId)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "unionId")));
    }
    if (StringUtil.isEmpty(unionIdPlatform)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "unionIdPlatform")));
    }
    authData.put(AUTHDATA_ATTR_UNIONID, unionId);
    authData.put(AUTHDATA_ATTR_UNIONID_PLATFORM, unionIdPlatform);
    if (asMainAccount) {
      authData.put(AUTHDATA_ATTR_MAIN_ACCOUNT, asMainAccount);
    }
    return loginWithAuthData(authData, platform, failOnNotExist);
  }

  public Observable<AVUser> associateWithAuthData(Map<String, Object> authData, String platform) {
    if (null == authData || authData.isEmpty()) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "authData")));
    }
    if (StringUtil.isEmpty(platform)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "platform")));
    }
    Map<String, Object> authDataAttr = new HashMap<String, Object>();
    authDataAttr.put(platform, authData);
    Object existedAuthData = this.get(AUTHDATA_TAG);
    if (existedAuthData instanceof Map) {
      authDataAttr.putAll((Map<String, Object>)existedAuthData);
    }
    this.put(AUTHDATA_TAG, authDataAttr);
    return (Observable<AVUser>) saveInBackground();
  }

  public Observable<AVUser> associateWithAuthData(Map<String, Object> authData, String platform, String unionId, String unionIdPlatform,
                                                  boolean asMainAccount) {
    if (null == authData || authData.isEmpty()) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "authData")));
    }
    if (StringUtil.isEmpty(unionId)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "unionId")));
    }
    if (StringUtil.isEmpty(unionIdPlatform)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "unionIdPlatform")));
    }
    authData.put(AUTHDATA_ATTR_UNIONID, unionId);
    authData.put(AUTHDATA_ATTR_UNIONID_PLATFORM, unionIdPlatform);
    if (asMainAccount) {
      authData.put(AUTHDATA_ATTR_MAIN_ACCOUNT, true);
    }
    return this.associateWithAuthData(authData, platform);
  }

  public Observable<AVUser> dissociateWithAuthData(final String platform) {
    if (StringUtil.isEmpty(platform)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "platform")));
    }

    String objectId = getObjectId();
    if (StringUtil.isEmpty(objectId) || !isAuthenticated()) {
      return Observable.error(new AVException(AVException.SESSION_MISSING,
              "the user object missing a valid session"));
    }
    this.remove(AUTHDATA_TAG + "." + platform);
    return this.saveInBackground().map(new Function<AVObject, AVUser>() {
      public AVUser apply(@NonNull AVObject var1) throws Exception {
        Map<String, Object> authData = (Map<String, Object>) AVUser.this.get(AUTHDATA_TAG);
        if (authData != null) {
          authData.remove(platform);
        }
        return AVUser.this;
      }
    });
  }

  /**
   * Session token operations
   */

  public Observable<Boolean> checkAuthenticatedInBackground() {
    String sessionToken = getSessionToken();
    if (StringUtil.isEmpty(sessionToken)) {
      logger.d("sessionToken is not existed.");
      return Observable.just(false);
    }
    return PaasClient.getStorageClient().checkAuthenticated(sessionToken);
  }

  public Observable<Boolean> refreshSessionTokenInBackground() {
    return PaasClient.getStorageClient().refreshSessionToken(this);
  }

  public static Observable<AVUser> becomeWithSessionTokenInBackground(String sessionToken) {
    return becomeWithSessionTokenInBackground(sessionToken, AVUser.class);
  }

  public static <T extends AVUser> Observable<T> becomeWithSessionTokenInBackground(String sessionToken, Class<T> clazz) {
    return PaasClient.getStorageClient().createUserBySession(sessionToken, clazz);
  }

  public static void logOut() {
    AVUser.changeCurrentUser(null, true);
  }

  /**
   * User Query
   */
  public static <T extends AVUser> AVQuery<T> getUserQuery(Class<T> clazz) {
    return new AVQuery<T>(CLASS_NAME, clazz);
  }

  public static AVQuery<AVUser> getQuery() {
    return getQuery(AVUser.class);
  }

  public Observable<List<AVRole>> getRolesInBackground() {
    AVQuery<AVRole> roleQuery = new AVQuery<AVRole>(AVRole.CLASS_NAME);
    roleQuery.whereEqualTo("users", this);
    return roleQuery.findInBackground();
  }

  /**
   * Current User Cache
   */
  transient private static boolean enableAutomatic = false;

  public static void enableAutomaticUser() {
    enableAutomatic = true;
  }

  @JSONField(serialize = false)
  public static boolean isEnableAutomatic() {
    return enableAutomatic;
  }

  public static void disableAutomaticUser() {
    enableAutomatic = false;
  }

  private static File currentUserArchivePath() {
    return new File(AppConfiguration.getDocumentDir() + "/currentUser");
  }

  private static boolean userArchiveExist() {
    return currentUserArchivePath().exists();
  }

  public static synchronized void changeCurrentUser(AVUser newUser, boolean save) {
    if (null != newUser) {
      newUser.setPassword(null);
    }
    File currentUserArchivePath = currentUserArchivePath();
    if (null != newUser && save) {
      String jsonString = JSON.toJSONString(newUser, ObjectValueFilter.instance,
              SerializerFeature.WriteClassName,
              SerializerFeature.DisableCircularReferenceDetect);
      logger.d(jsonString);
      PersistenceUtil.sharedInstance().saveContentToFile(jsonString, currentUserArchivePath);
    } else if (save) {
      PersistenceUtil.sharedInstance().removeLock(currentUserArchivePath.getAbsolutePath());
      boolean deleteRst = currentUserArchivePath.delete();
      if (!deleteRst) {
        logger.w("failed to delete currentUser cache file.");
      }
    }
    PaasClient.getStorageClient().setCurrentUser(newUser);
  }

  public static AVUser getCurrentUser() {
    return getCurrentUser(AVUser.class);
  }

  public static <T extends AVUser> T getCurrentUser(Class<T> userClass) {
    AVUser user = PaasClient.getStorageClient().getCurrentUser();
    if (null != user && userClass.isAssignableFrom(user.getClass())) {
      return (T) user;
    } else if (userArchiveExist()) {
      File currentUserArchivePath = currentUserArchivePath();
      synchronized (AVUser.class) {
        String jsonString = PersistenceUtil.sharedInstance().readContentFromFile(currentUserArchivePath);
        if (!StringUtil.isEmpty(jsonString)) {
          if (jsonString.indexOf("@type") >= 0) {
            // new version.
            try {
              T newUser = userClass.newInstance();
              Map<String, Object> jsonData = JSON.parseObject(jsonString, Map.class);
              Object serverDataString = jsonData.get("serverData");
              if (serverDataString instanceof String) {
                Map<String, Object> rawData = JSON.parseObject((String)serverDataString, Map.class);
                rawData.remove("@type");
                newUser.resetServerData(rawData);
              } else {
                newUser.resetServerData(jsonData);
                changeCurrentUser(newUser, true);
              }
              user = newUser;
              PaasClient.getStorageClient().setCurrentUser(user);
            } catch (Exception ex) {
              logger.w("failed to deserialize AVUser instance.", ex);
            }
          } else {
            // older format
            try {
              T newUser = userClass.newInstance();
              Map<String, Object> rawData = JSON.parseObject(jsonString, Map.class);
              newUser.resetServerData(rawData);
              changeCurrentUser(newUser, true);
              user = newUser;
            } catch (Exception ex) {
              logger.w(ex);
            }
          }
        }
      }
    }
    if (enableAutomatic && null == user) {
      try {
        user = userClass.newInstance();
        changeCurrentUser(user, true);
      } catch (Exception ex) {
        logger.w(ex);
      }
    }
    return (T) user;
  }

  /**
   * Password-relative operations
   */
  public static Observable<AVNull> requestPasswordResetInBackground(String email) {
    return PaasClient.getStorageClient().requestResetPassword(email);
  }

  public static Observable<AVNull> requestPasswordResetBySmsCodeInBackground(String phoneNumber) {
    return requestPasswordResetBySmsCodeInBackground(phoneNumber, null);
  }

  public static Observable<AVNull> requestPasswordResetBySmsCodeInBackground(String phoneNumber, String validateToken) {
    return PaasClient.getStorageClient().requestResetPasswordBySmsCode(phoneNumber, validateToken);
  }

  public static Observable<AVNull> resetPasswordBySmsCodeInBackground(String smsCode, String newPassword) {
    return PaasClient.getStorageClient().resetPasswordBySmsCode(smsCode, newPassword);
  }
  public Observable<AVNull> updatePasswordInBackground(String oldPass, String newPass) {
    return PaasClient.getStorageClient().updatePassword(this, oldPass, newPass);
  }

  public static Observable<AVNull> requestEmailVerifyInBackground(String email) {
    return PaasClient.getStorageClient().requestEmailVerify(email);
  }

  public static Observable<AVNull> requestMobilePhoneVerifyInBackground(String mobilePhone) {
    return requestMobilePhoneVerifyInBackground(mobilePhone, null);
  }
  public static Observable<AVNull> requestMobilePhoneVerifyInBackground(String mobilePhone, String validateToken) {
    return PaasClient.getStorageClient().requestMobilePhoneVerify(mobilePhone, validateToken);
  }

  public static Observable<AVNull> requestLoginSmsCodeInBackground(String phoneNumber) {
    return requestLoginSmsCodeInBackground(phoneNumber, null);
  }
  public static Observable<AVNull> requestLoginSmsCodeInBackground(String phoneNumber, String validateToken) {
    return PaasClient.getStorageClient().requestLoginSmsCode(phoneNumber, validateToken);
  }
  public static Observable<AVNull> verifyMobilePhoneInBackground(String verifyCode) {
    return PaasClient.getStorageClient().verifyMobilePhone(verifyCode);
  }

  /**
   * follow-relative opersations
   */
  public Observable<JSONObject> followInBackground(String userObjectId) {
    return this.followInBackground(userObjectId, null);
  }
  public Observable<JSONObject> followInBackground(String userObjectId, Map<String, Object> attributes) {
    return PaasClient.getStorageClient().followUser(getObjectId(), userObjectId, attributes);
  }
  public Observable<JSONObject> unfollowInBackground(String userObjectId) {
    return PaasClient.getStorageClient().unfollowUser(getObjectId(), userObjectId);
  }
  public <T extends AVUser> AVQuery<T> followerQuery(Class<T> clazz) {
    return AVUser.followerQuery(getObjectId(), clazz);
  }
  public <T extends AVUser> AVQuery<T> followeeQuery(Class<T> clazz) {
    return AVUser.followeeQuery(getObjectId(), clazz);
  }
  public static <T extends AVUser> AVQuery<T> followerQuery(final String userObjectId,
                                                            Class<T> clazz) {
    if (StringUtil.isEmpty(userObjectId)) {
      throw new IllegalArgumentException("Blank user objectId");
    }
    AVFellowshipQuery query = new AVFellowshipQuery<T>("_Follower", clazz);
    query.whereEqualTo("user", AVUser.createWithoutData(CLASS_NAME, userObjectId));
    query.setFriendshipTag("follower");
    return query;
  }
  public static <T extends AVUser> AVQuery<T> followeeQuery(final String userObjectId, Class<T> clazz) {
    if (StringUtil.isEmpty(userObjectId)) {
      throw new IllegalArgumentException("Blank user objectId");
    }
    AVFellowshipQuery query = new AVFellowshipQuery<T>("_Followee", clazz);
    query.whereEqualTo("user", AVUser.createWithoutData(CLASS_NAME, userObjectId));
    query.setFriendshipTag("followee");
    return query;
  }

  /**
   * third-party login methods.
   */

}
