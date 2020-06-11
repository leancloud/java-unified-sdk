package cn.leancloud;

import java.lang.reflect.Method;
import java.util.*;

import cn.leancloud.ops.Utils;
import cn.leancloud.json.JSON;

public class EngineHookHandlerInfo extends EngineHandlerInfo {

  public EngineHookHandlerInfo(String endpoint, Method handlerMethod,
      List<EngineFunctionParamInfo> params, Class returnType, String hookClass) {
    super(endpoint, handlerMethod, params, returnType, hookClass);
  }

  @Override
  public Object parseParams(String requestBody) throws InvalidParameterException {
    Map<String, Object> hookParams = JSON.parseObject(requestBody, Map.class);
    AVObject param = null;
    EngineFunctionParamInfo paramInfo = methodParameterList.get(0);
    if (AVUser.class.isAssignableFrom(paramInfo.type)) {
      param = new AVUser();
    } else {
      param = Transformer.objectFromClassName(hookClass);
    }
    EngineRequestContext.parseMetaData((Map<String, Object>) hookParams.get(paramInfo.getName()));
    param.resetServerData((Map<String, Object>) hookParams.get(paramInfo.getName()));
    return param;
  }

  @Override
  public Object wrapperResponse(Object result, String requestBody, boolean rpcCall) {
    Map<String, Object> hookParams = new HashMap<String, Object>();
    if (result != null) {
      Map<String, Object> objectMapping =
          (Map<String, Object>) Utils.getParsedObject(result, true);
      objectMapping.remove("__type");
      objectMapping.remove("className");

      // deal with updates within hook function.
      if (result instanceof AVObject) {
        Set<String> keys = ((AVObject)result).operations.keySet();
        for (String key : keys) {
          Object value = ((AVObject)result).get(key);
          if (null == value) {
            objectMapping.remove(key);
          } else {
            objectMapping.put(key, Utils.getParsedObject(value));
          }
        }
      }

      hookParams.putAll(objectMapping);
    }
    long ts = new Date().getTime();
    String sign = LeanEngine.hmacSha1(endPoint + ":" + ts, LeanEngine.getMasterKey());
    if (endPoint.startsWith("__before")) {
      hookParams.put("__before", ts + "," + sign);
    }
    if (endPoint.startsWith("__after")) {
      hookParams.put("__after", ts + "," + sign);
    }
    return hookParams;
  }

}
