package cn.leancloud;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public interface LCUserCookieSign {

  /**
   * decode user from http request.
   *
   * @param request http request
   * @return user instance.
   */
  LCUser decodeUser(HttpServletRequest request);

  /**
   * encode user into cookie.
   *
   * @param user user instance
   * @return cookie
   */
  Cookie encodeUser(LCUser user);

  /**
   * get cookie signature.
   *
   * @param user user instance
   * @return cookie
   */
  Cookie getCookieSign(LCUser user);

  /**
   * validate cookie signature.
   * @param request http request
   * @return flag indicating cookie sign is valid or not.
   */
  boolean validateCookieSign(HttpServletRequest request);
}
