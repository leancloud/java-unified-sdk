package cn.leancloud;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public interface AVUserCookieSign {

  /**
   * decode user from http request.
   *
   * @param request
   * @return
   */
  AVUser decodeUser(HttpServletRequest request);

  /**
   * encode user into cookie.
   *
   * @param user
   * @return
   */
  Cookie encodeUser(AVUser user);

  /**
   * get cookie signature.
   *
   * @param user
   * @return
   */
  Cookie getCookieSign(AVUser user);

  /**
   * validate cookie signature.
   * @param request
   * @return
   */
  boolean validateCookieSign(HttpServletRequest request);
}
