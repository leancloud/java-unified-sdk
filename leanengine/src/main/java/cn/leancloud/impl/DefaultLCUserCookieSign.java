package cn.leancloud.impl;

import cn.leancloud.*;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.json.JSON;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DefaultLCUserCookieSign implements LCUserCookieSign {
  private static final LCLogger LOGGER = LogUtil.getLogger(DefaultLCUserCookieSign.class);

  private static final String SESSION_TOKEN = "_sessionToken";
  private static final String UID = "_uid";
  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

  static {
    System.setProperty("org.glassfish.web.rfc2109_cookie_names_enforced", "false");
  }
  String sessionKey;
  String secret;
  int maxAge;

  public DefaultLCUserCookieSign(String secret, int maxAge) {
    this(secret, "avos.sess", maxAge);
  }

  public DefaultLCUserCookieSign(String secret, String sessionKey, int maxAge) {
    this.sessionKey = sessionKey;
    this.secret = secret;
    this.maxAge = maxAge;
  }

  public LCUser decodeUser(HttpServletRequest request) {
    Cookie cookie = getCookie(request, sessionKey);
    if (cookie != null) {
      String userInfoStr = new String(Base64.getDecoder().decode(cookie.getValue()));
      Map<String, Object> userInfo = JSON.parseObject(userInfoStr, Map.class);
      System.out.println("decode user Map: " + userInfoStr);
      if (userInfo.containsKey(UID) && userInfo.containsKey(SESSION_TOKEN)) {
        LCUser user;
        String userObjectId = (String) userInfo.get(UID);
        try {
          user = LCUser.createWithoutData(LCUser.class, userObjectId);
          Map<String, Object> value = new HashMap<String, Object>();
          value.put(LCUser.ATTR_SESSION_TOKEN, userInfo.get(SESSION_TOKEN));
          value.put(LCObject.KEY_OBJECT_ID, userObjectId);
          user.resetServerData(value);
          return user;
        } catch (LCException e) {
          LOGGER.w(e);
        }
      }
    }
    return null;
  }

  public Cookie encodeUser(LCUser user) {
    if (user != null) {
      String cookieValue = getUserCookieValue(user);
      Cookie cookie = new Cookie(sessionKey, cookieValue);
      cookie.setMaxAge(maxAge);
      cookie.setPath("/");
      return cookie;
    } else {
      Cookie cookie = new Cookie(sessionKey, null);
      cookie.setMaxAge(0);
      cookie.setPath("/");
      return cookie;
    }
  }

  public Cookie getCookieSign(LCUser user) {
    Cookie cookie = new Cookie(sessionKey + ".sig", null);
    cookie.setPath("/");
    if (user != null) {
      String cookieValue = getUserCookieValue(user);
      String text = sessionKey + "=" + cookieValue;
      try {
        cookie.setValue(encrypt(secret, text));
        cookie.setMaxAge(maxAge);
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        e.printStackTrace();
      }
    } else {
      cookie.setMaxAge(0);
    }
    return cookie;
  }

  public boolean validateCookieSign(HttpServletRequest request) {
    Cookie userCookie = getCookie(request, sessionKey);
    Cookie cookieSign = getCookie(request, sessionKey + ".sig");
    if (userCookie != null && cookieSign != null && cookieSign.getValue() != null
            && userCookie.getValue() != null) {
      try {
        String encryptedCookieValue = encrypt(secret, sessionKey + "=" + userCookie.getValue());
        return cookieSign.getValue().equals(encryptedCookieValue);
      } catch (Exception e) {

      }
    }
    return false;
  }

  public static String encrypt(String secret, String text)
          throws NoSuchAlgorithmException, InvalidKeyException {
    SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
    Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
    mac.init(signingKey);
    byte[] rawHmac = mac.doFinal(text.getBytes());
    return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
  }

  private String getUserCookieValue(LCUser user) {
    Map<String, String> userInfo = new HashMap<String, String>();
    userInfo.put(UID, user.getObjectId());
    userInfo.put(SESSION_TOKEN, user.getSessionToken());
    String userInfoStr = JSON.toJSONString(userInfo);
    String cookieValue = Base64.getEncoder().encodeToString(userInfoStr.getBytes());
    return cookieValue;
  }

  private static Cookie getCookie(HttpServletRequest req, String cookieName) {
    Cookie[] cookies = req.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(cookieName)) {
        return cookie;
      }
    }
    return null;
  }


}
