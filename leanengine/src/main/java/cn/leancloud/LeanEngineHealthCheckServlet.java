package cn.leancloud;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.leancloud.json.JSONObject;

/**
 * 定义云函数中的健康检查函数
 * 
 * @author lbt05
 *
 */
@WebServlet(name = "LeanEngineHealthServlet", urlPatterns = {"/__engine/1/ping"})
public class LeanEngineHealthCheckServlet extends HttpServlet {

  private static final long serialVersionUID = -7406297470714318279L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    resp.setHeader("content-type", LeanEngine.JSON_CONTENT_TYPE);
    JSONObject result = new JSONObject();
    result.put("runtime", System.getProperty("java.version"));
    result.put("version", EngineAppConfiguration.getUserAgent());
    resp.getWriter().write(result.toJSONString());
  }
}
