package cn.leancloud;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;

import cn.leancloud.EndpointParser.EndpointInfo;

@WebServlet(name = "CloudCodeServlet",
    urlPatterns = {"/1/functions/*", "/1.1/functions/*", "/1/call/*", "/1.1/call/*"},
    loadOnStartup = 0)
public class CloudCodeServlet extends HttpServlet {

  private static final long serialVersionUID = -5828358153354045625L;
  private static final LCLogger LOGGER = LogUtil.getLogger(CloudCodeServlet.class);

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    setAllowOriginHeader(req, resp);
    resp.setHeader("Access-Control-Max-Age", "86400");
    resp.setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
    resp.setHeader("Access-Control-Allow-Headers",
        "X-LC-Id, X-LC-Key, X-LC-Session, X-LC-Sign, X-LC-Prod, X-LC-UA, X-Uluru-Application-Key, X-Uluru-Application-Id, X-Uluru-Application-Production, X-Uluru-Client-Version, X-Uluru-Session-Token, X-AVOSCloud-Application-Key, X-AVOSCloud-Application-Id, X-AVOSCloud-Application-Production, X-AVOSCloud-Client-Version, X-AVOSCloud-Session-Token, X-AVOSCloud-Super-Key, X-Requested-With, Content-Type, X-AVOSCloud-Request-sign");
    resp.setHeader("Content-Length", "0");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.getWriter().println();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    setAllowOriginHeader(req, resp);
    try {
      PlatformRequestAuthentication.validate(req);
    } catch (UnauthException e) {
      e.resp(resp);
      return;
    }
    EndpointInfo internalEndpoint = EndpointParser.getInternalEndpoint(req);
    LOGGER.d("endpoint info: " + internalEndpoint);

    if (internalEndpoint == null || StringUtil.isEmpty(internalEndpoint.getInternalEndpoint())
        || LeanEngine.getHandler(internalEndpoint.getInternalEndpoint()) == null) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
      resp.getWriter().println("{\"code\":\"400\",\"error\":\"Unsupported operation.\"}");
      return;
    } else {
      try {
        Object returnValue = LeanEngine.getHandler(internalEndpoint.getInternalEndpoint())
            .execute(req, internalEndpoint.isRPCcall());
        if (internalEndpoint.isNeedResponse()) {
          String respJSONStr = JSON.toJSONString(returnValue);
          resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
          resp.getWriter().write(respJSONStr);
          LOGGER.d("resp json string: " + respJSONStr);
        }
      } catch (IllegalArgumentException e) {
        if (internalEndpoint.isNeedResponse()) {
          InvalidParameterException ex = new InvalidParameterException();
          ex.resp(resp);
        }
        LOGGER.w(e);
      } catch (Exception e) {
        if (internalEndpoint.isNeedResponse()) {
          resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
          JSONObject result = JSONObject.Builder.create(null);
          if (e.getCause() instanceof LCException) {
            LCException ave = (LCException) e.getCause();
            result.put("code", ave.getCode());
            result.put("error", ave.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          } else {
            e.printStackTrace();
            result.put("code", 1);
            result.put("error", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          }
          resp.getWriter().write(result.toJSONString());
        }
      }
    }
  }


  private void setAllowOriginHeader(HttpServletRequest req, HttpServletResponse resp) {
    String allowOrigin = req.getHeader("origin");
    if (allowOrigin == null) {
      allowOrigin = "*";
    }
    resp.setHeader("Access-Control-Allow-Origin", allowOrigin);
  }
}
