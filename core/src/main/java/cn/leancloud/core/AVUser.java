package cn.leancloud.core;

import cn.leancloud.AVLogger;
import cn.leancloud.network.PaasClient;
import cn.leancloud.utils.LogUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import java.util.HashMap;
import java.util.Map;

@JSONType(deserializer = ObjectTypeAdapter.class, serializer = ObjectTypeAdapter.class)
public class AVUser extends AVObject {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVUser.class);
  private static final String ATTR_USERNAME = "username";
  private static final String ATTR_PASSWORD = "password";
  private static final String ATTR_EMAIL = "email";
  private static final String ATTR_MOBILEPHONE = "mobilePhoneNumber";
  private static final String ATTR_MOBILEPHONE_VERIFIED = "mobilePhoneVerified";

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

  public Observable<AVUser> signUp() {
    JSONObject paramData = generateChangedParam();
    LOGGER.d("signup param: " + paramData.toJSONString());
    return PaasClient.getStorageClient().signUp(paramData);
  }

  public static Observable<AVUser> logIn(String username, String password) {
    return (Observable<AVUser>)logIn(username, password, AVUser.class);
  }

  public static <T extends AVUser> Observable<T> logIn(String username, String password, final Class<T> clazz) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("password", password);
    JSONObject data = new JSONObject(params);
    return PaasClient.getStorageClient().logIn(data, clazz);
  }
}
