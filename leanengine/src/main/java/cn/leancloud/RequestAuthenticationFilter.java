package cn.leancloud;

import cn.leancloud.utils.LogUtil;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter(filterName = "requestAuthenticationFilter", urlPatterns = {"/*"})
public class RequestAuthenticationFilter implements Filter {
  private static LCLogger LOGGER = LogUtil.getLogger(RequestAuthenticationFilter.class);

  public void init(FilterConfig filterConfig) throws ServletException {}

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      EngineRequestContext.clean();
      EngineSessionCookie sessionCookie = LeanEngine.getSessionCookie();
      if (sessionCookie != null && request instanceof HttpServletRequest
          && response instanceof HttpServletResponse) {
        LCUser requestUser = sessionCookie.parseCookie((HttpServletRequest) request, (HttpServletResponse) response);
        if (null != requestUser) {
          EngineRequestContext.setAuthenticatedUser(requestUser);
        }
      }
    } catch (Exception e) {
      LOGGER.w(e);
    }
    chain.doFilter(request, response);
  }

  public void destroy() {

  }
}
