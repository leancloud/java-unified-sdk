package cn.leancloud;

import javax.servlet.http.HttpServletRequest;

public class EndpointParser {

  static String getInternalEndpoint(String className, EngineHookType type) {
    switch (type) {
      case beforeSave:
      case afterSave:
      case beforeUpdate:
      case afterUpdate:
      case beforeDelete:
      case afterDelete:
        return type.toString() + "_for_" + className;
      case onVerifiedSMS:
      case onVerifiedEmail:
      case onLogin:
        return type.toString();
    }
    return null;
  }

  public static EndpointInfo getInternalEndpoint(HttpServletRequest req) {
    String requestPath = req.getRequestURI();
    String[] splited = requestPath.split("/");

    if (splited.length == 4) {
      if (splited[2].equals("functions") || splited[2].equals("call")) {
        RequestUserParser.parse(req);
        IMHookType hookType = IMHookType.parse(splited[3]);
        return new EndpointInfo(splited[3], hookType == null ? true : hookType.isResponseNeed,
            splited[2].equals("call"));
      } else {
        return null;
      }
    } else if (splited.length == 5) {
      EngineHookType type = EngineHookType.parse(splited[4]);
      return new EndpointInfo(getInternalEndpoint(splited[3], type), type.isResponseNeed, false);
    }
    return null;
  }

  public static class EndpointInfo {
    boolean needResponse;
    boolean isRPCcall;
    String internalEndpoint;

    public EndpointInfo(String intern, boolean response, boolean rpc) {
      this.internalEndpoint = intern;
      this.needResponse = response;
      this.isRPCcall = rpc;
    }

    public boolean isNeedResponse() {
      return needResponse;
    }

    public boolean isRPCcall() {
      return isRPCcall;
    }

    public String getInternalEndpoint() {
      return internalEndpoint;
    }

    @Override
    public String toString() {
      return "EndpointInfo [needResponse=" + needResponse + ", isRPCcall=" + isRPCcall
          + ", internalEndpoint=" + internalEndpoint + "]";
    }
  }
}
