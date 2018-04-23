package cn.leancloud;

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

@WebFilter(filterName = "requestUserAuthFilter", urlPatterns = {"/*"})
public class RequestUserAuthFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {}

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      AVUser.changeCurrentUser(null, false);
      EngineRequestContext.clean();
      EngineSessionCookie sessionCookie = LeanEngine.getSessionCookie();
      if (sessionCookie != null && request instanceof HttpServletRequest
          && response instanceof HttpServletResponse) {
        sessionCookie.parseCookie((HttpServletRequest) request, (HttpServletResponse) response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    chain.doFilter(request, response);
  }

  public void destroy() {

  }
}
