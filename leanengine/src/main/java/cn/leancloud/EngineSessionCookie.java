package cn.leancloud;

import java.net.URL;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.leancloud.impl.DefaultAVUserCookieSign;
import cn.leancloud.utils.LogUtil;

public class EngineSessionCookie {
  private static AVLogger LOGGER = LogUtil.getLogger(EngineSessionCookie.class);
  boolean fetchUser;

  ThreadLocal<HttpServletResponse> responseHolder = new ThreadLocal<HttpServletResponse>();
  ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<HttpServletRequest>();

  AVUserCookieSign sign;

  public EngineSessionCookie(String secret, int maxAge, boolean fetchUser) {
    this(new DefaultAVUserCookieSign(secret, maxAge), fetchUser);
  }

  public EngineSessionCookie(String secret, String sessionKey, int maxAge, boolean fetchUser) {
    this(new DefaultAVUserCookieSign(secret, sessionKey, maxAge), fetchUser);
  }

  public EngineSessionCookie(AVUserCookieSign sign, boolean fetchUser) {
    this.fetchUser = fetchUser;
    this.sign = sign;
  }

  protected AVUser parseCookie(HttpServletRequest req, HttpServletResponse response) {
    this.responseHolder.set(response);
    this.requestHolder.set(req);
    AVUser user = null;
    if (sign.validateCookieSign(req)) {
      user = sign.decodeUser(req);
      if (fetchUser && user != null && !user.isDataAvailable()) {
        try {
          user.fetch();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return user;
  }

  public void wrapCookie(boolean inResponse) {
    if (inResponse) {
      HttpServletResponse resp = responseHolder.get();
      HttpServletRequest req = requestHolder.get();
      if (resp != null) {
        AVUser u = EngineRequestContext.getAuthenticatedUser();
        String host = null;
        try {
          URL requestURL = new URL(req.getRequestURL().toString());
          host = requestURL.getHost();
        } catch (Exception e) {
          LOGGER.w(e);
        }
        addCookie(req, resp, sign.encodeUser(u));
        addCookie(req, resp, sign.getCookieSign(u));
      }
    } else {
      responseHolder.set(null);
    }
  }

  public static void addCookie(HttpServletRequest request, HttpServletResponse response,
      Cookie cookie) {
    Cookie[] cookies = request.getCookies();
    boolean contains = false;
    if (cookies != null && cookies.length > 0) {
      for (Cookie existingCookie : cookies) {
        if (cookie.getName().equals(existingCookie.getName())) {
          String cookieValue = cookie.getValue();
          if (cookieValue == null) {
            contains = existingCookie.getValue() == null;
          } else {
            contains = cookieValue.equals(existingCookie.getValue());
          }
        }
      }
    }
    if (!contains) {
      response.addCookie(cookie);
    }
  }
}
