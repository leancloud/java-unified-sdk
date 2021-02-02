package cn.leancloud;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.leancloud.core.GeneralRequestSignature;
import cn.leancloud.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class PlatformRequestAuthentication {

  private static final Logger logger = LogManager.getLogger(PlatformRequestAuthentication.class);
  static final String ANDROID_AFFILIATED_SUFFIX = "ax-sig-1";
  static final String SIGN_MASTERKEY_SUFFIX = "master";


  private String appId;
  private String appKey;
  private String masterKey;
  private String prod;
  private String sessionToken;
  private String sign;
  private String remoteAddress;

  public static void validate(HttpServletRequest req) throws UnauthException {
    PlatformRequestAuthentication info = parse(req);
    logger.debug("request auth: {}", info);

    if (null == info || info.getAppId() == null) {
      throw new UnauthException();
    }

    if (LeanEngine.getAppId().equals(info.getAppId()) //
        && (LeanEngine.getAppKey().equals(info.getAppKey()) //
            || LeanEngine.getMasterKey().equals(info.getAppKey()) //
            || LeanEngine.getMasterKey().equals(info.getMasterKey()))) {
      if (LeanEngine.getMasterKey().equals(info.getMasterKey())) {
        // 只有masterKey时才能获取metaData
        req.setAttribute("authMasterKey", true);
      }
      req.setAttribute(EngineRequestContext.ATTRIBUTE_KEY_AUTHENTICATION, info);
      return;
    }
    if (info.getSign() != null) {
      String[] split = info.getSign().split(",");
      String sign = split[0];
      String ts = split[1];
      String master = null;
      if (split.length > 2) {
        master = split[2];
      }
      if (ANDROID_AFFILIATED_SUFFIX.equals(master)) {
        String androidKey = LeanEngine.getAndroidKey();
        if (!StringUtil.isEmpty(androidKey)) {
          String computedSign = GeneralRequestSignature.requestSign(androidKey, Long.parseLong(ts), ANDROID_AFFILIATED_SUFFIX);
          if (info.getSign().equals(computedSign)) {
            req.setAttribute(EngineRequestContext.ATTRIBUTE_KEY_AUTHENTICATION, info);
            return;
          }
        }
      } else {
        boolean useMasterKey = SIGN_MASTERKEY_SUFFIX.equals(master);
        String computedSign =
                GeneralRequestSignature.requestSign(Long.parseLong(ts), useMasterKey);
        if (info.getSign().equals(computedSign)) {
          req.setAttribute(EngineRequestContext.ATTRIBUTE_KEY_AUTHENTICATION, info);
          return;
        }
      }
    }
    throw new UnauthException();
  }

  public static PlatformRequestAuthentication getInstance(HttpServletRequest req) {
    if (null == req) {
      return null;
    }
    return (PlatformRequestAuthentication) req.getAttribute(EngineRequestContext.ATTRIBUTE_KEY_AUTHENTICATION);
  }

  private static PlatformRequestAuthentication parse(HttpServletRequest req) {
    if (null == req) {
      return null;
    }
    if (req.getContentType() != null && req.getContentType().startsWith("text/plain")) {
      return null;
    } else {
      String appId = getHeaders(req, "x-lc-id", "x-avoscloud-application-id", "x-uluru-application-id");
      String appKey =
          getHeaders(req, "x-lc-key", "x-avoscloud-application-key", "x-uluru-application-key");
      String masterKey = getHeaders(req, "x-avoscloud-master-key", "x-uluru-master-key");
      if (appKey != null && appKey.indexOf(",master") > 0) {
        masterKey = appKey.substring(0, appKey.indexOf(",master"));
        appKey = null;
      }
      String prod = getHeaders(req, "x-lc-prod", "x-avoscloud-application-production",
          "x-uluru-application-production");
      if ("false".equals(prod)) {
        prod = "0";
      }
      String sessionToken =
          getHeaders(req, "x-lc-session", "x-uluru-session-token", "x-avoscloud-session-token");
      String sign = getHeaders(req, "x-lc-sign", "x-avoscloud-request-sign");

      // 放在这里只能算是一个side effect
      String remoteAddress = getHeaders(req, "x-real-ip", "x-forwarded-for");
      if (StringUtil.isEmpty(remoteAddress)) {
        remoteAddress = req.getRemoteAddr();
      }
      PlatformRequestAuthentication requestAuthentication = new PlatformRequestAuthentication(appId, appKey, masterKey,
              prod, sessionToken, sign, remoteAddress);
      EngineRequestContext.setSessionToken(sessionToken);
      EngineRequestContext.setRemoteAddress(remoteAddress);
      return requestAuthentication;
    }
  }

  private static String getHeaders(HttpServletRequest req, String... headers) {
    for (String header : headers) {
      String result = req.getHeader(header);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public PlatformRequestAuthentication(String appId, String appKey, String masterKey, String prod, String sessionToken,
                                       String sign, String remoteAddress) {
    this.appId = appId;
    this.appKey = appKey;
    this.masterKey = masterKey;
    this.prod = prod;
    this.sessionToken = sessionToken;
    this.sign = sign;
    this.remoteAddress = remoteAddress;
  }

  public String getAppId() {
    return appId;
  }

  public String getAppKey() {
    return appKey;
  }

  public String getMasterKey() {
    return masterKey;
  }

  public String getProd() {
    return prod;
  }

  public String getSessionToken() {
    return sessionToken;
  }

  public String getSign() {
    return sign;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  @Override
  public String toString() {
    return "RequestAuth [appId=" + appId + ", appKey="
        + (appKey != null ? appKey.substring(0, 2) + "..." : null) //
        + ", masterKey=" + (masterKey != null ? masterKey.substring(0, 2) + "..." : null) //
        + ", prod=" + prod + ", sessionToken=" + sessionToken + ", sign=" + sign + "]";
  }

}


class UnauthException extends Exception {

  private static final long serialVersionUID = -51778374436527741L;

  public void resp(HttpServletResponse resp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
    resp.getWriter().println("{\"code\":401,\"error\":\"Unauthorized.\"}");
  }
}


class InvalidParameterException extends Exception {
  public void resp(HttpServletResponse resp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
    resp.getWriter().println("{\"code\":400,\"error\":\"Invalid paramters.\"}");
  }
}
