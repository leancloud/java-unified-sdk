package cn.leancloud;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public interface AVUserCookieSign {

  /**
   * decode user from http request.
   *
   * @param request http request
   * @return user instance.
   */
  AVUser decodeUser(HttpServletRequest request);

  /**
   * encode user into cookie.
   *
   * @param user user instance
   * @return cookie
   */
  Cookie encodeUser(AVUser user);

  /**
   * get cookie signature.
   *
   * @param user user instance
   * @return cookie
   */
  Cookie getCookieSign(AVUser user);

  /**
   * validate cookie signature.
   * @param request http request
   * @return flag indicating cookie sign is valid or not.
   */
  boolean validateCookieSign(HttpServletRequest request);
}
