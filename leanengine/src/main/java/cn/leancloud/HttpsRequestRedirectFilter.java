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

@WebFilter(filterName = "httpsRedirectFilter", urlPatterns = {"/*"})
public class HttpsRequestRedirectFilter implements Filter {
  private static AVLogger LOGGER = LogUtil.getLogger(HttpsRequestRedirectFilter.class);

  public void init(FilterConfig filterConfig) throws ServletException {}

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    try {
      if (request instanceof HttpServletRequest && LeanEngine.httpsRedirectionEnabled) {
        HttpServletRequest req = ((HttpServletRequest) request);
        String host = req.getHeader("host");
        String protocol = req.getHeader("x-forwarded-proto");
        String path = req.getRequestURI();
        boolean isProduction = "production".equals(LeanEngine.getAppEnv().toLowerCase());
        boolean isSubLeanAppDomain = host != null && (host.endsWith("leanapp.cn") || host.endsWith("avosapps.us"));
        boolean isHttps = "https".equals(protocol);
        boolean isCloudFuncUrl = path.startsWith("/1/functions/")
            || path.startsWith("/1/call/")
            || path.startsWith("/1.1/functions/")
            || path.startsWith("/1.1/call/");
        if ((isProduction || isSubLeanAppDomain) && !isHttps && !isCloudFuncUrl) {
          ((HttpServletResponse) response).sendRedirect("https://" + host + path);
          return;
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
