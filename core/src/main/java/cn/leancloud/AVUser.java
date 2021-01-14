package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.callback.FollowersAndFolloweesCallback;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.core.PaasClient;
import cn.leancloud.gson.ObjectDeserializer;
import cn.leancloud.ops.Utils;
import cn.leancloud.sms.AVSMS;
import cn.leancloud.sms.AVSMSOption;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.ErrorUtils;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

import java.io.File;
import java.util.*;

// TODO: need transfer Anonymous User/Common User

@AVClassName("_User")
public class AVUser extends AVObject {
  public static final String ATTR_USERNAME = "username";
  private static final String ATTR_PASSWORD = "password";
  public static final String ATTR_EMAIL = "email";
  public static final String ATTR_MOBILEPHONE = "mobilePhoneNumber";
  private static final String ATTR_SMSCODE = "smsCode";
  private static final String ATTR_MOBILEPHONE_VERIFIED = "mobilePhoneVerified";
  public static final String ATTR_SESSION_TOKEN = "sessionToken";

  private static final String PARAM_ATTR_FRIENDSHIP = "friendship";

  private static final String AUTHDATA_TAG = "authData";
  private static final String AUTHDATA_PLATFORM_ANONYMOUS = "anonymous";

  private static final String AUTHDATA_ATTR_UNIONID = "unionid";
  private static final String AUTHDATA_ATTR_UNIONID_PLATFORM = "platform";
  private static final String AUTHDATA_ATTR_MAIN_ACCOUNT = "main_account";

  private static final String ILLEGALARGUMENT_MSG_FORMAT = "illegal parameter. %s must not null/empty.";

  public static final String CLASS_NAME = "_User";
  public static final String FOLLOWER_TAG = "follower";
  public static final String FOLLOWEE_TAG = "followee";

  private static Class<? extends AVUser> subClazz = null;

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
   * @return current user.
   */
  public static AVUser currentUser() {
    return getCurrentUser();
  }

  /**
   * get user email.
   * @return user email.
   */
  public String getEmail() {
    return (String) get(ATTR_EMAIL);
  }

  /**
   * set user email
   *
   * @param email user email.
   */
  public void setEmail(String email) {
    put(ATTR_EMAIL, email);
  }

  /**
   * get user name.
   *
   * @return user name
   */
  public String getUsername() {
    return (String) get(ATTR_USERNAME);
  }

  /**
   * set user name.
   *
   * @param name username
   */
  public void setUsername(String name) {
    put(ATTR_USERNAME, name);
  }

  /**
   * get user password.
   * @return user password.
   */
  public String getPassword() {
    return (String) get(ATTR_PASSWORD);
  }

  /**
   * set user password.
   *
   * @param password user password.
   */
  public void setPassword(String password) {
    put(ATTR_PASSWORD, password);
  }

  /**
   * get user mobilephone.
   *
   * @return user mobilephone number.
   */
  public String getMobilePhoneNumber() {
    return (String) get(ATTR_MOBILEPHONE);
  }

  /**
   * set user mobilephone.
   *
   * @param mobile user mobilephone number.
   */
  public void setMobilePhoneNumber(String mobile) {
    put(ATTR_MOBILEPHONE, mobile);
  }

  /**
   * whether user's mobilephone is verified or not.
   *
   * @return flag to indicate user's mobilephone is verified or not
   */
  public boolean isMobilePhoneVerified() {
    return getBoolean(ATTR_MOBILEPHONE_VERIFIED);
  }

  /**
   * get user session token.
   * if user not login, session token is null.
   *
   * @return user session token, null if not login.
   */
  public String getSessionToken() {
    return (String)get(ATTR_SESSION_TOKEN);
  }

  /**
   * not use it!
   * @param token user token.
   */
  public void internalChangeSessionToken(String token) {
    getServerData().put(ATTR_SESSION_TOKEN, token);
  }

  /**
   * whether user is authenticated or not.
   * @return flag to indicate user is authenticated or not.
   */
  public boolean isAuthenticated() {
    // TODO: need to support thirdparty login.
    String sessionToken = getSessionToken();
    return !StringUtil.isEmpty(sessionToken);
  }

  private void updateCurrentUserCache() {
    String sessionToken = getSessionToken();
    AVUser currentUser = AVUser.currentUser();
    if (null != currentUser && !StringUtil.isEmpty(currentUser.getObjectId())
            && currentUser.getObjectId().equals(getObjectId()) && !StringUtil.isEmpty(sessionToken)) {
      changeCurrentUser(this, true);
    }
  }

  @Override
  protected void onSaveSuccess() {
    super.onSaveSuccess();
    updateCurrentUserCache();
  }

  @Override
  protected void onSaveFailure() {
    super.onSaveFailure();
  }

  @Override
  protected void onDataSynchronized() {
    super.onDataSynchronized();
    updateCurrentUserCache();
  }

  /**
   * Whether is anonymous or not.
   * @return flag to indicate current user is anonymous or not.
   */
  public boolean isAnonymous() {
    JSONObject existedAuthData = this.getJSONObject(AUTHDATA_TAG);
    if (existedAuthData != null) {
      if (existedAuthData.size() == 1 && existedAuthData.containsKey(AUTHDATA_PLATFORM_ANONYMOUS)) {
        return true;
      }
    }
    return false;
  }

  /**
   * sign up(blocking).
   */
  public void signUp() {
    signUpInBackground().blockingSubscribe();
  }

  /**
   * sign up in background.
   * @return observable instance.
   */
  public Observable<AVUser> signUpInBackground() {
    JSONObject paramData = generateChangedParam();
    logger.d("signup param: " + paramData.toJSONString());
    return PaasClient.getStorageClient().signUp(paramData).map(new Function<AVUser, AVUser>() {
      @Override
      public AVUser apply(AVUser avUser) throws Exception {
        AVUser.this.mergeRawData(avUser, true);
        AVUser.this.onSaveSuccess();
        return AVUser.this;
      }
    });
  }

  /**
   * signUpOrLoginByMobilePhone
   *
   * @param mobilePhoneNumber mobile phone
   * @param smsCode sms code
   * @return user instance.
   */
  public static AVUser signUpOrLoginByMobilePhone(String mobilePhoneNumber, String smsCode) {
    return signUpOrLoginByMobilePhone(mobilePhoneNumber, smsCode, internalUserClazz());
  }

  /**
   * signUpOrLoginByMobilePhone
   * @param mobilePhoneNumber mobile phone number
   * @param smsCode sms code
   * @param clazz class name
   * @param <T> template type.
   * @return user instance.
   */
  public static <T extends AVUser> T signUpOrLoginByMobilePhone(String mobilePhoneNumber, String smsCode, Class<T> clazz) {
    return signUpOrLoginByMobilePhoneInBackground(mobilePhoneNumber, smsCode, clazz).blockingSingle();
  }

  /**
   * signUpOrLoginByMobilePhoneInBackground
   * @param mobilePhoneNumber mobile phone number.
   * @param smsCode sms code
   * @return observable instance.
   */
  public static Observable<? extends AVUser> signUpOrLoginByMobilePhoneInBackground(String mobilePhoneNumber, String smsCode) {
    return signUpOrLoginByMobilePhoneInBackground(mobilePhoneNumber, smsCode, internalUserClazz());
  }

  /**
   * signUpOrLoginByMobilePhoneInBackground
   *
   * @param mobilePhoneNumber mobile phone number
   * @param smsCode sms code
   * @param clazz class name
   * @param <T> template type.
   * @return observable instance.
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
    JSONObject data = JSONObject.Builder.create(params);
    return PaasClient.getStorageClient().signUpOrLoginByMobilephone(data, clazz);
  }

  /**
   * logIn in background
   *
   * @param username username
   * @param password user password
   * @return observable instance.
   */
  public static Observable<? extends AVUser> logIn(String username, String password) {
    return logIn(username, password, internalUserClazz());
  }

  public static Observable<? extends AVUser> logInAnonymously() {
    String anonymousId = UUID.randomUUID().toString().toLowerCase();
    Map<String, Object> param = new HashMap<>();
    param.put("id", anonymousId);
    return loginWithAuthData(param, AUTHDATA_PLATFORM_ANONYMOUS);
  }

  /**
   * logIn in background
   *
   * @param username username
   * @param password user password
   * @param clazz user class name
   * @param <T> template type.
   * @return observable instance.
   */
  public static <T extends AVUser> Observable<T> logIn(String username, String password, final Class<T> clazz) {
    Map<String, Object> params = createUserMap(username, password, null, null, null);
    JSONObject data = JSONObject.Builder.create(params);
    return PaasClient.getStorageClient().logIn(data, clazz);
  }

  /**
   * logIn with mobile phone and password.
   * @param mobile mobile phone
   * @param password password
   * @return observable instance.
   */
  public static Observable<? extends AVUser> loginByMobilePhoneNumber(String mobile, String password) {
    return loginByMobilePhoneNumber(mobile, password, internalUserClazz());
  }

  /**
   * logIn with email and password
   * @param email email.
   * @param password password.
   * @return observable instance.
   */
  public static Observable<? extends AVUser> loginByEmail(String email, String password) {
    HashMap<String, Object> params = createUserMapAFAP(null, password, email, null, null);
    return PaasClient.getStorageClient().logIn(JSONObject.Builder.create(params), internalUserClazz());
  }

  /**
   * logIn with mobile phone and password.
   * @param mobile mobile phone.
   * @param password user password.
   * @param clazz user class.
   * @param <T> template type.
   * @return observable instance.
   */
  public static <T extends AVUser> Observable<T> loginByMobilePhoneNumber(String mobile, String password, final Class<T> clazz) {
    Map<String, Object> params = createUserMap(null, password, null, mobile, null);
    JSONObject data = JSONObject.Builder.create(params);
    return PaasClient.getStorageClient().logIn(data, clazz);
  }

  /**
   * logIn with mobile phone and sms code.
   * @param mobile mobile phone.
   * @param smsCode sms code.
   * @return observable instance.
   */
  public static Observable<? extends AVUser> loginBySMSCode(String mobile, String smsCode) {
    return loginBySMSCode(mobile, smsCode, internalUserClazz());
  }

  /**
   * logIn with mobile phone and sms code.
   * @param mobile mobile phone.
   * @param smsCode sms code.
   * @param clazz user class.
   * @param <T> template type.
   * @return observable instance.
   */
  public static <T extends AVUser> Observable<T> loginBySMSCode(String mobile, String smsCode, Class<T> clazz) {
    Map<String, Object> params = createUserMap(null, null, null, mobile, smsCode);
    JSONObject data = JSONObject.Builder.create(params);
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

  private static HashMap<String, Object> createUserMapAFAP(String username, String password, String email,
                                                       String phoneNumber, String smsCode) {
    HashMap<String, Object> map = new HashMap<String, Object>();

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

  /**
   * third-party login methods.
   */

  /**
   * login with auth data.
   * @param authData auth data.
   * @param platform platform string.
   * @return observable instance.
   */
  public static Observable<? extends AVUser> loginWithAuthData(final Map<String, Object> authData, final String platform) {
    return loginWithAuthData(internalUserClazz(), authData, platform);
  }

  /**
   * login with auth data.
   * @param authData auth data.
   * @param platform platform string.
   * @param unionId unionid.
   * @param unionIdPlatform unionid platform string.
   * @param asMainAccount flag to treat as main account.
   * @return observable instance.
   */
  public static Observable<? extends AVUser> loginWithAuthData(final Map<String, Object> authData, final String platform,
                                                                   final String unionId, final String unionIdPlatform, final boolean asMainAccount) {
    return loginWithAuthData(internalUserClazz(), authData, platform, unionId, unionIdPlatform, asMainAccount);
  }

  /**
   * login with auth data.
   * @param authData auth data.
   * @param platform platform string.
   * @param clazz user class name.
   * @param <T> template type.
   * @return observable instance.
   */
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
    JSONObject param = JSONObject.Builder.create(data);
    return PaasClient.getStorageClient().signUp(param).map(new Function<AVUser, T>() {
      @Override
      public T apply(AVUser avUser) throws Exception {
        T result = Transformer.transform(avUser, clazz);
        AVUser.changeCurrentUser(result, true);
        return result;
      }
    });
  }

  /**
   * login with auth data.
   * @param authData auth data.
   * @param platform platform string.
   * @param unionId unionid.
   * @param unionIdPlatform unionid platform string.
   * @param asMainAccount flag to treat as main account.
   * @param clazz user class name.
   * @param <T> template type.
   * @return observable instance.
   */
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

  /**
   * login with auth data.
   * @param authData auth data.
   * @param platform platform string.
   * @param failOnNotExist flag to indicate to exit if failed or not.
   * @return observable instance.
   */
  public Observable<AVUser> loginWithAuthData(final Map<String, Object> authData, final String platform,
                                              final boolean failOnNotExist) {
    if (null == authData || authData.isEmpty()) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "authData")));
    }
    if (StringUtil.isEmpty(platform)) {
      return Observable.error(new IllegalArgumentException(String.format(ILLEGALARGUMENT_MSG_FORMAT, "platform")));
    }

    HashMap<String, Object> data = createUserMapAFAP(getUsername(), null, getEmail(), getMobilePhoneNumber(), null);
    Map<String, Object> authMap = new HashMap<String, Object>();
    authMap.put(platform, authData);
    data.put(AUTHDATA_TAG, authMap);
    JSONObject param = JSONObject.Builder.create(data);
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
    return (Observable<AVUser>) saveInBackground(new AVSaveOption().setFetchWhenSave(true));
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
   * @return observable instance.
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

  public static AVUser becomeWithSessionToken(String sessionToken) {
    AVUser usr = becomeWithSessionTokenInBackground(sessionToken).blockingFirst();
    return usr;
  }

  public static Observable<? extends AVUser> becomeWithSessionTokenInBackground(String sessionToken) {
    return becomeWithSessionTokenInBackground(sessionToken, internalUserClazz());
  }

  public static <T extends AVUser> T becomeWithSessionToken(String sessionToken, Class<T> clazz) {
    T result = becomeWithSessionTokenInBackground(sessionToken, clazz).blockingFirst();
    return result;
  }

  public static <T extends AVUser> Observable<T> becomeWithSessionTokenInBackground(String sessionToken, Class<T> clazz) {
    return PaasClient.getStorageClient().createUserBySession(sessionToken, clazz).map(new Function<T, T>() {
      @Override
      public T apply(T result) throws Exception {
        AVUser.changeCurrentUser(result, true);
        return result;
      }
    });
  }

  public static void logOut() {
    AVUser.changeCurrentUser(null, true);
  }

  /**
   * Get User Query
   * @param clazz class name.
   * @param <T> template type.
   * @return query instance.
   */
  public static <T extends AVUser> AVQuery<T> getUserQuery(Class<T> clazz) {
    return new AVQuery<T>(CLASS_NAME, clazz);
  }

  /**
   * Get User Query
   * @return query instance.
   */
  public static AVQuery<AVUser> getQuery() {
    return getQuery(AVUser.class);
  }

  /**
   * Get roles in background.
   * @return observable instance.
   */
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

  //@JSONField(serialize = false)
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
      newUser.removeOperationForKey(ATTR_PASSWORD);
    }
    File currentUserArchivePath = currentUserArchivePath();
    if (null != newUser && save) {
      String jsonString = newUser.toJSONString();
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
    return getCurrentUser(internalUserClazz());
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
          if (jsonString.indexOf("@type") >= 0 || jsonString.indexOf(ObjectDeserializer.KEY_VERSION) >= 0) {
            // new version.
            try {
              user = (AVUser) AVObject.parseAVObject(jsonString);;
              PaasClient.getStorageClient().setCurrentUser(user);
            } catch (Exception ex) {
              logger.w("failed to deserialize AVUser instance.", ex);
            }
          } else {
            // older format
            try {
              T newUser = userClass.newInstance();
              Map<String, Object> rawData = JSON.parseObject(jsonString, Map.class);
              if (rawData.containsKey("serverData") || rawData.get("serverData") instanceof Map) {
                newUser.resetServerData((Map<String, Object>)rawData.get("serverData"));
              } else {
                newUser.resetServerData(rawData);
              }

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
    return (T) Transformer.transform(user, userClass);
  }

  /**
   * Password-relative operations
   * @param email user email.
   * @return observable instance.
   */
  public static Observable<AVNull> requestPasswordResetInBackground(String email) {
    return PaasClient.getStorageClient().requestResetPassword(email);
  }

  /**
   * request sms code for resetting password
   * @param phoneNumber mobile phone number
   * @return observable instance
   */
  public static Observable<AVNull> requestPasswordResetBySmsCodeInBackground(String phoneNumber) {
    return requestPasswordResetBySmsCodeInBackground(phoneNumber, null);
  }

  /**
   * request sms code for resetting password, collaborating with AVCaptcha
   * @param phoneNumber mobile phone number
   * @param validateToken validated token, retrieved after invoking AVCaptcha#verifyCaptchaCodeInBackground
   * @return observable instance
   */
  public static Observable<AVNull> requestPasswordResetBySmsCodeInBackground(String phoneNumber, String validateToken) {
    return PaasClient.getStorageClient().requestResetPasswordBySmsCode(phoneNumber, validateToken);
  }

  /**
   * reset password with sms code for current user.
   * @param smsCode sms code
   * @param newPassword new password
   * @return observable instance
   */
  public static Observable<AVNull> resetPasswordBySmsCodeInBackground(String smsCode, String newPassword) {
    return PaasClient.getStorageClient().resetPasswordBySmsCode(smsCode, newPassword);
  }

  /**
   * update current user's password
   * @param oldPass old password
   * @param newPass new password
   * @return observable instance
   */
  public Observable<AVNull> updatePasswordInBackground(String oldPass, String newPass) {
    return PaasClient.getStorageClient().updatePassword(this, oldPass, newPass);
  }

  /**
   * request verified email
   * @param email email address
   * @return observable instance
   */
  public static Observable<AVNull> requestEmailVerifyInBackground(String email) {
    return PaasClient.getStorageClient().requestEmailVerify(email);
  }

  /**
   * request sms code for verification mobile phone.
   * @param mobilePhone mobile phone number.
   * @return observable instance
   */
  public static Observable<AVNull> requestMobilePhoneVerifyInBackground(String mobilePhone) {
    if (StringUtil.isEmpty(mobilePhone) || !AVSMS.checkMobilePhoneNumber(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("mobile phone number is empty or invalid"));
    }
    return requestMobilePhoneVerifyInBackground(mobilePhone, null);
  }

  /**
   * request sms code for verification mobile phone, collaborating with AVCaptcha
   * @param mobilePhone mobile phone number.
   * @param validateToken validated token, retrieved after invoking AVCaptcha#verifyCaptchaCodeInBackground
   * @return observable instance.
   */
  public static Observable<AVNull> requestMobilePhoneVerifyInBackground(String mobilePhone, String validateToken) {
    if (StringUtil.isEmpty(mobilePhone) || !AVSMS.checkMobilePhoneNumber(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("mobile phone number is empty or invalid"));
    }
    return PaasClient.getStorageClient().requestMobilePhoneVerify(mobilePhone, validateToken);
  }

  /**
   * request sms code for login
   * @param mobilePhone mobile phone number.
   * @return observable instance
   */
  public static Observable<AVNull> requestLoginSmsCodeInBackground(String mobilePhone) {
    if (StringUtil.isEmpty(mobilePhone) || !AVSMS.checkMobilePhoneNumber(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("mobile phone number is empty or invalid"));
    }
    return requestLoginSmsCodeInBackground(mobilePhone, null);
  }

  /**
   * request sms code for login, collaborating with AVCaptcha
   * @param mobilePhone mobile phone number
   * @param validateToken validated token, retrieved after invoking AVCaptcha#verifyCaptchaCodeInBackground
   * @return observable instance.
   */
  public static Observable<AVNull> requestLoginSmsCodeInBackground(String mobilePhone, String validateToken) {
    if (StringUtil.isEmpty(mobilePhone) || !AVSMS.checkMobilePhoneNumber(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("mobile phone number is empty or invalid"));
    }
    return PaasClient.getStorageClient().requestLoginSmsCode(mobilePhone, validateToken);
  }

  /**
   * verify sms code with current user's phone number.
   * @param verifyCode sms code
   * @return observable instance.
   */
  public static Observable<AVNull> verifyMobilePhoneInBackground(String verifyCode) {
    return PaasClient.getStorageClient().verifyMobilePhone(verifyCode);
  }

  /**
   * request sms code for updating phone number of current user.
   * @param mobilePhone mobile phone number.
   * @param option sms option
   * @return observable instance
   */
  public static Observable<AVNull> requestSMSCodeForUpdatingPhoneNumberInBackground(String mobilePhone, AVSMSOption option) {
    if (StringUtil.isEmpty(mobilePhone) || !AVSMS.checkMobilePhoneNumber(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("mobile phone number is empty or invalid"));
    }
    Map<String, Object> param = (null == option)? new HashMap<String, Object>() : option.getOptionMap();
    return PaasClient.getStorageClient().requestSMSCodeForUpdatingPhoneNumber(mobilePhone, param);
  }

  /**
   * verify sms code for updating phone number of current user.
   * @param code    sms code
   * @param mobilePhone mobile phone number.
   * @return observable instance
   */
  public static Observable<AVNull> verifySMSCodeForUpdatingPhoneNumberInBackground(String code, String mobilePhone) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("code or mobilePhone is empty"));
    }
    return PaasClient.getStorageClient().verifySMSCodeForUpdatingPhoneNumber(code, mobilePhone);
  }

  private boolean checkUserAuthentication(final AVCallback callback) {
    if (!this.isAuthenticated() || StringUtil.isEmpty(getObjectId())) {
      if (callback != null) {
        callback.internalDone(ErrorUtils.propagateException(AVException.SESSION_MISSING,
                "No valid session token, make sure signUp or login has been called."));
      }
      return false;
    }
    return true;
  }

  /**
   * follow-relative opersations
   * @param userObjectId  user objectId.
   * @return observable instance.
   */
  public Observable<JSONObject> followInBackground(String userObjectId) {
    return this.followInBackground(userObjectId, new HashMap<String, Object>());
  }

  public Observable<JSONObject> followInBackground(String userObjectId, Map<String, Object> attributes) {
    if (!checkUserAuthentication(null)) {
      return Observable.error(ErrorUtils.propagateException(AVException.SESSION_MISSING,
              "No valid session token, make sure signUp or login has been called."));
    }
    return PaasClient.getStorageClient().followUser(getObjectId(), userObjectId, attributes);
  }

  public Observable<JSONObject> unfollowInBackground(String userObjectId) {
    if (!checkUserAuthentication(null)) {
      return Observable.error(ErrorUtils.propagateException(AVException.SESSION_MISSING,
              "No valid session token, make sure signUp or login has been called."));
    }
    return PaasClient.getStorageClient().unfollowUser(getObjectId(), userObjectId);
  }

  public AVQuery<AVObject> followerQuery() {
    return AVUser.followerQuery(getObjectId(), AVObject.class);
  }

  public AVQuery<AVObject> followeeQuery() {
    return AVUser.followeeQuery(getObjectId(), AVObject.class);
  }

  public static <T extends AVObject> AVQuery<T> followerQuery(final String userObjectId,
                                                            Class<T> clazz) {
    if (StringUtil.isEmpty(userObjectId)) {
      throw new IllegalArgumentException("Blank user objectId");
    }
    AVQuery<T> query = new AVQuery<>("_Follower", clazz);
    query.whereEqualTo("user", AVUser.createWithoutData(CLASS_NAME, userObjectId));
    query.include("follower");
    return query;
  }

  public static <T extends AVObject> AVQuery<T> followeeQuery(final String userObjectId, Class<T> clazz) {
    if (StringUtil.isEmpty(userObjectId)) {
      throw new IllegalArgumentException("Blank user objectId");
    }
    AVQuery<T> query = new AVQuery<>("_Followee", clazz);
    query.whereEqualTo("user", AVUser.createWithoutData(CLASS_NAME, userObjectId));
    query.include("followee");
    return query;
  }

  /**
   * get friendship query of current user.
   *
   * @param isFollowerDirection query direction:
   *                            true - query follower of current user(users which followed current user).
   *                            false - query followee of current user(users which current user followed).
   * @return query instance, null for non-authenticated user.
   */
  public AVQuery<AVFriendship> friendshipQuery(boolean isFollowerDirection) {
    String userObjectId = getObjectId();
    if (StringUtil.isEmpty(userObjectId)) {
      logger.d("user object id is empty.");
      return null;
    }
    AVQuery<AVFriendship> query = new AVQuery<>(AVFriendship.CLASS_NAME);
    if (isFollowerDirection) {
      query.whereEqualTo(AVFriendship.ATTR_FOLLOWEE, AVUser.createWithoutData(CLASS_NAME, userObjectId));
      query.include(AVFriendship.ATTR_USER);
    } else {
      query.whereEqualTo(AVFriendship.ATTR_USER, AVUser.createWithoutData(CLASS_NAME, userObjectId));
      query.include(AVFriendship.ATTR_FOLLOWEE);
    }
    return query;
  }

  /**
   * apply new friendship to someone.
   *
   * @param friend target user.
   * @param attributes additional attributes.
   * @return Observable instance to monitor operation result.
   */
  public Observable<AVFriendshipRequest> applyFriendshipInBackground(AVUser friend, Map<String, Object> attributes) {
    if (!checkUserAuthentication(null)) {
      logger.d("current user isn't authenticated.");
      return Observable.error(ErrorUtils.propagateException(AVException.SESSION_MISSING,
              "No valid session token, make sure signUp or login has been called."));
    }
    if (null == friend || StringUtil.isEmpty(friend.getObjectId())) {
      return Observable.error(ErrorUtils.propagateException(AVException.INVALID_PARAMETER,
              "friend user is invalid."));
    }
    Map<String, Object> param = new HashMap<>();
    param.put(AVFriendshipRequest.ATTR_USER, Utils.getParsedObject(this));
    param.put(AVFriendshipRequest.ATTR_FRIEND, Utils.getParsedObject(friend));
    if (null != attributes && attributes.size() > 0) {
      param.put(PARAM_ATTR_FRIENDSHIP, attributes);
    }
    JSONObject jsonObject = JSONObject.Builder.create(param);
    return PaasClient.getStorageClient().applyFriendshipRequest(jsonObject);
  }

  /**
   * update friendship attributes.
   *
   * @param friendship friendship instance.
   * @return observable instance.
   */
  public Observable<AVFriendship> updateFriendship(AVFriendship friendship) {
    if (!checkUserAuthentication(null)) {
      logger.d("current user isn't authenticated.");
      return Observable.error(ErrorUtils.propagateException(AVException.SESSION_MISSING,
              "No valid session token, make sure signUp or login has been called."));
    }
    if (null == friendship || StringUtil.isEmpty(friendship.getObjectId())) {
      return Observable.error(ErrorUtils.propagateException(AVException.INVALID_PARAMETER,
              "friendship request(objectId) is invalid."));
    }
    if (null == friendship.getFollowee() || StringUtil.isEmpty(friendship.getFollowee().getObjectId())) {
      return Observable.error(ErrorUtils.propagateException(AVException.INVALID_PARAMETER,
              "friendship request(followee) is invalid."));
    }
    JSONObject changedParam = friendship.generateChangedParam();
    if (null == changedParam || changedParam.size() < 1) {
      logger.d("nothing is changed within friendship.");
      return Observable.just(friendship);
    }
    HashMap<String, Object> param = new HashMap<>();
    param.put(PARAM_ATTR_FRIENDSHIP, changedParam);
    return PaasClient.getStorageClient().updateFriendship(getObjectId(), friendship.getFollowee().getObjectId(), param);
  }

  /**
   * accept a friendship.
   * @param request friendship request.
   * @param attributes additional attributes.
   * @return Observable instance to monitor operation result.
   * @notice: attributes is necessary as parameter bcz they are not properties of FriendshipRequest.
   */
  public Observable<AVFriendshipRequest> acceptFriendshipRequest(AVFriendshipRequest request,
                                                                 Map<String, Object> attributes) {
    if (!checkUserAuthentication(null)) {
      logger.d("current user isn't authenticated.");
      return Observable.error(ErrorUtils.propagateException(AVException.SESSION_MISSING,
              "No valid session token, make sure signUp or login has been called."));
    }
    if (null == request || StringUtil.isEmpty(request.getObjectId())) {
      return Observable.error(ErrorUtils.propagateException(AVException.INVALID_PARAMETER,
              "friendship request(objectId) is invalid."));
    }

    HashMap<String, Object> param = new HashMap<>();
    if (null != attributes && attributes.size() > 0) {
      param.put(PARAM_ATTR_FRIENDSHIP, attributes);
    }
    JSONObject jsonObject = JSONObject.Builder.create(param);
    return PaasClient.getStorageClient().acceptFriendshipRequest(request, jsonObject);
  }

  /**
   * decline a friendship.
   * @param request friendship request.
   * @return Observable instance to monitor operation result.
   */
  public Observable<AVFriendshipRequest> declineFriendshipRequest(AVFriendshipRequest request) {
    if (!checkUserAuthentication(null)) {
      logger.d("current user isn't authenticated.");
      return Observable.error(ErrorUtils.propagateException(AVException.SESSION_MISSING,
              "No valid session token, make sure signUp or login has been called."));
    }
    if (null == request || StringUtil.isEmpty(request.getObjectId())) {
      return Observable.error(ErrorUtils.propagateException(AVException.INVALID_PARAMETER,
              "friendship request(objectId) is invalid."));
    }

    return PaasClient.getStorageClient().declineFriendshipRequest(request);
  }

  /**
   * get query for AVFriendshipRequest.
   *
   * @param status request status. following value can be used individually or combined with `and` operator:
   *               AVFriendshipRequest.STATUS_PENDING(0x01) - request is pending yet.
   *               AVFriendshipRequest.STATUS_ACCEPTED(0x02) - request is accepted by user.
   *               AVFriendshipRequest.STATUS_DECLINED(0x04) - request is declined by user.
   *               AVFriendshipRequest.STATUS_ANY(0x07) - no matter status, all of requests are wanted by current query.
   * @param includeTargetUser boolean flag, indicating that need to include target user pointer or not.
   * @param requestToMe boolean flag, indicating all requests are sent to current user or not.
   *                    true - someone others sent requests to current user.
   *                    false - current user sent requests to others.
   * @return AVFriendshipRequest query, null for current user isn't authenticated or status is invlaid.
   */
  public AVQuery<AVFriendshipRequest> friendshipRequestQuery(int status,
                                                             boolean includeTargetUser, boolean requestToMe) {
    if (!checkUserAuthentication(null)) {
      logger.d("current user isn't authenticated.");
      return null;
    }
    List<String> statusCondition = new ArrayList<>(1);
    if ((status & AVFriendshipRequest.STATUS_PENDING) == AVFriendshipRequest.STATUS_PENDING) {
      statusCondition.add(AVFriendshipRequest.RequestStatus.Pending.name().toLowerCase());
    }
    if ((status & AVFriendshipRequest.STATUS_ACCEPTED) == AVFriendshipRequest.STATUS_ACCEPTED) {
      statusCondition.add(AVFriendshipRequest.RequestStatus.Accepted.name().toLowerCase());
    }
    if ((status & AVFriendshipRequest.STATUS_DECLINED) == AVFriendshipRequest.STATUS_DECLINED) {
      statusCondition.add(AVFriendshipRequest.RequestStatus.Declined.name().toLowerCase());
    }
    if (statusCondition.size() < 1) {
      logger.d("status parameter is invalid.");
      return null;
    }

    AVQuery<AVFriendshipRequest> result = new AVQuery<>(AVFriendshipRequest.CLASS_NAME);
    result.whereContainedIn(AVFriendshipRequest.ATTR_STATUS, statusCondition);
    if (requestToMe) {
      result.whereEqualTo(AVFriendshipRequest.ATTR_FRIEND, this);
      if (includeTargetUser) {
        result.include(AVFriendshipRequest.ATTR_USER);
      }
    } else {
      result.whereEqualTo(AVFriendshipRequest.ATTR_USER, this);
      if (includeTargetUser) {
        result.include(AVFriendshipRequest.ATTR_FRIEND);
      }
    }
    result.addDescendingOrder(AVObject.KEY_UPDATED_AT);
    return result;
  }

  private void processResultList(List<JSONObject> results, List<AVUser> list, String tag) {
    for (JSONObject item : results) {
      if (null != item) {
        AVUser user = (AVUser) Utils.parseObjectFromMap((Map<String, Object>)item.get(tag));
        list.add(user);
      }
    }
  }
  private Map<String, List<AVUser>> parseFollowerAndFollowee(JSONObject jsonObject) {
    Map<String, List<AVUser>> map = new HashMap<String, List<AVUser>>();
    if (null != jsonObject) {
      List<JSONObject> followers = AVUtils.getObjectListFromMapList((List<Map<String, Object>>)jsonObject.get("followers"));
      if ( null != followers && followers.size() > 0) {
        List<AVUser> followerUsers = new LinkedList<AVUser>();
        processResultList(followers, followerUsers, FOLLOWER_TAG);
        map.put(FOLLOWER_TAG, followerUsers);
      }
      List<JSONObject> followees = AVUtils.getObjectListFromMapList((List<Map<String, Object>>)jsonObject.get("followees"));
      if (null != followees && followees.size() > 0) {
        List<AVUser> followeeUsers = new LinkedList<AVUser>();
        processResultList(followees, followeeUsers, FOLLOWEE_TAG);
        map.put(FOLLOWEE_TAG, followeeUsers);
      }
    }
    return map;
  }
  public void getFollowersAndFolloweesInBackground(final FollowersAndFolloweesCallback callback) {
    if (null == callback) {
      return;
    }
    if (!checkUserAuthentication(callback)) {
      return;
    }
    PaasClient.getStorageClient().getFollowersAndFollowees(getObjectId()).subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(JSONObject jsonObject) {
        if (null == jsonObject) {
          callback.done(null, null);
        } else {
          Map<String, List<AVUser>> result = parseFollowerAndFollowee(jsonObject);
          callback.done(result, null);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        callback.done(null, new AVException(throwable));
      }

      @Override
      public void onComplete() {
      }
    });
  }

  /**
   * 通过设置此方法，所有关联对象中的 AVUser 对象都会被强转成注册的 AVUser 子类对象
   * @param clazz class name
   */
  public static void alwaysUseSubUserClass(Class<? extends AVUser> clazz) {
    AVUser.registerSubclass(clazz);
    subClazz = clazz;
  }
  private static Class internalUserClazz() {
    return subClazz == null ? AVUser.class: subClazz;
  }

  /**
   * 通过这个方法可以将 AVUser 对象强转为其子类对象
   *
   * @param user  user object.
   * @param clazz user class name.
   * @param <T> template type.
   * @return user subclass instance.
   */
  public static <T extends AVUser> T cast(AVUser user, Class<T> clazz) {
    try {
      T newUser = AVObject.cast(user, clazz);
      return newUser;
    } catch (Exception e) {

    }
    return null;
  }
}
