package cn.leancloud;

import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.core.PaasClient;
import cn.leancloud.ops.Utils;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.reactivex.Observable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JSONType(deserializer = ObjectTypeAdapter.class, serializer = ObjectTypeAdapter.class)
public class AVUser extends AVObject {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVUser.class);
  private static final String ATTR_USERNAME = "username";
  private static final String ATTR_PASSWORD = "password";
  private static final String ATTR_EMAIL = "email";
  private static final String ATTR_MOBILEPHONE = "mobilePhoneNumber";
  private static final String ATTR_MOBILEPHONE_VERIFIED = "mobilePhoneVerified";
  private static final String ATTR_SESSION_TOKEN = "sessionToken";

  public static final String CLASS_NAME = "_User";
  public enum SNS_PLATFORM {
    FACEBOOK("facebook"), TWITTER("twitter"), QQ("qq"), WEIBO("weibo"), WECHAT("weixin");
    private SNS_PLATFORM(String name) {
      this.name = name;
    }
    private String name;
    public String getName() {
      return this.name;
    }
  };

  public AVUser() {
    super(CLASS_NAME);
  }

  public static AVUser currentUser() {
    return null;
  }

  @JSONField(serialize = false)
  public String getEmail() {
    return (String) get(ATTR_EMAIL);
  }
  public void setEmail(String email) {
    put(ATTR_EMAIL, email);
  }

  @JSONField(serialize = false)
  public String getUsername() {
    return (String) get(ATTR_USERNAME);
  }
  public void setUsername(String name) {
    put(ATTR_USERNAME, name);
  }

  @JSONField(serialize = false)
  public String getPassword() {
    return (String) get(ATTR_PASSWORD);
  }
  public void setPassword(String password) {
    put(ATTR_PASSWORD, password);
  }

  @JSONField(serialize = false)
  public String getMobilePhoneNumber() {
    return (String) get(ATTR_MOBILEPHONE);
  }
  public void setMobilePhoneNumber(String mobile) {
    put(ATTR_MOBILEPHONE, mobile);
  }

  @JSONField(serialize = false)
  public boolean isMobilePhoneVerified() {
    return getBoolean(ATTR_MOBILEPHONE_VERIFIED);
  }

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

  public void signUp() {
    signUpInBackground().blockingSubscribe();
  }

  public Observable<AVUser> signUpInBackground() {
    JSONObject paramData = generateChangedParam();
    LOGGER.d("signup param: " + paramData.toJSONString());
    return PaasClient.getStorageClient().signUp(paramData);
  }

  public static Observable<AVUser> logIn(String username, String password) {
    return (Observable<AVUser>)logIn(username, password, AVUser.class);
  }

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

  /**
   * Session token operations
   */

  public Observable<Boolean> checkAuthenticatedInBackground() {
    String sessionToken = getSessionToken();
    if (StringUtil.isEmpty(sessionToken)) {
      LOGGER.d("sessionToken is not existed.");
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
    AVQuery<T> query = new AVQuery<T>(CLASS_NAME, clazz);
    return query;
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

  public static boolean isEnableAutomatic() {
    return enableAutomatic;
  }

  public static void disableAutomaticUser() {
    enableAutomatic = false;
  }

  private static File currentUserArchivePath() {
    File file = new File(PersistenceUtil.sharedInstance().getDocumentDir() + "/currentUser");
    return file;
  }

  static private boolean userArchiveExist() {
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
      LOGGER.d(jsonString);
      PersistenceUtil.sharedInstance().saveContentToFile(jsonString, currentUserArchivePath);
    } else if (save) {
      PersistenceUtil.sharedInstance().removeLock(currentUserArchivePath.getAbsolutePath());
      currentUserArchivePath.delete();
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
          if (jsonString.indexOf("@type") > 0) {
            // new version.
            try {
              AVUser newUser = (AVUser) JSON.parse(jsonString);
              if (userClass.isAssignableFrom(newUser.getClass())) {
                user = newUser;
              } else {
                T tmp = userClass.newInstance();
                tmp.resetByRawData(newUser);
                user = tmp;
              }
              PaasClient.getStorageClient().setCurrentUser(user);
            } catch (Exception ex) {
              ;
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
              ;
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
        ;
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
